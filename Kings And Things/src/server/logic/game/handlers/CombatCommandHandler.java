package server.logic.game.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import server.event.DiceRolled;
import server.event.commands.ApplyHitsCommand;
import server.event.commands.ResolveCombat;
import server.logic.game.Player;
import server.logic.game.Roll;
import server.logic.game.validators.CombatPhaseValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants.Ability;
import common.Constants.CombatPhase;
import common.Constants.RollReason;
import common.Logger;
import common.game.HexState;
import common.game.TileProperties;

public class CombatCommandHandler extends CommandHandler
{
	/**
	 * Call this to resolve combat in, or explore, a particular hex
	 * @param hex The hex to resolve
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If parameters are invalid, or
	 * if combat in hex can not be resolved according to game rules
	 * @throws IllegalStateException If it is not the combat phase,
	 * or if another combat is already being resolved
	 */
	public void resolveCombat(TileProperties hex, int playerNumber)
	{
		CombatPhaseValidator.validateCanResolveCombat(hex,playerNumber,getCurrentState());
		beginCombatResolution(hex, playerNumber);
	}

	public void applyHits(TileProperties thing, int playerNumber, int hitCount)
	{
		CombatPhaseValidator.validateCanApplyHits(thing, playerNumber, hitCount, getCurrentState());
		makeHitsApplied(thing,playerNumber,hitCount);
	}

	private void beginCombatResolution(TileProperties hex, int playerNumber)
	{
		boolean isExploration = true;
		Player defender = null;
		for(Player p : getCurrentState().getPlayers())
		{
			if(p.ownsHex(hex))
			{
				isExploration = false;
				defender = p;
			}
		}
		getCurrentState().setCombatLocation(getCurrentState().getBoard().getXYCoordinatesOfHex(hex));
		if(isExploration)
		{
			List<Integer> playerOrder = getCurrentState().getPlayerOrder();
			int attackerIndex = playerOrder.indexOf(playerNumber);
			int defenderIndex = attackerIndex>0? attackerIndex-1 : playerOrder.size()-1;
			
			getCurrentState().setCurrentCombatPhase(CombatPhase.DETERMINE_DEFENDERS);
			getCurrentState().setDefendingPlayerNumber(playerOrder.get(defenderIndex));
			getCurrentState().addNeededRoll(new Roll(1,getCurrentState().getCombatHex().getHex(),RollReason.EXPLORE_HEX,playerNumber));
		}
		else
		{
			getCurrentState().setDefendingPlayerNumber(defender.getID());
			getCurrentState().setCurrentCombatPhase(CombatPhase.SELECT_TARGET_PLAYER);
			advanceToNextCombatPhase();
		}
	}

	private void makeHitsApplied(TileProperties thing, int playerNumber, int hitCount)
	{
		Player player = getCurrentState().getPlayerByPlayerNumber(playerNumber);
		
		if(thing.hasAbility(Ability.Armor))
		{
			thing.setValue(thing.getValue() - hitCount);
		}
		
		if(thing.getValue() == 0 || !thing.hasAbility(Ability.Armor))
		{
			//buildings are now neutralized
			if(thing.isBuilding())
			{
				thing.flip();
			}
			else
			{
				//TODO check if special character first
				thing.resetValue();
				getCup().reInsertTile(thing);
				player.removeOwnedThingOnBoard(thing);
				getCurrentState().getCombatHex().removeThingFromHex(thing);
			}
		}
		
		getCurrentState().removeHitsFromPlayer(playerNumber, hitCount);
		
		if(!getCurrentState().hitsToApply())
		{
			advanceToNextCombatPhase();
		}
	}
	
	private void advanceToNextCombatPhase()
	{
		int currentPhaseOrdinal = getCurrentState().getCurrentCombatPhase().ordinal();
		CombatPhase nextPhase = getCombatPhaseByOrdinal((currentPhaseOrdinal + 1) % CombatPhase.values().length);
		
		HexState combatHex = getCurrentState().getCombatHex();
		getCurrentState().setCurrentCombatPhase(nextPhase);
		Set<TileProperties> things = combatHex.getFightingThingsInHex();
		if(nextPhase == CombatPhase.MAGIC_ATTACK || nextPhase == CombatPhase.RANGED_ATTACK || nextPhase == CombatPhase.MELEE_ATTACK)
		{
			for(TileProperties thing : things)
			{
				if((nextPhase == CombatPhase.MAGIC_ATTACK && thing.hasAbility(Ability.Magic)) ||
					(nextPhase == CombatPhase.RANGED_ATTACK && thing.hasAbility(Ability.Range)) ||
					(nextPhase == CombatPhase.MELEE_ATTACK && (!thing.hasAbility(Ability.Range) && !thing.hasAbility(Ability.Magic))))
				{
					for(Player p : getCurrentState().getPlayers())
					{
						if(p.ownsThingOnBoard(thing))
						{
							int diceCount = thing.isSpecialCreatureWithAbility(Ability.Charge)? 2 : 1;
							getCurrentState().addNeededRoll(new Roll(diceCount,thing,RollReason.ATTACK_WITH_CREATURE,p.getID()));
						}
					}
				}
			}
			if(!getCurrentState().isWaitingForRolls())
			{
				getCurrentState().setCurrentCombatPhase(getCombatPhaseByOrdinal(nextPhase.ordinal() + 1));
				advanceToNextCombatPhase();
			}
		}
	}
	
	private CombatPhase getCombatPhaseByOrdinal(int ordinal)
	{
		for(CombatPhase phase : CombatPhase.values())
		{
			if(phase.ordinal() == ordinal)
			{
				return phase;
			}
		}
		
		throw new IllegalArgumentException("Recieved invalid combat phase ordinal: " + ordinal);
	}

	private void applyRollEffects()
	{
		ArrayList<Roll> handledRolls = new ArrayList<Roll>();
		for(Roll r : getCurrentState().getFinishedRolls())
		{
			switch(r.getRollReason())
			{
				case ATTACK_WITH_CREATURE:
				{
					handledRolls.add(r);
					//TODO handle 3 and 4 way combat by adding targetting mechanism
					Player rollingPlayer = getCurrentState().getPlayerByPlayerNumber(r.getRollingPlayerID());
					int hitCount = 0;
					for(int roll : r.getRolls())
					{
						if(roll <= r.getRollTarget().getValue())
						{
							hitCount++;
						}
					}
					if(hitCount > 0)
					{
						for(Player p : getCurrentState().getPlayers())
						{
							for(TileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
							{
								if(p.ownsThingOnBoard(tp) && !p.equals(rollingPlayer))
								{
									getCurrentState().addHitsToPlayer(p.getID(), hitCount);
									break;
								}
							}
						}
						//TODO notify players of hits
					}
					int nextOrdinal = getCurrentState().getCurrentCombatPhase().ordinal() + 1;
					if(!getCurrentState().hitsToApply())
					{
						nextOrdinal++;
					}
					for(CombatPhase phase : CombatPhase.values())
					{
						if(phase.ordinal() == nextOrdinal)
						{
							getCurrentState().setCurrentCombatPhase(phase);
						}
					}
					break;
				}
				case CALCULATE_DAMAGE_TO_TILE:
					handledRolls.add(r);
					//TODO handle
					break;
				case EXPLORE_HEX:
				{
					handledRolls.add(r);
					if(isDemoMode())
					{
						//give hex to player
						makeHexOwnedByPlayer(r.getRollTarget(), r.getRollingPlayerID());
						getCurrentState().setCurrentCombatPhase(CombatPhase.PLACE_THINGS);
					}
					else
					{
						//TODO implement regular exploration
					}
					break;
				}
				default:
					break;
			}
		}
		
		for(Roll r : handledRolls)
		{
			getCurrentState().removeRoll(r);
		}
	}
	
	@Subscribe
	public void receiveApplyRollCommand(DiceRolled event)
	{
		try
		{
			applyRollEffects();
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process DiceRolled due to: ", t);
		}
	}

	@Subscribe
	public void receiveApplyHitsCommand(ApplyHitsCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				applyHits(command.getTarget(), command.getPlayerID(), command.getNumHits());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ApplyHitsCommand due to: ", t);
			}
		}
	}

	@Subscribe
	public void receiveResolveCombatCommand(ResolveCombat command)
	{
		if(command.isUnhandled())
		{
			try
			{
				resolveCombat(command.getCombatHex(), command.getPlayerID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ResolveCombatCommand due to: ", t);
			}
		}
	}
}

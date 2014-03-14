package server.logic.game.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import server.event.commands.ApplyHitsCommand;
import server.event.commands.DiceRolled;
import server.event.commands.PlayerWaivedRetreat;
import server.event.commands.ResolveCombat;
import server.logic.game.BuildableBuildingGenerator;
import server.logic.game.Player;
import server.logic.game.validators.CombatPhaseValidator;

import com.google.common.eventbus.Subscribe;
import common.Constants.Ability;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.CombatPhase;
import common.Constants.RollReason;
import common.Logger;
import common.event.notifications.CombatHits;
import common.event.notifications.HexStatesChanged;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Roll;

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
	public void resolveCombat(ITileProperties hex, int playerNumber)
	{
		CombatPhaseValidator.validateCanResolveCombat(hex,playerNumber,getCurrentState());
		beginCombatResolution(hex, playerNumber);
	}

	public void applyHits(ITileProperties thing, int playerNumber, int hitCount)
	{
		CombatPhaseValidator.validateCanApplyHits(thing, playerNumber, hitCount, getCurrentState());
		makeHitsApplied(thing,playerNumber,hitCount);
	}

	private void beginCombatResolution(ITileProperties hex, int playerNumber)
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
			for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
			{
				if(!tp.isFaceUp())
				{
					tp.flip();
				}
			}
			getCurrentState().setDefendingPlayerNumber(defender.getID());
			getCurrentState().setCurrentCombatPhase(CombatPhase.SELECT_TARGET_PLAYER);
			advanceToNextCombatPhase();
		}
	}

	private void makeHitsApplied(ITileProperties thing, int playerNumber, int hitCount)
	{
		Player player = getCurrentState().getPlayerByPlayerNumber(playerNumber);
		
		if(thing.hasAbility(Ability.Armor))
		{
			thing.setValue(thing.getValue() - hitCount);
		}
		
		if(thing.getValue() == 0 || !thing.hasAbility(Ability.Armor))
		{
			//buildings are now neutralized
			//TODO let user choose to flip if it's a special character
			thing.flip();
			if(!thing.isBuilding())
			{
				thing.resetValue();
				if(thing.isSpecialCharacter())
				{
					getCurrentState().getBankHeroes().reInsertTile(thing);
				}
				else
				{
					getCurrentState().getCup().reInsertTile(thing);
				}
				player.removeOwnedThingOnBoard(thing);
				boolean success = getCurrentState().getCombatHex().removeThingFromHex(thing);
				if(!success)
				{
					throw new IllegalStateException("Unable to remove combat creature from hex");
				}
			}
			HexStatesChanged notification = new HexStatesChanged(1);
			notification.getArray()[0] = getCurrentState().getCombatHex();
			notification.postNetworkEvent();
		}
		
		getCurrentState().removeHitsFromPlayer(playerNumber, hitCount);
		
		boolean someoneNeedsToApplyHits = false;
		HashSet<Integer> playersStillInCombat = new HashSet<Integer>();
		for(Player p : getCurrentState().getPlayers())
		{
			for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
			{
				if(p.ownsThingOnBoard(tp))
				{
					playersStillInCombat.add(p.getID());
					if(getCurrentState().getHitsOnPlayer(p.getID())>0)
					{
						someoneNeedsToApplyHits = true;
					}
				}
			}
			if(someoneNeedsToApplyHits && playersStillInCombat.size()>1)
			{
				break;
			}
		}
		
		if(!someoneNeedsToApplyHits)
		{
			if(playersStillInCombat.size()>1)
			{
				advanceToNextCombatPhase();
			}
			else
			{
				getCurrentState().removeAllHitsFromAllPlayers();
				getCurrentState().setCurrentCombatPhase(CombatPhase.DETERMINE_DAMAGE);
				for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
				{
					if(tp.isFaceUp() && tp.isCreature() && !tp.isSpecialCharacter())
					{
						tp.flip();
					}
				}
				Player oldOwner = null;
				Player newOwner = null;
				for(Player p : getCurrentState().getPlayers())
				{
					if(p.ownsHex(getCurrentState().getCombatHex().getHex()))
					{
						oldOwner = p;
						break;
					}
				}
				for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
				{
					for(Player p : getCurrentState().getPlayers())
					{
						if(p.ownsThingOnBoard(tp))
						{
							newOwner = p;
							break;
						}
					}
				}
				if(newOwner != null && !oldOwner.equals(newOwner))
				{
					oldOwner.removeHexFromOwnership(getCurrentState().getCombatHex().getHex());
					newOwner.addOwnedHex(getCurrentState().getCombatHex().getHex());
					if(getCurrentState().getCombatHex().hasSpecialIncomeCounter())
					{
						oldOwner.removeOwnedThingOnBoard(getCurrentState().getCombatHex().getSpecialIncomeCounter());
						newOwner.addOwnedThingOnBoard(getCurrentState().getCombatHex().getSpecialIncomeCounter());
					}
					if(getCurrentState().getCombatHex().hasBuilding())
					{
						oldOwner.removeOwnedThingOnBoard(getCurrentState().getCombatHex().getBuilding());
						newOwner.addOwnedThingOnBoard(getCurrentState().getCombatHex().getBuilding());
					}
				}
				if(getCurrentState().getCombatHex().hasSpecialIncomeCounter())
				{
					getCurrentState().addNeededRoll(new Roll(1,getCurrentState().getCombatHex().getSpecialIncomeCounter(),RollReason.CALCULATE_DAMAGE_TO_TILE,newOwner==null? oldOwner.getID() : newOwner.getID()));
				}
				if(getCurrentState().getCombatHex().hasBuilding())
				{
					ITileProperties building = getCurrentState().getCombatHex().getBuilding();
					if(!building.getName().equals(Building.Citadel.name()))
					{
						getCurrentState().addNeededRoll(new Roll(1,building,RollReason.CALCULATE_DAMAGE_TO_TILE,newOwner==null? oldOwner.getID() : newOwner.getID()));
					}
				}
			}
		}
	}
	
	private void playerWaivedRetreat(int playerNumber)
	{
		if(getCurrentState().getCurrentCombatPhase() == CombatPhase.ATTACKER_RETREAT)
		{
			advanceToNextCombatPhase();
		}
		else if(getCurrentState().getCurrentCombatPhase() == CombatPhase.DEFENDER_RETREAT)
		{
			//wrap around to magic attack
			getCurrentState().setCurrentCombatPhase(getCombatPhaseByOrdinal(CombatPhase.MAGIC_ATTACK.ordinal()-1));
			advanceToNextCombatPhase();
		}
		else
		{
			throw new IllegalStateException("No one has the option of retreating.");
		}
	}
	
	private void advanceToNextCombatPhase()
	{
		int currentPhaseOrdinal = getCurrentState().getCurrentCombatPhase().ordinal();
		CombatPhase nextPhase = getCombatPhaseByOrdinal((currentPhaseOrdinal + 1) % CombatPhase.values().length);
		
		HexState combatHex = getCurrentState().getCombatHex();
		getCurrentState().setCurrentCombatPhase(nextPhase);
		Set<ITileProperties> things = combatHex.getFightingThingsInHex();
		if(nextPhase == CombatPhase.MAGIC_ATTACK || nextPhase == CombatPhase.RANGED_ATTACK || nextPhase == CombatPhase.MELEE_ATTACK)
		{
			for(ITileProperties thing : things)
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
		boolean attackedWithCreature = false;
		boolean determinedDamage = false;
		for(Roll r : getCurrentState().getFinishedRolls())
		{
			switch(r.getRollReason())
			{
				case ATTACK_WITH_CREATURE:
				{
					handledRolls.add(r);
					attackedWithCreature = true;
					//TODO handle 3 and 4 way combat by adding targeting mechanism
					Player rollingPlayer = getCurrentState().getPlayerByPlayerNumber(r.getRollingPlayerID());
					int hitCount = 0;
					final int rollingPlayerID = rollingPlayer.getID();
					
					for(int roll : r.getFinalRolls())
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
							for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
							{
								if(p.ownsThingOnBoard(tp) && !p.equals(rollingPlayer))
								{
									getCurrentState().addHitsToPlayer(p.getID(), hitCount);
									//notifies players of hits
									new CombatHits(rollingPlayerID,p.getID(),hitCount).postNetworkEvent();
									break;
								}
							}
						}
					}
					break;
				}
				case CALCULATE_DAMAGE_TO_TILE:
				{
					handledRolls.add(r);
					determinedDamage = true;
					int roll = r.getFinalRolls().get(0);
					if(roll == 1 || roll == 6)
					{
						ITileProperties rollTarget = r.getRollTarget();
						Player owningPlayer = null;
						for(Player p : getCurrentState().getPlayers())
						{
							if(p.ownsThingOnBoard(rollTarget))
							{
								owningPlayer = p;
								break;
							}
						}
						owningPlayer.removeOwnedThingOnBoard(rollTarget);
						getCurrentState().getCombatHex().removeThingFromHex(rollTarget);
						if(rollTarget.isSpecialIncomeCounter())
						{
							getCurrentState().getCup().reInsertTile(rollTarget);
						}
						else if(!rollTarget.getName().equals(Building.Tower.name()) && rollTarget.isBuildableBuilding())
						{
							if(rollTarget.getName().equals(BuildableBuilding.Castle.name()))
							{
								ITileProperties newBuilding = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Keep);
								getCurrentState().getCombatHex().addThingToHex(newBuilding);
								owningPlayer.addOwnedThingOnBoard(newBuilding);
							}
							else if(rollTarget.getName().equals(BuildableBuilding.Keep.name()))
							{
								ITileProperties newBuilding = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Tower);
								getCurrentState().getCombatHex().addThingToHex(newBuilding);
								owningPlayer.addOwnedThingOnBoard(newBuilding);
							}
							else
							{
								throw new IllegalStateException("Unrecognized building type: " + rollTarget.getName());
							}
						}
					}
					if(getCurrentState().getCombatHex().hasBuilding() && !getCurrentState().getCombatHex().getBuilding().isFaceUp())
					{
						getCurrentState().getCombatHex().getBuilding().flip();
						getCurrentState().getCombatHex().getBuilding().resetValue();
					}
					break;
				}
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
		if(attackedWithCreature)
		{
			getCurrentState().setCurrentCombatPhase(getCombatPhaseByOrdinal(getCurrentState().getCurrentCombatPhase().ordinal() + 1));
			if(!getCurrentState().hitsToApply())
			{
				advanceToNextCombatPhase();
			}
		}
		if(determinedDamage)
		{
			for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
			{
				if(!tp.isFaceUp() && tp.isBuilding())
				{
					tp.flip();
				}
			}
			getCurrentState().setCurrentCombatPhase(getCombatPhaseByOrdinal(getCurrentState().getCurrentCombatPhase().ordinal() + 1));
		}
		for(Roll r : handledRolls)
		{
			getCurrentState().removeRoll(r);
		}
	}
	
	@Subscribe
	public void receivePlayerWaivedRetreat(PlayerWaivedRetreat event)
	{
		if(event.isUnhandled())
		{
			try
			{
				playerWaivedRetreat(event.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process PlayerWaivedRetreat due to: ", t);
			}
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
				applyHits(command.getTarget(), command.getID(), command.getNumHits());
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
				resolveCombat(command.getCombatHex(), command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ResolveCombatCommand due to: ", t);
			}
		}
	}
}

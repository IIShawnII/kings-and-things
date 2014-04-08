package server.logic.game.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import server.event.DiceRolled;
import server.event.PlayerRemovedThingsFromHex;
import server.event.PlayerWaivedBribe;
import server.event.PlayerWaivedRetreat;
import server.event.internal.ApplyHitsCommand;
import server.event.internal.BribeDefenderCommand;
import server.event.internal.ResolveCombatCommand;
import server.event.internal.RetreatCommand;
import server.event.internal.TargetPlayerCommand;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.BuildableBuildingGenerator;
import server.logic.game.validators.CombatPhaseValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.Ability;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.CombatPhase;
import common.Constants.RollReason;
import common.Constants.UpdateInstruction;
import common.Logger;
import common.event.network.CombatHits;
import common.event.network.CommandRejected;
import common.event.network.CurrentPhase;
import common.event.network.ExplorationResults;
import common.event.network.HexNeedsThingsRemoved;
import common.event.network.HexStatesChanged;
import common.event.network.InitiateCombat;
import common.event.network.PlayerTargetChanged;
import common.event.network.PlayersList;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
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
	
	public void bribeDefender(ITileProperties defender, int playerNumber)
	{
		CombatPhaseValidator.validateCanBribeDefender(defender,playerNumber,getCurrentState());
		makeDefenderBribed(defender,playerNumber);
	}

	public void applyHits(ITileProperties thing, int playerNumber, int hitCount)
	{
		ITileProperties myVersion = null;
		for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
		{
			if(tp.equals(thing))
			{
				myVersion = tp;
			}
		}
		CombatPhaseValidator.validateCanApplyHits(myVersion, playerNumber, hitCount, getCurrentState());
		makeHitsApplied(myVersion,playerNumber,hitCount);
	}
	
	public void setPlayersTarget(int playerNumber, int targetPlayerNumber)
	{
		CombatPhaseValidator.validateCanTargetPlayer(playerNumber, targetPlayerNumber, getCurrentState());
		makePlayersTarget(playerNumber, targetPlayerNumber);
	}
	
	public void retreatFromCombat(int playerNumber, ITileProperties destinationHex)
	{
		CombatPhaseValidator.validateCanRetreatFromCombat(playerNumber, destinationHex, getCurrentState());
		retreatFromHex(playerNumber,destinationHex);
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
		getCurrentState().getCombatHex().setMarker(Constants.getPlayerMarker(Constants.PUBLIC));
		HexStatesChanged msg = new HexStatesChanged(1);
		msg.getArray()[0] = getCurrentState().getCombatHex();
		msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
		if(isExploration)
		{
			List<Integer> playerOrder = getCurrentState().getPlayerOrder();
			int attackerIndex = playerOrder.indexOf(playerNumber);
			int defenderIndex = attackerIndex>0? attackerIndex-1 : playerOrder.size()-1;
			getCurrentState().setDefendingPlayerNumber(playerOrder.get(defenderIndex));
			
			if(getCurrentState().getCombatHex().getFightingThingsInHexNotOwnedByPlayers(getCurrentState().getPlayers()).size()>0)
			{
				getCurrentState().setCurrentCombatPhase(CombatPhase.DEFENDER_RETREAT);
				advanceOrEnd();
				for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
				{
					if(!tp.isFaceUp() && !tp.isSpecialCharacter())
					{
						tp.flip();
					}
				}
				new InitiateCombat(getCurrentState().getCombatHex(), getCurrentState().getPlayersStillFightingInCombatHex(), getCurrentState().getDefendingPlayerNumber(), getCurrentState().getPlayerOrder(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(getCurrentState().getPlayersInCombatIDMask());
			}
			else
			{
				getCurrentState().setCurrentCombatPhase(CombatPhase.DETERMINE_DEFENDERS);
				getCurrentState().addNeededRoll(new Roll(1,getCurrentState().getCombatHex().getHex(),RollReason.EXPLORE_HEX,playerNumber));
			}
			
			new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(playerNumber);
		}
		else
		{
			for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
			{
				if(!tp.isFaceUp() && !tp.isSpecialCharacter())
				{
					tp.flip();
				}
			}
			getCurrentState().setDefendingPlayerNumber(defender.getID());
			getCurrentState().setCurrentCombatPhase(CombatPhase.SELECT_TARGET_PLAYER);
			if(!needToChooseTargets(hex))
			{
				autoDetermineTargets();
				advanceToNextCombatPhase();
			}
			
			new InitiateCombat(getCurrentState().getCombatHex(), getCurrentState().getPlayersStillFightingInCombatHex(), getCurrentState().getDefendingPlayerNumber(), getCurrentState().getPlayerOrder(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(getCurrentState().getPlayersInCombatIDMask());
		}
	}

	private void makeHitsApplied(ITileProperties thing, int playerNumber, int hitCount)
	{
		if(thing.hasAbility(Ability.Armor))
		{
			thing.setValue(thing.getValue() - hitCount);
		}
		
		if(thing.getValue() == 0 || !thing.hasAbility(Ability.Armor))
		{
			if(thing.isBuilding())
			{
				//buildings are now neutralized
				thing.flip();
			}
			else
			{
				removePlayerThingFromBoard(playerNumber, getCurrentState().getCombatHex().getHex(), thing);
			}
		}
		HexStatesChanged notification = new HexStatesChanged(1);
		notification.getArray()[0] = getCurrentState().getCombatHex();
		notification.postNetworkEvent(Constants.ALL_PLAYERS_ID);
		
		getCurrentState().removeHitsFromPlayer(playerNumber, hitCount);

		new CombatHits(playerNumber,getCurrentState().getHitsOnPlayer(playerNumber)).postNetworkEvent(getCurrentState().getPlayersInCombatIDMask());
		
		boolean someoneNeedsToApplyHits = false;
		for(Player p : getCurrentState().getPlayers())
		{
			if(getCurrentState().getHitsOnPlayer(p.getID())>0 && !getCurrentState().getPlayersStillFightingInCombatHex().contains(p))
			{
				getCurrentState().removeHitsFromPlayer(p.getID(), getCurrentState().getHitsOnPlayer(p.getID()));
			}
			else if(getCurrentState().getHitsOnPlayer(p.getID())>0)
			{
				someoneNeedsToApplyHits = true;
			}
		}
		
		if(!someoneNeedsToApplyHits)
		{
			advanceOrEnd();
		}
	}
	
	private void makeDefenderBribed(ITileProperties defender, int playerNumber)
	{
		boolean highAmount = false;
		
		for(ITileProperties thing : getCurrentState().getCombatHex().getThingsInHex())
		{
			if(thing.isTreasure() || thing.isSpecialIncomeCounter() || thing.isMagicItem())
			{
				highAmount = true;
				break;
			}
		}
		
		int multiplier = highAmount? 2 : 1;
		int goldAmount = multiplier * defender.getValue();
		
		getCurrentState().getPlayerByPlayerNumber(playerNumber).removeGold(goldAmount);

		if(defender.isCreature())
		{
			getCurrentState().getCup().reInsertTile(defender);
			getCurrentState().getCombatHex().removeThingFromHex(defender);
		}
		else
		{
			defender.setValue(0);
			defender.flip();
			//neutralized
		}
		if(getCurrentState().getPlayersStillFightingInCombatHex().size() == 1)
		{
			givePlayerExplorationHex(playerNumber);
		}
		
		new PlayersList(getCurrentState().getPlayers()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
		HexStatesChanged msg = new HexStatesChanged(1);
		msg.getArray()[0] = getCurrentState().getCombatHex();
		msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
	}
	
	private void advanceOrEnd()
	{
		if(getCurrentState().getPlayersStillFightingInCombatHex().size()>1)
		{
			advanceToNextCombatPhase();
		}
		else
		{
			getCurrentState().removeAllHitsFromAllPlayers();
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
			if(newOwner != null && !newOwner.equals(oldOwner))
			{
				if(oldOwner!=null)
				{
					oldOwner.removeHexFromOwnership(getCurrentState().getCombatHex().getHex());
				}
				newOwner.addOwnedHex(getCurrentState().getCombatHex().getHex());
				if(getCurrentState().getCombatHex().hasSpecialIncomeCounter())
				{
					if(oldOwner!=null)
					{
						oldOwner.removeOwnedThingOnBoard(getCurrentState().getCombatHex().getSpecialIncomeCounter());
					}
					newOwner.addOwnedThingOnBoard(getCurrentState().getCombatHex().getSpecialIncomeCounter());
				}
				if(getCurrentState().getCombatHex().hasBuilding())
				{
					if(oldOwner!=null)
					{
						oldOwner.removeOwnedThingOnBoard(getCurrentState().getCombatHex().getBuilding());
					}
					newOwner.addOwnedThingOnBoard(getCurrentState().getCombatHex().getBuilding());
					if(getCurrentState().getCombatHex().getBuilding().getName().equals(Building.Citadel.name()))
					{
						getCurrentState().addHexToListOfConstructedHexes(getCurrentState().getCombatHex());
					}
				}
				boolean wasExploration = false;
				for(ITileProperties thing : getCurrentState().getCombatHex().getThingsInHex())
				{
					if(!thing.isCreature() || !thing.isSpecialIncomeCounter() || !thing.isBuilding())
					{
						wasExploration = true;
						break;
					}
				}
				if(wasExploration)
				{
					givePlayerExplorationHex(newOwner.getID());
				}
				getCurrentState().getCombatHex().setMarker(Constants.getPlayerMarker(newOwner.getID()));
				HexStatesChanged msg = new HexStatesChanged(1);
				msg.getArray()[0] = getCurrentState().getCombatHex();
				msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
			}
			else
			{
				if(oldOwner==null)
				{
					getCurrentState().getCombatHex().removeMarker();
				}
				else
				{
					getCurrentState().getCombatHex().setMarker(Constants.getPlayerMarker(oldOwner.getID()));
				}
				HexStatesChanged msg = new HexStatesChanged(1);
				msg.getArray()[0] = getCurrentState().getCombatHex();
				msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
			}
			if(oldOwner==null && newOwner==null)
			{
				getCurrentState().setCurrentCombatPhase(CombatPhase.NO_COMBAT);
			}
			else
			{
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
				if(getCurrentState().isWaitingForRolls())
				{
					getCurrentState().setCurrentCombatPhase(CombatPhase.DETERMINE_DAMAGE);
				}
				else
				{
					getCurrentState().setCurrentCombatPhase(CombatPhase.PLACE_THINGS);
				}
			}
			new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
		}
	}
	
	private void makePlayersTarget(int playerNumber, int targetPlayerNumber)
	{
		getCurrentState().setPlayersTarget(playerNumber, targetPlayerNumber);
		
		new PlayerTargetChanged(getCurrentState().getPlayerByPlayerNumber(playerNumber), getCurrentState().getPlayerByPlayerNumber(targetPlayerNumber)).postNetworkEvent(Constants.ALL_PLAYERS_ID);
		boolean someoneNeedsToSelectTarget = false;
		for(Player p : getCurrentState().getPlayersStillFightingInCombatHex())
		{
			if(getCurrentState().getPlayersTarget(p.getID()) == null)
			{
				someoneNeedsToSelectTarget = true;
				break;
			}
		}
		if(!someoneNeedsToSelectTarget)
		{
			advanceToNextCombatPhase();
		}
	}
	
	private void playerWaivedRetreat(int playerNumber)
	{
		advanceToNextCombatPhase();
	}
	
	private void playerWaivedBribe(int playerNumber)
	{
		getCurrentState().setCurrentCombatPhase(CombatPhase.DEFENDER_RETREAT);
		advanceToNextCombatPhase();

		for(ITileProperties tp : getCurrentState().getCombatHex().getFightingThingsInHex())
		{
			if(!tp.isFaceUp() && !tp.isSpecialCharacter())
			{
				tp.flip();
			}
		}
		new InitiateCombat(getCurrentState().getCombatHex(), getCurrentState().getPlayersStillFightingInCombatHex(), getCurrentState().getDefendingPlayerNumber(), getCurrentState().getPlayerOrder(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(getCurrentState().getPlayersInCombatIDMask());
	}

	private void retreatFromHex(int playerNumber, ITileProperties destinationHex)
	{
		Player coward = getCurrentState().getPlayerByPlayerNumber(playerNumber);
		Set<ITileProperties> runningCreatures = getCurrentState().getCombatHex().getThingsInHexOwnedByPlayer(coward);
		for(ITileProperties thing : runningCreatures)
		{
			if(thing.isCreature())
			{
				getCurrentState().getCombatHex().removeThingFromHex(thing);
				getCurrentState().getBoard().getHexStateForHex(destinationHex).addThingToHex(thing);
			}
		}
		
		int creatureCount = 0;
		for(ITileProperties thing : getCurrentState().getBoard().getHexStateForHex(destinationHex).getThingsInHexOwnedByPlayer(coward))
		{
			if(thing.isCreature())
			{
				creatureCount++;
			}
		}
		if(creatureCount>Constants.MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX && !(getCurrentState().getBoard().getHexStateForHex(destinationHex).hasBuilding() && getCurrentState().getBoard().getHexStateForHex(destinationHex).getBuilding().getName().equals(Building.Citadel)))
		{
			getCurrentState().addHexThatNeedsThingsRemoved(getCurrentState().getBoard().getHexStateForHex(destinationHex), creatureCount - Constants.MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX);
			new HexNeedsThingsRemoved(getCurrentState().getBoard().getHexStateForHex(destinationHex), creatureCount - Constants.MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX, true,coward).postNetworkEvent(playerNumber);
		}
		else
		{
			advanceOrEnd();
		}
		HexStatesChanged evt = new HexStatesChanged(2);
		evt.getArray()[0] = getCurrentState().getCombatHex();
		evt.getArray()[1] = getCurrentState().getBoard().getHexStateForHex(destinationHex);
		evt.postNetworkEvent(Constants.ALL_PLAYERS_ID);
	}
	
	private void removeThingsFromHex(int playerNumber, ITileProperties hex, Set<ITileProperties> things)
	{
		switch(getCurrentState().getCurrentCombatPhase())
		{

			case ATTACKER_ONE_RETREAT:
			case ATTACKER_TWO_RETREAT:
			case ATTACKER_THREE_RETREAT:
			case DEFENDER_RETREAT:
			{
				for(ITileProperties thing : things)
				{
					removePlayerThingFromBoard(playerNumber,hex,thing);
				}
				HexState hs = getCurrentState().getBoard().getHexStateForHex(hex);
				
				HexStatesChanged msg = new HexStatesChanged(1);
				msg.getArray()[0] = hs;
				msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
				
				getCurrentState().updateHexThatNeedsThingsRemoved(hs, getCurrentState().getThingsToRemoveFromHex(hs) - things.size());
				new HexNeedsThingsRemoved(hs, getCurrentState().getThingsToRemoveFromHex(hs), false,getCurrentState().getPlayerByPlayerNumber(playerNumber)).postNetworkEvent(playerNumber);
				if(!getCurrentState().hasHexesThatNeedThingsRemoved())
				{
					advanceOrEnd();
				}
				break;
			}
			default:
				break;
		}
	}

	private void advanceToNextCombatPhase()
	{
		advanceToNextCombatPhaseHelper();
		new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
	}
	
	private void advanceToNextCombatPhaseHelper()
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
					boolean owned = false;
					for(Player p : getCurrentState().getPlayers())
					{
						if(p.ownsThingOnBoard(thing))
						{
							owned = true;
							if(getCurrentState().getPlayersStillFightingInCombatHex().contains(getCurrentState().getPlayersTarget(p.getID())))
							{
								int diceCount = thing.isSpecialCreatureWithAbility(Ability.Charge)? 2 : 1;
								getCurrentState().addNeededRoll(new Roll(diceCount,thing,RollReason.ATTACK_WITH_CREATURE,p.getID()));
							}
						}
					}
					if(!owned)
					{
						int diceCount = thing.isSpecialCreatureWithAbility(Ability.Charge)? 2 : 1;
						getCurrentState().addNeededRoll(new Roll(diceCount,thing,RollReason.ATTACK_WITH_CREATURE,getCurrentState().getDefendingPlayerNumber()));
					}
				}
			}
			if(!getCurrentState().isWaitingForRolls())
			{
				getCurrentState().setCurrentCombatPhase(getCombatPhaseByOrdinal(nextPhase.ordinal() + 1));
				advanceToNextCombatPhaseHelper();
			}
		}
		else if(nextPhase == CombatPhase.ATTACKER_ONE_RETREAT)
		{
			if(getCurrentState().getAttackerByIndex(1) == null)
			{
				advanceToNextCombatPhaseHelper();
			}
		}
		else if(nextPhase == CombatPhase.ATTACKER_TWO_RETREAT)
		{
			if(getCurrentState().getAttackerByIndex(2) == null)
			{
				advanceToNextCombatPhaseHelper();
			}
		}
		else if(nextPhase == CombatPhase.ATTACKER_THREE_RETREAT)
		{
			if(getCurrentState().getAttackerByIndex(3) == null)
			{
				advanceToNextCombatPhaseHelper();
			}
		}
		else if((nextPhase == CombatPhase.DEFENDER_RETREAT && 
					(!getCurrentState().getPlayersStillFightingInCombatHex().contains(getCurrentState().getDefendingPlayer()) || 
					getCurrentState().getCombatHex().getFightingThingsInHexNotOwnedByPlayers(getCurrentState().getPlayers()).size()>0)) 
				|| nextPhase == CombatPhase.DETERMINE_DAMAGE)
		{
			getCurrentState().clearAllPlayerTargets();
			if(needToChooseTargets(getCurrentState().getCombatHex().getHex()))
			{
				//wrap around to target selection
				getCurrentState().setCurrentCombatPhase(CombatPhase.SELECT_TARGET_PLAYER);
			}
			else
			{
				autoDetermineTargets();
				//wrap around to magic attack
				getCurrentState().setCurrentCombatPhase(getCombatPhaseByOrdinal(CombatPhase.MAGIC_ATTACK.ordinal()-1));
				advanceToNextCombatPhaseHelper();
			}
		}
	}
	
	private boolean needToChooseTargets(ITileProperties combatHex)
	{
		HashSet<Integer> playersStillFighting = new HashSet<>();
		for(ITileProperties thing : getCurrentState().getBoard().getHexStateForHex(combatHex).getFightingThingsInHex())
		{
			for(Player p : getCurrentState().getPlayers())
			{
				if(p.ownsThingOnBoard(thing))
				{
					playersStillFighting.add(p.getID());
				}
			}
		}
		return playersStillFighting.size() >= 3;
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
	
	private void autoDetermineTargets()
	{
		Player p1 = null;
		Player p2 = null;
		Iterator<Player> it = getCurrentState().getPlayersStillFightingInCombatHex().iterator();
		p1 = it.next();
		p2 = it.next();
		getCurrentState().setPlayersTarget(p1.getID(), p2.getID());
		getCurrentState().setPlayersTarget(p2.getID(), p1.getID());
		

		new PlayerTargetChanged(p1, p2).postNetworkEvent(Constants.ALL_PLAYERS_ID);
		new PlayerTargetChanged(p2, p1).postNetworkEvent(Constants.ALL_PLAYERS_ID);
	}

	private void applyRollEffects() throws NoMoreTilesException
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
						getCurrentState().addHitsToPlayer(getCurrentState().getPlayersTarget(rollingPlayerID).getID(), hitCount);
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
						removePlayerThingFromBoard(owningPlayer.getID(), getCurrentState().getCombatHex().getHex(), rollTarget);
						if(!rollTarget.getName().equals(Building.Tower.name()) && rollTarget.isBuildableBuilding())
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
					HexStatesChanged msg = new HexStatesChanged(1);
					msg.getArray()[0] = getCurrentState().getCombatHex();
					msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
					break;
				}
				case EXPLORE_HEX:
				{
					handledRolls.add(r);
					int roll_value = r.getFinalTotal();
					
					if(roll_value == 1 || roll_value == 6) {
						//give hex to player
						makeHexOwnedByPlayer(r.getRollTarget(), r.getRollingPlayerID());
						getCurrentState().setCurrentCombatPhase(CombatPhase.PLACE_THINGS);
						new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
					} else {
						List<ITileProperties> listOfThings = new ArrayList<>(roll_value);
						List<ITileProperties> listOfSpecialIncomeCounters = new ArrayList<>();
						ITileProperties nextTile;
						for (int i = 0; i < roll_value; i++) {
							nextTile = getCurrentState().getCup().drawTile();
							if (nextTile.isSpecialIncomeCounter()) {
								if (!nextTile.isBuilding() && nextTile.getBiomeRestriction() != getCurrentState().getCombatHex().getHex().getBiomeRestriction()) {
									// returns special income counter to the cup
									getCurrentState().getCup().reInsertTile(nextTile);
								} else {
									// adds special income counter to a list of special income counters 
									// that are cities, villages, or keyed to hex terrain
									listOfSpecialIncomeCounters.add(nextTile);
								}
							} else if (nextTile.isEvent()) {
								// returns random event to the cup immediately
								getCurrentState().getCup().reInsertTile(nextTile);
							} else {
								listOfThings.add(nextTile);
							}
						}
						ITileProperties highestValueCounter = null;
						for(ITileProperties thing : listOfSpecialIncomeCounters)
						{
							if(highestValueCounter == null || highestValueCounter.getValue() < thing.getValue())
							{
								highestValueCounter = thing;
							}
						}
						listOfSpecialIncomeCounters.remove(highestValueCounter);
						for(ITileProperties thing : listOfSpecialIncomeCounters)
						{
							getCurrentState().getCup().reInsertTile(thing);
						}
						
						if(highestValueCounter!=null)
						{
							getCurrentState().getCombatHex().addThingToHexForExploration(highestValueCounter);
						}
						boolean defendingCreaturesExist = false;
						
						for (ITileProperties thing : listOfThings) {
							if (thing.isCreature()) {
								defendingCreaturesExist = true;
							}
							getCurrentState().getCombatHex().addThingToHexForExploration(thing);
						}

						if(listOfThings.size()>0)
						{
							new ExplorationResults(getCurrentState().getCombatHex(), getCurrentState().getPlayerByPlayerNumber(r.getRollingPlayerID())).postNetworkEvent(r.getRollingPlayerID());
						}
						HexStatesChanged msg = new HexStatesChanged(1);
						msg.getArray()[0] = getCurrentState().getCombatHex();
						msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
						
						if (defendingCreaturesExist) {
							int rollingPlayerIndex = -1;
							for (int i = 0; i < getCurrentState().getPlayers().size(); i++) {
								if (getCurrentState().getPlayerOrder().get(i) == r.getRollingPlayerID()) {
									rollingPlayerIndex = i;
									break;
								}
							}
							//sets defending player to the person that moves before the explorer
							if (rollingPlayerIndex == 0) {
								getCurrentState().setDefendingPlayerNumber(getCurrentState().getPlayerOrder().get(getCurrentState().getPlayerOrder().size()-1));
							} else {
								getCurrentState().setDefendingPlayerNumber(getCurrentState().getPlayerOrder().get(rollingPlayerIndex-1));
							}
							
							getCurrentState().setCurrentCombatPhase(CombatPhase.BRIBE_CREATURES);
							new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(r.getRollingPlayerID());
						}
						else
						{
							givePlayerExplorationHex(r.getRollingPlayerID());
						}
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
			else
			{
				new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(Constants.ALL_PLAYERS_ID);

				for(Player p : getCurrentState().getPlayersStillFightingInCombatHex())
				{
					int hitsOnPlayer = getCurrentState().getHitsOnPlayer(p.getID());
					if(hitsOnPlayer>0)
					{
						//notifies players of hits
						new CombatHits(p.getID(),hitsOnPlayer).postNetworkEvent(getCurrentState().getPlayersInCombatIDMask());
					}
				}
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
			new CurrentPhase<CombatPhase>(getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentCombatPhase()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
		}
		for(Roll r : handledRolls)
		{
			getCurrentState().removeRoll(r);
		}
	}
	
	private void givePlayerExplorationHex(int playerID)
	{
		ArrayList<ITileProperties> thingsToRemove = new ArrayList<>();
		for(ITileProperties thing : getCurrentState().getCombatHex().getThingsInHex())
		{
			if(!thing.isCreature() && !thing.isBuilding() && !thing.isSpecialIncomeCounter())
			{
				getCurrentState().getPlayerByPlayerNumber(playerID).addThingToTrayOrHand(thing);
				thingsToRemove.add(thing);
			}
			else if(thing.isBuilding() || thing.isSpecialIncomeCounter())
			{
				getCurrentState().getPlayerByPlayerNumber(playerID).addOwnedThingOnBoard(thing);
			}
		}
		for(ITileProperties thing : thingsToRemove)
		{
			getCurrentState().getCombatHex().removeThingFromHex(thing);
		}
		
		makeHexOwnedByPlayer(getCurrentState().getCombatHex().getHex(),playerID);
		getCurrentState().setCurrentCombatPhase(CombatPhase.PLACE_THINGS);
		super.notifyClientsOfPlayerTray(playerID);
	}
	
	@Subscribe
	public void playerRemovedThingsFromBoard(PlayerRemovedThingsFromHex event)
	{
		try
		{
			removeThingsFromHex(event.getID(),event.getHex(),event.getThingsToRemove());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process PlayerRemovedThingsFromHex event due to: ", t);
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
	public void receivePlayerWaivedBribe(PlayerWaivedBribe event)
	{
		if(event.isUnhandled())
		{
			try
			{
				playerWaivedBribe(event.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process PlayerWaivedBribe due to: ", t);
			}
		}
	}
	
	@Subscribe
	public void receiveApplyRoll(DiceRolled event)
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
	public void receiveRetreatCommand(RetreatCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				retreatFromCombat(command.getID(),command.getDestinationHex());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process RetreatCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.Retreat).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
	
	@Subscribe
	public void receiveTargetPlayerCommand(TargetPlayerCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				setPlayersTarget(command.getID(),command.getTargetID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process TargetPlayerCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.TargetPlayer).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
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
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.ApplyHit).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}

	@Subscribe
	public void receiveBribeDefenderCommand(BribeDefenderCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				bribeDefender(command.getDefender(), command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process BribeDefenderCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.BribeCreature).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
	
	@Subscribe
	public void receiveResolveCombatCommand(ResolveCombatCommand command)
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
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.InitiateCombat).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
}

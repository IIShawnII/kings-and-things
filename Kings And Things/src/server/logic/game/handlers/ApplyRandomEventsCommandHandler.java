package server.logic.game.handlers;

import java.util.ArrayList;

import server.event.DiceRolled;
import server.event.internal.ApplyRandomEventsCommand;
import server.event.internal.ConstructBuildingCommand;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.validators.ApplyRandomEventsValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants.Biome;
import common.Constants.BuildableBuilding;
import common.Constants.RandomEvent;
import common.Constants.RollReason;
import common.Constants.UpdateInstruction;
import common.Constants;
import common.Logger;
import common.event.network.CommandRejected;
import common.event.network.HandPlacement;
import common.event.network.PlayersList;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.Roll;

public class ApplyRandomEventsCommandHandler extends CommandHandler {
	
	public void applyRandomEventEffect (ITileProperties randomEventTile, ITileProperties targetOfEvent, int playerID) {
		ApplyRandomEventsValidator.validateCanPlayRandomEvent(playerID, randomEventTile, targetOfEvent, getCurrentState());
		
		String randomEventName = randomEventTile.getName();
		RandomEvent evt = RandomEvent.valueOf(randomEventName);
		Player playerApplyingRandomEvent = getCurrentState().getPlayerByPlayerNumber(playerID);
		
		switch (evt) {
			case Big_Juju:
				/**
				 * Changes terrain type of any hex within range of your magic-using creature
				 */
				//TODO - need checks to see if target is current player's hex or enemy player's hex
				//this.getCurrentState().getBoard().getHexStateForHex(targetOfEvent).setHex(hex);
				/*
				 * HexState targetHex;
				 * 
				 * for (creature : playerApplyingRandomEvent.listOfCreatures) {
				 * 		if (creature is magic creature && targetHex < creature.combatValue) {
				 * 			applyBigJuju(targetHex);
				 * 	 	}
				 * }
				 */
				break;
			case Dark_Plague:
				/**
				 * All players lose counters equal to combat value of forts, cities and villages
				 * in each hex. Player can satisfy losses with self-same forts, cities and
				 * villages, but are not required to.
				 */
				int sumOfCombatValue = 0;
				//
				for (HexState hex: this.getCurrentState().getBoard().getHexesAsList()) {
					for (Player playerAffectedByBigJuju: this.getCurrentState().getPlayers()) {
						if (playerAffectedByBigJuju.ownsHex(hex.getHex())) {
							for (ITileProperties thingInHex: hex.getThingsInHexOwnedByPlayer(playerAffectedByBigJuju)) 
							{
								if (thingInHex.isBuilding()) {
									sumOfCombatValue += thingInHex.getValue();
								}
							}
							this.getCurrentState().addHexThatNeedsThingsRemoved(hex, sumOfCombatValue);
						}
					}
					sumOfCombatValue = 0;	// resets combat value
				}
				break;
			case Defection:
				/**
				 * Roll to obtain a special character from an unused pool or another player 
				 */
				if (getCurrentState().isOwnedByPlayer(targetOfEvent)) {
					Player owningPlayer = getCurrentState().getOwningPlayer(targetOfEvent);
					// Player that applies event rolls
					getCurrentState().addNeededRoll(new Roll(2, targetOfEvent, RollReason.DEFECTION_USER, playerApplyingRandomEvent.getID()));
					// Player that owns special character rolls
					getCurrentState().addNeededRoll(new Roll(2, targetOfEvent, RollReason.DEFECTION_DEFENDER, owningPlayer.getID()));
					
				} else {
					/*TODO Check which player is the defender*/
				}
				break;
			case Good_Harvest:
				/**
				 * Player collects gold except from special income counters
				 */
				//Apply gold collection only for the player applying Good_Harvest
				playerApplyingRandomEvent.removeThingFromTray(randomEventTile);
				getCurrentState().getCup().reInsertTile(randomEventTile);
				playerApplyingRandomEvent.addGold(playerApplyingRandomEvent.getSpecialEventIncome());
				new PlayersList(getCurrentState().getPlayers()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
				notifyClientsOfPlayerTray(playerID);
				break;
			case Mother_Lode:
				/**
				 * Player collects double from all special income counters. Mines are quadrupled
				 * if player owns Dwarf King.
				 */
				boolean ownsDwarfKing = false;
				ArrayList<ITileProperties> specialIncomeCounters = new ArrayList<>();
				
				// checks if player owns Dwarf King and has special income counter
				for (ITileProperties thing : playerApplyingRandomEvent.getOwnedThingsOnBoard()) {
					if (thing.getName().equals("Dwarf_King")) {
						ownsDwarfKing = true;
					}
					if (thing.isSpecialIncomeCounter()) {
						specialIncomeCounters.add(thing);
					}
				}
				
				// Goes through income counters
				for (ITileProperties specialIncomeCounter : specialIncomeCounters) {
					if (specialIncomeCounter.getBiomeRestriction() == Biome.Mountain && ownsDwarfKing) {
						// If player owns mines, player gets gold worth quadruple the amount of special income counter
						playerApplyingRandomEvent.addGold(4*specialIncomeCounter.getValue());
					} else {
						// Player gets gold worth double the amount of special income counter
						playerApplyingRandomEvent.addGold(2*specialIncomeCounter.getValue());
					}
				}
				break;
			case Teenie_Pox:
				/**
				 * One player may lose 2 - 5 counters from his/her largest stack. Forts, cities
				 * and villages must be reduced if necessary to meet losses.
				 */
				/*
				 * Player chooses a player to apply Teenie_Pox on
				 * max_combatValue_hex = 0
				 * if (player.rolls > 1 or player.rolls < 6) {
				 * 		for (hex : player(Teenie_Pox).hexes) {
				 * 			if (player(Teenie_Pox).hexes > max_combatValue_hex) {
				 * 				keep track of hex with max combat value
				 * 			}
				 *      }
				 *      applyTeeniePox(opposingPlayerHexWithMaxCombatValue)
				 * }
				 */
				break;
			case Terrain_Disaster:
				/**
				 * One hex loses 2 - 5 counters. Forts, cities, and villages must be reduced if
				 * necessary to meet losses.
				 */
				/*
				 * Player chooses any hex on the board
				 * player rolls 2 die
				 * do {
				 * 		if (player.rolls >= 6 || player.rolls <= 8) {
				 * 			applyTeeniePox(chosenHex)
				 * 			break
				 * 		}
				 * 		Player chooses another hex	
				 * 		Player rolls 2 die			 * 		
				 * } while (player.rolls < 6 || player.rolls > 8)
				 */
				// Current code in progress
				/*
				do {
					// hex is chosen
					this.getCurrentState().getBoard().getHexStateForHex(targetOfEvent);
					// player rolls
					getCurrentState().addNeededRoll(new Roll(2, targetOfEvent, RollReason.DEFECTION_USER, playerApplyingRandomEvent.getID()));
					if () {
						targetOfEvent.get
						if (targetOfEvent.is) {
							
						}
					}
				} while ();
				break;
				*/
			case Vandalism:
				/**
				 * One player loses a fort level (citadels are immune).
				 */
				/*
				 * Player chooses opposing player
				 * Opposing player must choose a hex containing tower, castle, or keep
				 * Tower must be eliminated, castle or keep must be reduced
				 */
				break;
			case Weather_Control:
				/**
				 * Place or move Black Cloud; all friendly counters under Cloud reduce Combat value by one.
				 */
				/*
				 * for (creature : playerApplyingRandomEvent.listOfCreatures) {
				 * 		if (creature is magic creature) {
				 * 			if (targetHex.isOwned()) {
				 * 				for (counter : OwningPlayer.counters) {
				 * 						counter.combatValue()--;
				 * 				}
				 * 			}
				 * 		}
				 * }
				 */
				break;
			case Willing_Workers:
				/**
				 * Gain one additional fort level.
				 */
				HexState targetHex = getCurrentState().getBoard().getHexStateForHex(targetOfEvent);
				 if (!targetHex.hasBuilding()) {
				 	new ConstructBuildingCommand(BuildableBuilding.Tower, targetOfEvent).postInternalEvent(playerID);
				 }
				 else if (targetHex.getBuilding().getName().equals(BuildableBuilding.Tower.name())) {
					new ConstructBuildingCommand(BuildableBuilding.Keep, targetOfEvent).postInternalEvent(playerID);
				 } 
				 else if (targetHex.getBuilding().getName().equals(BuildableBuilding.Keep.name())) {
					 new ConstructBuildingCommand(BuildableBuilding.Castle, targetOfEvent).postInternalEvent(playerID);
				 }
				break;
		}
	}
	
	// 
	private void applyRoll() throws NoMoreTilesException {
		Roll defectionUser = null;
		Roll defectionDefender = null;
		
		for (Roll r : getCurrentState().getFinishedRolls()) {
			if (r.getRollReason() == RollReason.DEFECTION_USER) {
				defectionUser = r;
			}
					
			if (r.getRollReason() == RollReason.DEFECTION_DEFENDER) {
				defectionDefender = r;
			}
		}
		
		if (defectionUser != null && defectionDefender != null) {
			if (defectionUser.getFinalTotal() > defectionDefender.getFinalTotal()) {
				// Removes special character from defending player or bank
				if (getCurrentState().isOwnedByPlayer(defectionUser.getRollTarget())) {
					// Removes special character from defending player
					getCurrentState().getPlayerByPlayerNumber(defectionDefender.getRollingPlayerID()).removeOwnedThingOnBoard(defectionDefender.getRollTarget());
				} else {
					// Removes special character from bank
					getCurrentState().getBankHeroes().drawTileByName(defectionUser.getRollTarget().getName());
				}
				
				// Adds special character to player's hand
				getCurrentState().getPlayerByPlayerNumber(defectionUser.getRollingPlayerID()).addCardToHand(defectionUser.getRollTarget());
				new HandPlacement(getCurrentState().getPlayerByPlayerNumber(defectionUser.getRollingPlayerID()).getCardsInHand()).postNetworkEvent(defectionUser.getRollingPlayerID());
			}
			// Removes handled rolls for player applying DEFECTION
			getCurrentState().removeRoll(defectionUser);
			// Removes handled rolls for player being affected DEFECTION
			getCurrentState().removeRoll(defectionDefender);
		}
	}
	
	private void applyBigJuju(HexState targetHex) {
		
	}
	
	@Subscribe
	public void dieRolled (DiceRolled roll) {
		try {
			applyRoll();
		} catch (Throwable t) {
			Logger.getErrorLogger().error("Unable to apply random event due to: ",t);
			new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.NeedRoll).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
		}
	}
	
	@Subscribe
	public void receiveApplyEventsCommand (ApplyRandomEventsCommand randomEvent) {
		try {
			applyRandomEventEffect(randomEvent.getEventOfPlayer(), randomEvent.getTargetOfEvent(), randomEvent.getID());
		} catch (Throwable t) {
			Logger.getErrorLogger().error("Unable to apply random event due to: ",t);
			new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.RandomEvent).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
		}
	}
}

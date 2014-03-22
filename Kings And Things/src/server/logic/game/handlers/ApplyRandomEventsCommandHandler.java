package server.logic.game.handlers;

import server.event.internal.ApplyRandomEventsCommand;
import server.logic.game.Player;

import com.google.common.eventbus.Subscribe;

import common.Constants.RandomEvent;
import common.Logger;
import common.game.HexState;
import common.game.ITileProperties;

public class ApplyRandomEventsCommandHandler extends CommandHandler {
	
	public void applyRandomEventEffect (ITileProperties randomEventTile, ITileProperties targetOfEvent) {
		
		String randomEventName = randomEventTile.getName();
		RandomEvent evt = RandomEvent.valueOf(randomEventName);
		
		switch (evt) {
			case Big_Juju:
				/**
				 * Changes terrain type of any hex within range of your magic-using creature
				 */
				//TODO - need checks to see if target is current player's hex or enemy player's hex
				//this.getCurrentState().getBoard().getHexStateForHex(targetOfEvent).setHex(hex);
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
					for (Player p: this.getCurrentState().getPlayers()) {
						if (p.ownsHex(hex.getHex())) {
							for (ITileProperties thingInHex: hex.getThingsInHexOwnedByPlayer(p)) 
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
				/*
				 * if (player owns special character) {
				 *		player and owner of special character roll twice
				 *		if (player's roll > owner's roll) {
				 *			player places special character in any hex player controls
				 *		}
				 *} else {
				 *		player collects special character
				 *}			 
				*/
				break;
			case Good_Harvest:
				/**
				 * Player collects gold except from special income counters
				 */
				//Apply gold collection only for the player applying Good_Harvest
				break;
			case Mother_Lode:
				/**
				 * Player collects double from all special income counters. Mines are quadrupled
				 * if player owns Dwarf King.
				 */
				/*
				 * player collects gold pieces (2*player's special income counters)
				 * if (player owns Dwarf_King) {
				 * 		mines are quadrupled
				 * }
				 */
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
				break;
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
				 * 
				 */
				break;
			case Willing_Workers:
				/**
				 * Gain one additional fort level.
				 */
				/*
				 * Player chooses hex he owns
				 * if (no bulding in hex) {
				 * 		build tower
				 * }
				 * else if (tower in hex) {
				 * 		upgrade to keep
				 * } 
				 * else if (keep in hex) {
				 * 		upgrade to castle
				 * }
				 */
				break;
		}
	}
	
	
	@Subscribe
	public void receiveApplyEventsCommand (ApplyRandomEventsCommand randomEvent) {
		try {
			applyRandomEventEffect(randomEvent.getEventOfPlayer(), randomEvent.getTargetOfEvent());
		} catch (Throwable t) {
			Logger.getErrorLogger().error("Unable to apply random event due to: ");
		}
	}
}
package server.logic.game.handlers;

import server.event.internal.ApplyRandomEventsCommand;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.Constants.MagicEvent;
import common.event.network.CommandRejected;
import common.game.ITileProperties;
import common.game.Player;

public class ApplyMagicEventsCommandHandler extends CommandHandler {
	
	public void applyRandomEventEffect (ITileProperties randomEventTile, ITileProperties targetOfEvent, int playerID) {
		
		String magicEventName = randomEventTile.getName();
		MagicEvent evt = MagicEvent.valueOf(magicEventName);
		Player playerApplyingMagicEvent = getCurrentState().getPlayerByPlayerNumber(playerID);
		
		switch (evt) {
			case Balloon:
				/*
				 * Flies 3 characters of combat value 3 or less up three hexes distance;
				 * Balloon and passengers fight during Ranged Combat Step
				 */
				break;
			case Bow:
				/*
				 * Creature fights during Ranged Combat Step; increases combat value by 1
				 */
				break;
			case Dispel_Magic:
				/*
				 * Enemy magic item has no effect and all magical creatures in the hex
				 * fight during Melee Combat Step
				 */
				break;
			case Dust_Of_Defense:
				/*
				 * Requires attacker to retreat without combat;
				 * only defender may play
				 */
				break;
			case Fan:
				/*
				 * Cancels Dust; moves Balloon or Cloud
				 */
				break;
			case Firewall:
				/*
				 * Creates a magic fort. Determine combat value by rolling one die
				 */
				break;
			case Golem:
				/*
				 * Takes one ranged hit per round without being eliminated
				 */
				break;
			case Lucky_Charm:
				/*
				 * Allows you to increase or decrease any die roll by one
				 */
				break;
			case Elixir:
				/*
				 * Cancels effects of Plague or Teeniepox
				 */
				break;
			case Sword:
				/*
				 * Creature fights during Melee Combat Step as a 
				 * charging creature (rolls two dice); increases combat value by 1
				 */
				break;
			case Talisman:
				/*
				 * Gives each creature a savage throw vs. elimination until it fails.
				 */
				break;
		}
		
	}

	@Subscribe
	public void receiveApplyEventsCommand (ApplyRandomEventsCommand magicEvent) {
		try {
			applyRandomEventEffect(magicEvent.getEventOfPlayer(), magicEvent.getTargetOfEvent(), magicEvent.getID());
		} catch (Throwable t) {
			Logger.getErrorLogger().error("Unable to apply random event due to: ",t);
			new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),null).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
		}
	}
	
}

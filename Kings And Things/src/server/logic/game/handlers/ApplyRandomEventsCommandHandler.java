package server.logic.game.handlers;

import server.event.commands.ApplyRandomEventsCommand;
import server.logic.game.Player;

import com.google.common.eventbus.Subscribe;

import common.Constants.RandomEvent;
import common.Logger;
import common.game.ITileProperties;

public class ApplyRandomEventsCommandHandler extends CommandHandler {
	
	public void applyRandomEventEffect (ITileProperties randomEventTile, ITileProperties targetOfEvent) {
		
		String randomEventName = randomEventTile.getName();
		RandomEvent evt = RandomEvent.valueOf(randomEventName);
		
		switch (evt) {
			case Big_Juju:
				this.getCurrentState().getBoard().getHexStateForHex(targetOfEvent).setHex(hex);
				break;
			case Dark_Plague:
				int sumOfCombatValue = 0;
				
				for (Player p: this.getCurrentState().getPlayers()) {
					for (ITileProperties thingsOnHex: p.getOwnedThingsOnBoard())
						if (thingsOnHex.isBuilding()) {
							sumOfCombatValue += thingsOnHex.getValue();
						}
				}
				break;
			case Defection:
				break;
			case Good_Harvest:
				break;
			case Mother_Lode:
				break;
			case Teenie_Pox:
				break;
			case Terrain_Disaster:
				break;
			case Vandalism:
				break;
			case Weather_Control:
				break;
			case Willing_Workers:
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

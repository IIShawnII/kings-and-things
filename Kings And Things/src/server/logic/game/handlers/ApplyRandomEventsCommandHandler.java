package server.logic.game.handlers;

import server.event.commands.ApplyRandomEventsCommand;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.game.ITileProperties;

public class ApplyRandomEventsCommandHandler extends CommandHandler {
	
	public void applyRandomEventEffect (ITileProperties randomEventTile) {
		
		String randomEventName = randomEventTile.getName();
		
		switch (randomEventName) {
		    
		}
	}
	
	
	@Subscribe
	public void receiveApplyEventsCommand (ApplyRandomEventsCommand randomEvent) {
		try {
			
		} catch (Throwable t) {
			Logger.getErrorLogger().error("Unable to apply random event due to: ");
		}
	}
}

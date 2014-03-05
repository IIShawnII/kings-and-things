package server.event.commands;

import common.event.AbstractCommand;
import common.game.ITileProperties;

public class ApplyRandomEventsCommand extends AbstractCommand {
	
	private final ITileProperties eventOfPlayer;
	
	
	// constructor
	public ApplyRandomEventsCommand (ITileProperties eventOfPlayer) {
		this.eventOfPlayer = eventOfPlayer;
	}
	
	/*Getter Methods*/
	
	// retrieves event of player
	public ITileProperties getEventOfPlayer () {
		return eventOfPlayer;
	}

}

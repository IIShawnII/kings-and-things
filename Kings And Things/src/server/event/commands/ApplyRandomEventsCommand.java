package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ApplyRandomEventsCommand extends AbstractInternalEvent {
	
	private final ITileProperties eventOfPlayer;
	
	
	// constructor
	public ApplyRandomEventsCommand (ITileProperties eventOfPlayer, final Object OWNER){
		super( OWNER);
		this.eventOfPlayer = eventOfPlayer;
	}
	
	/*Getter Methods*/
	
	// retrieves event of player
	public ITileProperties getEventOfPlayer () {
		return eventOfPlayer;
	}

}

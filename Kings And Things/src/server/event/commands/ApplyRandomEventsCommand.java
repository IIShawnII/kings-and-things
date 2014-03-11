package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ApplyRandomEventsCommand extends AbstractInternalEvent {
	
	private final ITileProperties eventOfPlayer;
	private final ITileProperties targetOfEvent;
	
	
	// constructor
	public ApplyRandomEventsCommand (ITileProperties eventOfPlayer, ITileProperties targetOfEvent, 
			final Object OWNER){
		super( OWNER);
		this.targetOfEvent = targetOfEvent;
		this.eventOfPlayer = eventOfPlayer;
	}
	
	/*Getter Methods*/
	
	// retrieves event of player
	public ITileProperties getEventOfPlayer () {
		return eventOfPlayer;
	}
	
	// retrieves target of event
	public ITileProperties getTargetOfEvent () {
		return targetOfEvent;
	}

}

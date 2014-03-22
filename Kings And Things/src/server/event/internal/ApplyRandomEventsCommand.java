package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ApplyRandomEventsCommand extends AbstractInternalEvent {
	
	private final ITileProperties eventOfPlayer;
	private final ITileProperties targetOfEvent;
	
	public ApplyRandomEventsCommand (ITileProperties eventOfPlayer, ITileProperties targetOfEvent){
		super();
		this.targetOfEvent = targetOfEvent;
		this.eventOfPlayer = eventOfPlayer;
	}
	
	/**
	 * retrieves event of player
	 */
	public ITileProperties getEventOfPlayer () {
		return eventOfPlayer;
	}
	
	/**
	 * retrieves target of event
	 */
	public ITileProperties getTargetOfEvent () {
		return targetOfEvent;
	}
}

package common.event.notifications;

import common.event.AbstractNetwrokEvent;

public class PlayerReady extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = 749685506391697600L;
	
	private String name;
	
	public PlayerReady( String playerName){
		name = playerName;
	}

	public String getName(){
		return name;
	}
}

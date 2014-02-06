package common.event.notifications;

import common.event.AbstractEvent;

public class PlayerUnReady extends AbstractEvent{
	
	private static final long serialVersionUID = -6660391742771135640L;
	
	private String name;
	
	public PlayerUnReady( String playerName){
		name = playerName;
	}

	public String getName(){
		return name;
	}
}

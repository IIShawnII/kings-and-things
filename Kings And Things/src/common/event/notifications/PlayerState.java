package common.event.notifications;

import common.event.AbstractNetwrokEvent;

public class PlayerState extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = -4607289867949344767L;
	
	private String name;
	private boolean isReady;
	
	public PlayerState( String name){
		this( name, false);
	}
	
	public PlayerState( String name, boolean isReady){
		this.name = name;
		this.isReady = isReady;
	}

	public String getName(){
		return name;
	}

	public boolean isReady(){
		return isReady;
	}

	@Override
	public String toString(){
		return "Network/PlayerReady: " + name + (isReady?" is Ready":" is Not Ready");
	}
}

package common.event;

import java.io.Serializable;

/**
 * This is the abstract super class of all events
 */
public abstract class AbstractEvent implements Serializable{
	
	private static final long serialVersionUID = -6511635541890750180L;
	
	int playerID;

	/**
	 * post this notification on command BusEvent
	 */
	public void postCommand(){
		EventDispatch.COMMAND.post( this);
	}

	/**
	 * post this notification on notification BusEvent
	 */
	public void postNotification(){
		EventDispatch.NOTIFICATION.post( this);
	}
	
	public void postNotification( int playerID){
		setPlayerID( playerID);
		postNotification();
	}
	
	public void postCommand( int playerID){
		setPlayerID( playerID);
		postCommand();
	}
	
	private void setPlayerID( int playerID){
		this.playerID = playerID;
	}
	
	public int getPlayerID(){
		return playerID;
	}
}

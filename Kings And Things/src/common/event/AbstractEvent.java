package common.event;

import common.game.PlayerInfo;

/**
 * This is the abstract super class of all events
 */
public abstract class AbstractEvent{

	protected final Object Owner;
	private int ID = -1;
	
	protected AbstractEvent( final Object OWNER){
		this.Owner = OWNER;
	}
	
	public Object getOwner(){
		return Owner;
	}

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
	
	public void postNotification( int ID){
		setID( ID);
		postNotification();
	}
	
	public void postCommand( int ID){
		setID( ID);
		postCommand();
	}
	
	protected void setID( int ID){
		this.ID = ID;
	}
	
	public int getID(){
		return ID;
	}
	
	public boolean isPublic(){
		return ID==-1;
	}
	
	public boolean isValidID( PlayerInfo player){
		return player!=null && ID==player.getID();
	}
}

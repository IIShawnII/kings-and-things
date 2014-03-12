package common.event;

import common.game.PlayerInfo;

/**
 * This is the abstract super class of all events
 */
public abstract class AbstractEvent{

	protected final Object Owner;
	private int ID = -1;
	
	protected AbstractEvent(){
		this( null);
	}
	
	protected AbstractEvent( final Object OWNER){
		this.Owner = OWNER;
	}
	
	public Object getOwner(){
		return Owner;
	}

	/**
	 * post this event on Internal BusEvent
	 */
	public void postInternalEvent(){
		EventDispatch.INTERNAL.post( this);
	}

	/**
	 * post this event on Network BusEvent
	 */
	public void postNetworkEvent(){
		EventDispatch.NETWORK.post( this);
	}
	
	public void postNetworkEvent( int ID){
		setID( ID);
		postNetworkEvent();
	}
	
	public void postInternalEvent( int ID){
		setID( ID);
		postInternalEvent();
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
		return player!=null && isValidID(player.getID());
	}
	
	public boolean isValidID( final int ID){
		return (ID&this.ID)==ID;
	}
}

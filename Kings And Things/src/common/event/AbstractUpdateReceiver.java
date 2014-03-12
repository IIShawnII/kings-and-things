package common.event;

import common.Logger;

import com.google.common.eventbus.Subscribe;

/**
 * this abstract class is meant to clean the process of receiving and handling updates
 * from event bus. and since Guava library tends to wrap exceptions in a vague and
 * unreachable wrapper this class allows for exceptions to be caught by the user.
 */
public abstract class AbstractUpdateReceiver<T extends AbstractEvent> {

	public static final int NETWORK = 0;
	public static final int INTERNAL = 1;
	
	protected final Object OWNER;
	protected final int ID; 
	private final int BUS;
	
	
	/**
	 * create an instance of <code>AbstractUpdateReceiver</code> and 
	 * register on either network or internal event bus
	 * @param BUS - AbstractUpdateReceiver.NETWORK(0) or AbstractUpdateReceiver.INTERNAL(1)
	 * @param ID - specific unique ID from Constants to represent this receiver (ex. GUI, LOGIC, BOARD, PROGRESS, LOAD_RESOURCE)
	 * @param OWNER - this must be the most outer class that holds AbstractUpdateReceiver
	 */
	protected AbstractUpdateReceiver( final int BUS, final int ID, final Object OWNER){
		registerOnEventBus( BUS);
		this.ID = ID;
		this.BUS = BUS;
		this.OWNER = OWNER;
	}
	
	/**
	 * main method to receive update from event bus. this methods will
	 * ensure that event received is not from the same OWNER first, then
	 * update is checked for private or public status and appropriate
	 * handlePrivate or handlePublic methods is called to process update.
	 * @param update - update received from event bus
	 */
	@Subscribe
	public final void receiveUpdate( T update){
		try{
			if(update.getOwner()!=OWNER){
				if( verifyPublic( update)){
					handlePublic( update);
				}else if(verifyPrivate( update)){
					handlePrivate( update);
				}
			}
		}catch(ClassCastException ex){
			//temporary error that will be resolved by
			//full implementation of UpatePakcage
		}catch( Exception ex){
			Logger.getErrorLogger().fatal( ex.getMessage() + ", Owner: " + OWNER, ex);
		}
	}
	
	/**
	 * register this class to receive updates from network event bus or internal event bus
	 * @param BUS - AbstractUpdateReceiver.NETWORK(0) or AbstractUpdateReceiver.INTERNAL(1)
	 */
	public final void registerOnEventBus( final int BUS){
		switch( BUS){
			case NETWORK: EventDispatch.registerOnNetwrokEvents( this);break;
			case INTERNAL: EventDispatch.registerOnInternalEvents( this);break;
			default:
				throw new IllegalArgumentException( "ERROR - BUS must be AbstractUpdateReceiver.NETWORK(0) or AbstractUpdateReceiver.INTERNAL(1)");
		}
	}
	
	/**
	 * unregister this class from already registered event bus
	 */
	public final void unregisterFromEventBus(){
		switch( BUS){
			case NETWORK: EventDispatch.unregisterFromNetworkEvents( this);break;
			case INTERNAL: EventDispatch.unregisterFromInternalEvents( this);break;
		}
	}
	
	/**
	 * called after update.isPublic() method has returned true.
	 * this method is meant to handle any needed code for processing
	 * public events not private events.
	 * @throws IllegalStateException - if this methods is not overridden and called
	 */
	public void handlePublic( T update){
		throw new IllegalStateException( "This method must be Overridden");
	}
	
	/**
	 * called after verifyPrivate method has returned true. this method 
	 * is meant to handle any needed code for processing update for
	 * private events not public event.
	 * @throws IllegalStateException - if this methods is not overridden and called
	 */
	public void handlePrivate( T update){
		throw new IllegalStateException( "This method must be Overridden");
	}
	
	/**
	 * check to see if all conditions (i.e ID or client specific) for processing 
	 * update are valid, if valid, return true. this will result in calling of
	 * handlePrivate, otherwise skip. by default this method returns false unless
	 * overridden by subclass.
	 * @return true if all conditions are valid, false otherwise
	 */
	public boolean verifyPrivate( T update){
		return false;
	}
	
	/**
	 * check to see if this update is public, if public, return true. will result
	 * in calling of handlePublic, otherwise skip. by default this method calls
	 * update.isPublic() and returns the result. can be overridden for more detail
	 * verification.
	 * @return true if all conditions are valid, false otherwise
	 */
	public boolean verifyPublic( T update){
		return update.isPublic();
	}
}

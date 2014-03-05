package common.event;

import common.Logger;
import com.google.common.eventbus.Subscribe;

/**
 * this abstract class is meant to clean the process of receiving and handling updates
 * from event bus. and since Guava library tends to wrap exceptions in a vague and
 * unreachable wrapper this class allows for exceptions to be caught by the user.
 */
public abstract class AbstractUpdateReceiver<T> {

	public static final int NETWORK = 0;
	public static final int INTERNAL = 1;
	
	protected final int ID; 
	private final int BUS;
	
	/**
	 * create an instance of <code>AbstractUpdateReceiver</code> and 
	 * register on either network or internal event bus
	 * @param BUS - AbstractUpdateReceiver.NETWORK(0) or AbstractUpdateReceiver.INTERNAL(1)
	 */
	protected AbstractUpdateReceiver( final int BUS, final int ID){
		registerOnEventBus( BUS);
		this.ID = ID;
		this.BUS = BUS;
	}
	
	/**
	 * main method to receive update from event bus. this methods will call
	 * verify and if result are true it will continue to call handle, otherwise
	 * the method ends without doing anything else. all exceptions that may occur
	 * in handle or verify methods will be caught in this method and logged as
	 * fatal error message.
	 * @param update - update received from event bus
	 */
	@Subscribe
	public final void receiveUpdate( T update){
		try{
			if( verify( update)){
				handle( update);
			}
		}catch( Exception ex){
			Logger.getErrorLogger().fatal( ex.getMessage(), ex);
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
	 * called after verify method has returned true. this method 
	 * is meant to handle any needed code for processing update.
	 */
	public abstract void handle( T update);
	
	/**
	 * check to see if all conditions (i.e ID, public) for processing 
	 * update are valid, if valid, call handle, otherwise skip.
	 * by default this method returns true unless overridden by subclass.
	 * @return true if all conditions are valid, false otherwise
	 */
	public boolean verify( T update){
		return true;
	}
}

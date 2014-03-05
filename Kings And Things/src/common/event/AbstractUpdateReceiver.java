package common.event;

import com.google.common.eventbus.Subscribe;

import common.Logger;

/**
 * this abstract class is meant to clean the process of receiving and handling
 * <code>UpdatePackage</code> from event bus. and since Guava library tends to wrap
 * exceptions in a vague and unreachable wrapper this class allows for exceptions
 * to be caught by the user.
 */
public abstract class AbstractUpdateReceiver {

	public static final int NETWORK = 0;
	public static final int INTERNAL = 1;
	
	/**
	 * main method to receive <code>UpdatePackage</code> from event bus. this methods will call
	 * verify and if result are true it will continue to call handle, otherwise
	 * the method ends without doing anything else. all exceptions that may occur
	 * in handle or verify methods will be caught in this method and logged as
	 * fatal error message.
	 * @param update - <code>UpdatePackage</code> received from event bus
	 */
	@Subscribe
	public final void receiveUpdate( UpdatePackage update){
		try{
			if( verify( update)){
				handle( update);
			}
		}catch( Exception ex){
			Logger.getErrorLogger().fatal( ex.getMessage(), ex);
		}
	}
	
	/**
	 * register this class to receive <code>UpdatePackage</code> on
	 * network event bus or internal event bus
	 * @param BUS - AbstractUpdateReceiver.NETWORK(0) or AbstractUpdateReceiver.INTERNAL(1)
	 */
	public final void registerOnEventBus( final int BUS){
		switch( BUS){
			case NETWORK: EventDispatch.registerForNetwrokEvents( this);break;
			case INTERNAL: EventDispatch.registerForInternalEvents( this);break;
			default:
				throw new IllegalArgumentException( "ERROR - BUS must be AbstractUpdateReceiver.NETWORK(0) or AbstractUpdateReceiver.INTERNAL(1)");
		}
	}
	
	/**
	 * called after verify method has returned true. this method is 
	 * meant to handle any needed code for processing <code>UpdatePackage</code>
	 */
	public abstract void handle( UpdatePackage update);
	
	/**
	 * check to see if all conditions for processing <code>UpdatePackage</code>
	 * (i.e ID, public) are valid, if valid call handle, otherwise skip.
	 * @return true if all conditions are valid, false otherwise
	 */
	public abstract boolean verify( UpdatePackage update);
}

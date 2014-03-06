package common.event;

import com.google.common.eventbus.EventBus;

public abstract class EventDispatch
{
	/**
	 * This is Google's implementation of an Event Bus, it allows objects to pass events around
	 * in much the same way as a data bus passes data around. Objects that want to consume events
	 * register with the Event Bus and create a public handler method for any event they want to receive.
	 * There is no need to implement any interfaces and the method can have any name, just put the
	 * '@Subscribe' annotation above the method. The method must take only one parameter, the event
	 * to be handled. Any time an object is posted to the EventBus, any registered handlers with
	 * '@Subscribe' methods that take an argument of that object type, will be called with the object.
	 * <p>
	 * See GameFlowManager for an example on registering for events and handling them, see Command
	 * for an example on posting events.
	 */
	static final EventBus INTERNAL = new EventBus();
	static final EventBus NETWORK = new EventBus();
	
	public static void registerOnInternalEvents( Object obj){
		INTERNAL.register( obj);
	}
	
	public static void registerOnNetwrokEvents( Object obj){
		NETWORK.register( obj);
	}

	public static void unregisterFromInternalEvents( Object obj) {
		INTERNAL.unregister( obj);
	}

	public static void unregisterFromNetworkEvents( Object obj) {
		NETWORK.unregister( obj);
	}
}

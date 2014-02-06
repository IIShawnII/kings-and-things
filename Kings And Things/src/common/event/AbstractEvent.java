package common.event;

import com.google.common.eventbus.EventBus;


/**
 * This is the super class of all notifications that can be sent across
 * the network
 */
public abstract class AbstractEvent
{
	private EventBus bus;
	
	protected AbstractEvent( EventBus bus){
		this.bus = bus;
	}
	
	/**
	 * Post this notification to the CommandEventBus so any registered
	 * listeners can handle it
	 */
	public void dispatch()
	{
		bus.post(this);
	}
}

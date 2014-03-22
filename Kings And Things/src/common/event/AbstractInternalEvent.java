package common.event;

import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractInternalEvent extends AbstractEvent
{
	private final AtomicBoolean isUnhandled = new AtomicBoolean(true);
	
	protected AbstractInternalEvent( final Object OWNER){
		super( OWNER);
	}
	
	protected AbstractInternalEvent(){
		super( null);
	}
	
	/**
	 * Use this for events you want to have processed only once
	 * per event instance
	 */
	public boolean isUnhandled()
	{
		return isUnhandled.compareAndSet(true, false);
	}
}

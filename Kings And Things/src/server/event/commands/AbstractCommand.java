package server.event.commands;

import java.util.concurrent.atomic.AtomicBoolean;

import common.event.AbstractEvent;

public class AbstractCommand extends AbstractEvent
{
	private final AtomicBoolean isUnhandled = new AtomicBoolean(true);
	
	/**
	 * Use this for events you want to have processed only once
	 * per event instance
	 */
	public boolean isUnhandled()
	{
		return isUnhandled.compareAndSet(true, false);
	}
}

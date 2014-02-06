package common.event.notifications;

import common.event.NotificationEventBus;

/**
 * This is the super class of all notifications that can be sent across
 * the network
 */
public abstract class Notification
{
	/**
	 * Post this notification to the CommandEventBus so any registered
	 * listeners can handle it
	 */
	public void dispatch()
	{
		NotificationEventBus.BUS.post(this);
	}
}

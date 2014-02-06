package server.event.commands;

import common.event.notifications.AbstractNotification;


public class SendNotificationAcrossNetworkEvent extends AbstractNotification
{
	private final AbstractNotification notification;
	
	/**
	 * This event represents a command that needs to be sent across the network,
	 * either from client to server, or server to client
	 * @param notification The notification to be sent
	 */
	public SendNotificationAcrossNetworkEvent( AbstractNotification notification)
	{
		this.notification = notification;
	}
	
	/**
	 * Gets the notification to be sent
	 * @return The notification to send
	 */
	public AbstractNotification getNotification()
	{
		return notification;
	}
}

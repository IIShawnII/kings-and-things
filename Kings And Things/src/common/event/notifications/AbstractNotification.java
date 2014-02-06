package common.event.notifications;

import common.event.AbstractEvent;
import common.event.NotificationEventBus;


public class AbstractNotification extends AbstractEvent {
	
	protected AbstractNotification(){
		super( NotificationEventBus.BUS);
	}
}

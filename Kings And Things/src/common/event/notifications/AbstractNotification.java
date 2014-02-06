package common.event.notifications;

import common.event.AbstractEvent;
import common.event.EventDispatch;


public class AbstractNotification extends AbstractEvent {
	
	protected AbstractNotification(){
		super( EventDispatch.NOTIFICATION);
	}
}

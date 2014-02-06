package client.event;

import common.event.AbstractEvent;
import common.event.EventDispatch;

public class AbstractCommand extends AbstractEvent {

	protected AbstractCommand() {
		super( EventDispatch.COMMAND);
	}
}

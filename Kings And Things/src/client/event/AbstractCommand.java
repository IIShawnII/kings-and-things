package client.event;

import common.event.AbstractEvent;
import common.event.CommandEventBus;

public class AbstractCommand extends AbstractEvent {

	protected AbstractCommand() {
		super( CommandEventBus.BUS);
	}

}

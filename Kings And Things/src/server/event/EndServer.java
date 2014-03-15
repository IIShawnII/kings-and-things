package server.event;

import common.event.AbstractInternalEvent;


public class EndServer extends AbstractInternalEvent {
	
	public EndServer( final Object OWNER){
		super( OWNER);
	}
}

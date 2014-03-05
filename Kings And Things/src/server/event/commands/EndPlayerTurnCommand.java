package server.event.commands;

import common.event.AbstractInternalEvent;


public class EndPlayerTurnCommand extends AbstractInternalEvent{

	public EndPlayerTurnCommand( final Object OWNER){
		super( OWNER);
	}
}

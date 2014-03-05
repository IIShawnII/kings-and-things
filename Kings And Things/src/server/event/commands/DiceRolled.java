package server.event.commands;

import common.event.AbstractInternalEvent;

public class DiceRolled extends AbstractInternalEvent{
	
	public DiceRolled( final Object OWNER){
		super( OWNER);
	}
}

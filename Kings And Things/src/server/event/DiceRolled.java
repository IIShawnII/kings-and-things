package server.event;

import common.event.AbstractInternalEvent;

public class DiceRolled extends AbstractInternalEvent{
	
	public DiceRolled( final Object OWNER){
		super( OWNER);
	}
}

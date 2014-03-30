package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.Roll;

public class RollDiceCommand extends AbstractInternalEvent{
	
	private final Roll roll;
	
	public RollDiceCommand( Roll roll){
		this.roll = roll;
	}
	
	public Roll getRoll(){
		return roll;
	}
}

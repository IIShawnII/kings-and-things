package server.event.internal;

import common.event.AbstractInternalEvent;

public class TargetPlayerCommand extends AbstractInternalEvent{
	
	private final int targetID;
	
	public TargetPlayerCommand(int targetID){
		super();
		this.targetID = targetID;
	}
	
	public int getTargetID(){
		return targetID;
	}
}

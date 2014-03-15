package server.event.commands;

import common.event.AbstractInternalEvent;

public class TargetPlayerCommand extends AbstractInternalEvent
{
	private final int targetID;
	
	public TargetPlayerCommand(int targetID, Object OWNER)
	{
		super(OWNER);
		this.targetID = targetID;
	}
	
	public int getTargetID()
	{
		return targetID;
	}
}

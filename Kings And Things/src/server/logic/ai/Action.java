package server.logic.ai;

import common.event.AbstractInternalEvent;

public class Action
{
	private final AbstractInternalEvent command;
	
	public Action(AbstractInternalEvent command)
	{
		this.command = command;
	}
	
	public AbstractInternalEvent getCommand()
	{
		return command;
	}
}

package common.game.commands;

import common.event.CommandEventBus;

/**
 * This is the super class of all commands that can be sent across
 * the network
 */
public abstract class Command
{
	/**
	 * Post this command to the CommandEventBus so any registered
	 * listeners can handle it
	 */
	public void dispatch()
	{
		CommandEventBus.BUS.post(this);
	}
}

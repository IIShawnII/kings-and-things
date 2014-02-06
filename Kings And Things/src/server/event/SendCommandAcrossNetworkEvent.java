package server.event;

import common.event.commands.Command;

public class SendCommandAcrossNetworkEvent
{
	private final Command command;
	
	/**
	 * This event represents a command that needs to be sent across the network,
	 * either from client to server, or server to client
	 * @param command The command to be sent
	 */
	public SendCommandAcrossNetworkEvent(Command command)
	{
		this.command = command;
	}
	
	/**
	 * Gets the command to be sent
	 * @return The command to send
	 */
	public Command getCommand()
	{
		return command;
	}
}

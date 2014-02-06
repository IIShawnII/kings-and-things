package server.event.commands;


public class SendCommandAcrossNetworkEvent extends AbstractCommand
{
	private final AbstractCommand command;
	
	/**
	 * This event represents a command that needs to be sent across the network,
	 * either from client to server, or server to client
	 * @param command The command to be sent
	 */
	public SendCommandAcrossNetworkEvent(AbstractCommand command)
	{
		this.command = command;
	}
	
	/**
	 * Gets the command to be sent
	 * @return The command to send
	 */
	public AbstractCommand getCommand()
	{
		return command;
	}
}

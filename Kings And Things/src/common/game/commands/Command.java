package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;

import common.event.CommandEventBus;

/**
 * This is the super class of all commands that can be sent across
 * the network
 */
public abstract class Command
{
	@XmlAttribute
	private int playerNumber;
	
	/**
	 * Post this command to the CommandEventBus so any registered
	 * listeners can handle it. The player number of the player sending the command is
	 * injected at this stage, so that the client doesn't need to specify it.
	 * @param playerNumber The number of the player who sent the command
	 */
	public void dispatch(int playerNumber)
	{
		this.playerNumber = playerNumber;
		CommandEventBus.BUS.post(this);
	}
	
	/**
	 * Retrieves the number of the player who sent the command
	 * @return The player number of the player who sent the
	 * command
	 */
	public int getPlayerNumber()
	{
		return playerNumber;
	}
}

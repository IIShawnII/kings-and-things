package server.event.commands;

import javax.xml.bind.annotation.XmlAttribute;

import common.event.AbstractEvent;
import common.event.EventDispatch;

/**
 * This is the super class of all commands that can be sent across
 * the network
 */
public abstract class AbstractCommand extends AbstractEvent
{
	@XmlAttribute
	private int playerNumber;
	
	protected AbstractCommand(){
		super( EventDispatch.COMMAND);
	}
	
	/**
	 * Post this command to the CommandEventBus so any registered
	 * listeners can handle it. The player number of the player sending the command is
	 * injected at this stage, so that the client doesn't need to specify it.
	 * @param playerNumber The number of the player who sent the command
	 */
	public void post(int playerNumber)
	{
		this.playerNumber = playerNumber;
		super.post();
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

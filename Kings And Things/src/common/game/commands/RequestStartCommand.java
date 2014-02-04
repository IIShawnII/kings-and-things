package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestStartCommand extends Command
{
	@XmlAttribute
	private final int playerNumber;
	
	public RequestStartCommand(int playerNumber)
	{
		this.playerNumber = playerNumber;
	}

	public int getPlayerNumber()
	{
		return playerNumber;
	}
	
	@SuppressWarnings("unused")
	private RequestStartCommand()
	{
		//required by JAXB
		this(0);
	}
}

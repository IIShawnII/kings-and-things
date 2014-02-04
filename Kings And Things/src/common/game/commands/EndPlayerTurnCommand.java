package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class EndPlayerTurnCommand extends Command
{
	@XmlAttribute
	private final int playerNumber;
	
	public EndPlayerTurnCommand(int playerNumber)
	{
		this.playerNumber = playerNumber;
	}
	
	public int getPlayerNumber()
	{
		return playerNumber;
	}

	@SuppressWarnings("unused")
	private EndPlayerTurnCommand()
	{
		//required by JAXB
		playerNumber = 0;
	}
}

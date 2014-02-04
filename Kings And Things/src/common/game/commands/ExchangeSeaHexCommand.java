package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class ExchangeSeaHexCommand extends Command
{
	@XmlElement
	private final TileProperties hex;
	@XmlAttribute
	private final int playerNumber;
	
	public ExchangeSeaHexCommand(TileProperties hex, int playerNumber)
	{
		this.hex = hex;
		this.playerNumber = playerNumber;
	}
	
	public TileProperties getHex()
	{
		return hex;
	}
	
	public int getPlayerNumber()
	{
		return playerNumber;
	}

	@SuppressWarnings("unused")
	private ExchangeSeaHexCommand()
	{
		//required by JAXB
		playerNumber = 0;
		hex = null;
	}
}

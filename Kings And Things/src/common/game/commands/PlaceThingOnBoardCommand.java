package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class PlaceThingOnBoardCommand extends Command
{
	@XmlElement
	private final TileProperties hex;
	@XmlAttribute
	private final int playerNumber;
	@XmlElement
	private final TileProperties thing;
	
	public PlaceThingOnBoardCommand(TileProperties thing, int playerNumber, TileProperties hex)
	{
		this.thing = thing;
		this.hex = hex;
		this.playerNumber = playerNumber;
	}
	
	public TileProperties getThing()
	{
		return thing;
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
	private PlaceThingOnBoardCommand()
	{
		//required by JAXB
		playerNumber = 0;
		hex = null;
		thing = null;
	}
}

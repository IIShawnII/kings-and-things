package server.event.commands;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class PlaceThingOnBoardCommand extends AbstractCommand
{
	@XmlElement
	private final TileProperties hex;
	@XmlElement
	private final TileProperties thing;
	
	public PlaceThingOnBoardCommand(TileProperties thing, TileProperties hex)
	{
		this.thing = thing;
		this.hex = hex;
	}
	
	public TileProperties getThing()
	{
		return thing;
	}
	
	public TileProperties getHex()
	{
		return hex;
	}
	
	@SuppressWarnings("unused")
	private PlaceThingOnBoardCommand()
	{
		//required by JAXB
		hex = null;
		thing = null;
	}
}

package server.event.commands;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class GiveHexToPlayerCommand extends AbstractCommand
{
	@XmlElement
	private final TileProperties hex;
	
	public GiveHexToPlayerCommand(TileProperties hex)
	{
		this.hex = hex;
	}
	
	public TileProperties getHex()
	{
		return hex;
	}

	@SuppressWarnings("unused")
	private GiveHexToPlayerCommand()
	{
		//required by JAXB
		hex = null;
	}
}

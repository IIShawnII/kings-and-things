package server.event.commands;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class ExchangeSeaHexCommand extends AbstractCommand
{
	@XmlElement
	private final TileProperties hex;
	
	public ExchangeSeaHexCommand(TileProperties hex)
	{
		super();
		this.hex = hex;
	}
	
	public TileProperties getHex()
	{
		return hex;
	}

	@SuppressWarnings("unused")
	private ExchangeSeaHexCommand()
	{
		//required by JAXB
		hex = null;
	}
}

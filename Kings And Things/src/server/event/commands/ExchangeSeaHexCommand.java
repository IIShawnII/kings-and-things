package server.event.commands;

import common.TileProperties;
import common.event.AbstractEvent;

public class ExchangeSeaHexCommand extends AbstractEvent{
	
	private final TileProperties hex;
	
	public ExchangeSeaHexCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

package server.event.commands;

import common.TileProperties;
import common.event.AbstractEvent;

public class ExchangeSeaHexCommand extends AbstractEvent{

	private static final long serialVersionUID = -8431537364031114053L;
	
	private final TileProperties hex;
	
	public ExchangeSeaHexCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

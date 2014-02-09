package server.event.commands;

import common.game.TileProperties;

public class ExchangeSeaHexCommand extends AbstractCommand{
	
	private final TileProperties hex;
	
	public ExchangeSeaHexCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

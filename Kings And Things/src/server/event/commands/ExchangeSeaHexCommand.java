package server.event.commands;

import common.game.ITileProperties;

public class ExchangeSeaHexCommand extends AbstractCommand{
	
	private final ITileProperties hex;
	
	public ExchangeSeaHexCommand(ITileProperties hex){
		this.hex = hex;
	}
	
	public ITileProperties getHex(){
		return hex;
	}
}

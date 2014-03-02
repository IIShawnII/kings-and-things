package server.event.commands;

import common.game.ITileProperties;

public class GiveHexToPlayerCommand extends AbstractCommand{
	
	private final ITileProperties hex;
	
	public GiveHexToPlayerCommand(ITileProperties hex){
		this.hex = hex;
	}
	
	public ITileProperties getHex(){
		return hex;
	}
}

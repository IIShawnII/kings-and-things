package server.event.commands;

import common.game.TileProperties;

public class GiveHexToPlayerCommand extends AbstractCommand{
	
	private final TileProperties hex;
	
	public GiveHexToPlayerCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

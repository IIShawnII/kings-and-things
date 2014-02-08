package server.event.commands;

import common.event.AbstractEvent;
import common.game.TileProperties;

public class GiveHexToPlayerCommand extends AbstractEvent{
	
	private final TileProperties hex;
	
	public GiveHexToPlayerCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

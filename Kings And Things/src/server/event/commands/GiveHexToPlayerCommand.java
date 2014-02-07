package server.event.commands;

import common.TileProperties;
import common.event.AbstractEvent;

public class GiveHexToPlayerCommand extends AbstractEvent{
	
	private final TileProperties hex;
	
	public GiveHexToPlayerCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

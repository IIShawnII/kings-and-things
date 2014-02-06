package server.event.commands;

import common.TileProperties;
import common.event.AbstractEvent;

public class GiveHexToPlayerCommand extends AbstractEvent{
	
	private static final long serialVersionUID = -2046312797845461616L;
	
	private final TileProperties hex;
	
	public GiveHexToPlayerCommand(TileProperties hex){
		this.hex = hex;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

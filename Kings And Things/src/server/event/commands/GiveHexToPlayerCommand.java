package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class GiveHexToPlayerCommand extends AbstractInternalEvent{
	
	private final ITileProperties hex;
	
	public GiveHexToPlayerCommand(ITileProperties hex, final Object OWNER){
		super( OWNER);
		this.hex = hex;
	}
	
	public ITileProperties getHex(){
		return hex;
	}
}

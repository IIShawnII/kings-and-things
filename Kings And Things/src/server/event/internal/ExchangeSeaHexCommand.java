package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.HexState;
import common.game.ITileProperties;

public class ExchangeSeaHexCommand extends AbstractInternalEvent{
	
	private final ITileProperties hex;
	private boolean isOwned;
	
	public ExchangeSeaHexCommand(HexState hex){
		this( hex.getHex());
		isOwned = hex.hasMarker();
	}
	
	public ExchangeSeaHexCommand(ITileProperties hex){
		super();
		this.hex = hex;
	}
	
	public ITileProperties getHex(){
		return hex;
	}

	public boolean isOwned() {
		return isOwned;
	}
}

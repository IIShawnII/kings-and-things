package server.event.commands;

import common.TileProperties;
import common.event.AbstractEvent;

public class PlaceThingOnBoardCommand extends AbstractEvent{
	
	private static final long serialVersionUID = 2202311714278703144L;
	
	private final TileProperties hex;
	private final TileProperties thing;
	
	public PlaceThingOnBoardCommand(TileProperties thing, TileProperties hex){
		this.thing = thing;
		this.hex = hex;
	}
	
	public TileProperties getThing(){
		return thing;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

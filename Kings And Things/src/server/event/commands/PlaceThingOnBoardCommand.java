package server.event.commands;

import common.game.TileProperties;

public class PlaceThingOnBoardCommand extends AbstractCommand{
	
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

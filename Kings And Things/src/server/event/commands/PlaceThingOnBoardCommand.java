package server.event.commands;

import common.event.AbstractCommand;
import common.game.ITileProperties;

public class PlaceThingOnBoardCommand extends AbstractCommand{
	
	private final ITileProperties hex;
	private final ITileProperties thing;
	
	public PlaceThingOnBoardCommand(ITileProperties thing, ITileProperties hex){
		this.thing = thing;
		this.hex = hex;
	}
	
	public ITileProperties getThing(){
		return thing;
	}
	
	public ITileProperties getHex(){
		return hex;
	}
}

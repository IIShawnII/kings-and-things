package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class PlaceThingOnBoardCommand extends AbstractInternalEvent{
	
	private final ITileProperties hex;
	private final ITileProperties thing;
	
	public PlaceThingOnBoardCommand(ITileProperties thing, ITileProperties hex){
		super();
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

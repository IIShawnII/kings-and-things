package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class RetreatCommand extends AbstractInternalEvent{
	
	private final ITileProperties destinationHex;

	public RetreatCommand(ITileProperties destinationHex){
		super();
		this.destinationHex = destinationHex;
	}
	
	public ITileProperties getDestinationHex(){
		return destinationHex;
	}
}

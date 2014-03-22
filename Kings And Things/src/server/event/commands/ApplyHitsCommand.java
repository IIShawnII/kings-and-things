package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ApplyHitsCommand extends AbstractInternalEvent{
	
	private final int numHits;
	private final ITileProperties target;
	
	public ApplyHitsCommand(int numHits, ITileProperties target){
		super();
		this.numHits = numHits;
		this.target = target;
	}

	public int getNumHits(){
		return numHits;
	}
	
	public ITileProperties getTarget(){
		return target;
	}
}

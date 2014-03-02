package server.event.commands;

import common.game.ITileProperties;

public class ApplyHitsCommand extends AbstractCommand
{
	private final int numHits;
	private final ITileProperties target;
	
	public ApplyHitsCommand(int numHits, ITileProperties target)
	{
		this.numHits = numHits;
		this.target = target;
	}

	public int getNumHits()
	{
		return numHits;
	}
	
	public ITileProperties getTarget()
	{
		return target;
	}
}

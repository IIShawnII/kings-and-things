package server.event.commands;

import common.game.TileProperties;

public class ApplyHitsCommand extends AbstractCommand
{
	private final int numHits;
	private final TileProperties target;
	
	public ApplyHitsCommand(int numHits, TileProperties target)
	{
		this.numHits = numHits;
		this.target = target;
	}

	public int getNumHits()
	{
		return numHits;
	}
	
	public TileProperties getTarget()
	{
		return target;
	}
}

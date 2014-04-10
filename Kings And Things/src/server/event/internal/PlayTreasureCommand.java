package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class PlayTreasureCommand extends AbstractInternalEvent
{
	private final ITileProperties treasure;
	public PlayTreasureCommand(ITileProperties treasure)
	{
		this.treasure = treasure;
	}
	
	public ITileProperties getTreasure()
	{
		return treasure;
	}
}

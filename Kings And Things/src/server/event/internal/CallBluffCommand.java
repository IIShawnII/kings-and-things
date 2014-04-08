package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class CallBluffCommand extends AbstractInternalEvent
{
	private final ITileProperties creature;
	
	public CallBluffCommand(ITileProperties creature)
	{
		this.creature = creature;
	}
	
	public ITileProperties getCreature()
	{
		return creature;
	}
}

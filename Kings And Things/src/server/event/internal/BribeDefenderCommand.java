package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class BribeDefenderCommand extends AbstractInternalEvent
{
	private final ITileProperties defender;
	
	public BribeDefenderCommand(ITileProperties defender)
	{
		this.defender = defender;
	}
	
	public ITileProperties getDefender()
	{
		return defender;
	}
}

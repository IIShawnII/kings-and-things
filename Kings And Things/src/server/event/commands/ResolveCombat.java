package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ResolveCombat extends AbstractInternalEvent
{
	private final ITileProperties hex;

	public ResolveCombat(ITileProperties hex, final Object OWNER){
		super( OWNER);
		this.hex = hex;
	}

	public ITileProperties getCombatHex()
	{
		return hex;
	}
}

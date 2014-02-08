package server.event.commands;

import common.event.AbstractEvent;
import common.game.TileProperties;

public class ResolveCombat extends AbstractEvent
{
	private final TileProperties hex;

	public ResolveCombat(TileProperties hex)
	{
		this.hex = hex;
	}

	public TileProperties getCombatHex()
	{
		return hex;
	}
}

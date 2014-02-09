package server.event.commands;

import common.game.TileProperties;

public class ResolveCombat extends AbstractCommand
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

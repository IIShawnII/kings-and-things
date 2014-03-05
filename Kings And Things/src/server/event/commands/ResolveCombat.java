package server.event.commands;

import common.event.AbstractCommand;
import common.game.ITileProperties;

public class ResolveCombat extends AbstractCommand
{
	private final ITileProperties hex;

	public ResolveCombat(ITileProperties hex)
	{
		this.hex = hex;
	}

	public ITileProperties getCombatHex()
	{
		return hex;
	}
}

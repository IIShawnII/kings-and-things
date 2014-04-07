package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;
import common.game.Player;

public class ExplorationResults extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = 3192925477280776743L;
	private final HexState hex;
	private final Player explorer;
	
	public ExplorationResults(HexState hex, Player p)
	{
		this.hex = hex;
		explorer = p;
	}

	public HexState getHex()
	{
		return hex;
	}

	public Player getExplorer()
	{
		return explorer;
	}
}

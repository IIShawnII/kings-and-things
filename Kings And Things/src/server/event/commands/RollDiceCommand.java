package server.event.commands;

import common.Constants.RollReason;
import common.event.AbstractEvent;
import common.game.TileProperties;

public class RollDiceCommand extends AbstractEvent
{
	private final RollReason reason;
	private final TileProperties tile;
	
	public RollDiceCommand(RollReason reasonForRoll, TileProperties tile)
	{
		reason = reasonForRoll;
		this.tile = tile;
	}
	
	public RollReason getReasonForRoll()
	{
		return reason;
	}
	
	public TileProperties getTileToRollFor()
	{
		return tile;
	}
}

package server.event.commands;

import common.Constants.RollReason;
import common.game.TileProperties;

public class RollDiceCommand extends AbstractCommand
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

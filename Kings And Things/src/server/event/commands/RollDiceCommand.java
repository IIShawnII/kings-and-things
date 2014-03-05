package server.event.commands;

import common.Constants.RollReason;
import common.event.AbstractCommand;
import common.game.ITileProperties;

public class RollDiceCommand extends AbstractCommand
{
	private final RollReason reason;
	private final ITileProperties tile;
	
	public RollDiceCommand(RollReason reasonForRoll, ITileProperties tile)
	{
		reason = reasonForRoll;
		this.tile = tile;
	}
	
	public RollReason getReasonForRoll()
	{
		return reason;
	}
	
	public ITileProperties getTileToRollFor()
	{
		return tile;
	}
}

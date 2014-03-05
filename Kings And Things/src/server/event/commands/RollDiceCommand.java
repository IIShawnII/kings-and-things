package server.event.commands;

import common.Constants.RollReason;
import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class RollDiceCommand extends AbstractInternalEvent
{
	private final RollReason reason;
	private final ITileProperties tile;
	
	public RollDiceCommand(RollReason reasonForRoll, ITileProperties tile, final Object OWNER){
		super( OWNER);
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

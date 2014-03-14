package server.event.commands;

import common.Constants.RollReason;
import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class RollDiceCommand extends AbstractInternalEvent
{
	private final RollReason reason;
	private final ITileProperties tile;
	private final int rollValue;
	
	public RollDiceCommand(RollReason reasonForRoll, ITileProperties tile, final Object OWNER){
		this(reasonForRoll,tile,3,OWNER);
	}

	/**
	 * Use this constructor to specify the value of the roll before it is rolled,
	 * this value will be ignored unless running in demo mode
	 */
	public RollDiceCommand(RollReason reasonForRoll, ITileProperties tile, int rollValue, final Object OWNER){
		super( OWNER);
		reason = reasonForRoll;
		this.tile = tile;
		this.rollValue = rollValue;
	}
	
	public RollReason getReasonForRoll()
	{
		return reason;
	}
	
	public ITileProperties getTileToRollFor()
	{
		return tile;
	}
	
	/**
	 * Gets the value this roll should have when rolled, useable only in demo mode
	 * @return The value of the roll
	 */
	public int getRollValue()
	{
		return rollValue;
	}
}

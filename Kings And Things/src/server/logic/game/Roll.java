package server.logic.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Constants.RollReason;
import common.game.TileProperties;

public class Roll
{
	private final ArrayList<Integer> rolls;
	private final TileProperties rollTarget;
	private final int diceCount;
	private final RollReason rollReason;
	private final int playerNumber;
	
	public Roll(int diceCount, TileProperties tileToRollFor, RollReason reason, int playerRolling)
	{
		this.diceCount = diceCount;
		this.rollTarget = tileToRollFor;
		rollReason = reason;
		rolls = new ArrayList<Integer>();
		playerNumber = playerRolling;
	}
	
	public int getDiceCount()
	{
		return diceCount;
	}
	
	public TileProperties getRollTarget()
	{
		return rollTarget;
	}
	
	public List<Integer> getRolls()
	{
		return Collections.unmodifiableList(rolls);
	}
	
	public void addRoll(int roll)
	{
		if(needsRoll())
		{
			rolls.add(roll);
		}
		else
		{
			throw new IllegalArgumentException("Can not roll again for this target");
		}
	}
	
	public boolean needsRoll()
	{
		return rolls.size() < diceCount;
	}
	
	public RollReason getRollReason()
	{
		return rollReason;
	}
	
	public int getRollingPlayerID()
	{
		return playerNumber;
	}
	
	public static boolean rollSatisfiesParameters(Roll r,RollReason reasonForRoll, int playerNumber, TileProperties tileToRollFor)
	{
		return r.getRollingPlayerID() == playerNumber && r.getRollReason()==reasonForRoll && (r.getRollTarget()==null? tileToRollFor==null : r.getRollTarget().equals(tileToRollFor));
	}
}

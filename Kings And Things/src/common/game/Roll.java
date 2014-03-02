package common.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import common.Constants.RollReason;

public class Roll
{
	private final ArrayList<Integer> baseRolls;
	private final HashMap<Integer,Integer> rollModifications;
	private final ITileProperties rollTarget;
	private final int diceCount;
	private final RollReason rollReason;
	private final int playerNumber;
	
	public Roll(int diceCount, ITileProperties tileToRollFor, RollReason reason, int playerRolling)
	{
		this.diceCount = diceCount;
		this.rollTarget = tileToRollFor;
		rollReason = reason;
		baseRolls = new ArrayList<Integer>();
		rollModifications = new HashMap<>();
		playerNumber = playerRolling;
	}
	
	public int getDiceCount()
	{
		return diceCount;
	}
	
	public ITileProperties getRollTarget()
	{
		return rollTarget;
	}
	
	public List<Integer> getBaseRolls()
	{
		return Collections.unmodifiableList(baseRolls);
	}
	
	public void addBaseRoll(int roll)
	{
		if(needsRoll())
		{
			baseRolls.add(roll);
		}
		else
		{
			throw new IllegalArgumentException("Can not roll again for this target");
		}
	}
	
	public List<Integer> getFinalRolls()
	{
		ArrayList<Integer> finalRolls = new ArrayList<Integer>();
		for(int i=0; i<baseRolls.size(); i++)
		{
			int baseRoll = baseRolls.get(i);
			if(rollModifications.containsKey(i))
			{
				baseRoll += rollModifications.get(i);
			}
			finalRolls.add(baseRoll);
		}
		return Collections.unmodifiableList(finalRolls);
	}
	
	public void addRollModificationFor(int baseRollIndex, int amount)
	{
		int previousModification = 0;
		if(rollModifications.containsKey(baseRollIndex))
		{
			previousModification = rollModifications.get(baseRollIndex);
		}
		rollModifications.put(baseRollIndex, amount + previousModification);
	}
	
	public boolean needsRoll()
	{
		return baseRolls.size() < diceCount;
	}
	
	public RollReason getRollReason()
	{
		return rollReason;
	}
	
	public int getRollingPlayerID()
	{
		return playerNumber;
	}
	
	public static boolean rollSatisfiesParameters(Roll r,RollReason reasonForRoll, int playerNumber, ITileProperties tileToRollFor)
	{
		return r.getRollingPlayerID() == playerNumber && r.getRollReason()==reasonForRoll && (r.getRollTarget()==null? tileToRollFor==null : r.getRollTarget().equals(tileToRollFor));
	}
}

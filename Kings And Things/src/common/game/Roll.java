package common.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import common.Constants.RollReason;

public class Roll implements Serializable{
	
	private static final long serialVersionUID = 266233708882040988L;
	
	private final ArrayList<Integer> baseRolls;
	private final HashMap<Integer,Integer> rollModifications;
	private final ITileProperties rollTarget;
	private final int diceCount;
	private final RollReason rollReason;
	private final int playerNumber;
	private final int targetValue;
	
	public Roll(int diceCount, ITileProperties tileToRollFor, RollReason reason, int playerRolling, int targetValue)
	{
		this.rollReason = reason;
		this.diceCount = diceCount;
		this.targetValue = targetValue;
		this.rollTarget = tileToRollFor;
		this.playerNumber = playerRolling;
		rollModifications = new HashMap<>();
		baseRolls = new ArrayList<Integer>();
	}
	
	public Roll(int diceCount, ITileProperties tileToRollFor, RollReason reason, int playerRolling)
	{
		this( diceCount, tileToRollFor, reason, playerRolling, 0);
	}
	
	public Roll(Roll other)
	{
		diceCount = other.diceCount;
		rollReason = other.rollReason;
		targetValue = other.targetValue;
		playerNumber = other.playerNumber;
		rollTarget = other.rollTarget.clone();
		baseRolls = new ArrayList<Integer>(other.baseRolls);
		rollModifications = new HashMap<>(other.rollModifications);
	}
	
	@Override
	public Roll clone()
	{
		return new Roll(this);
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
	
	public int getFinalTotal()
	{
		int total = 0;
		for(int roll : getFinalRolls())
		{
			total+= roll;
		}
		
		return total;
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
	
	public static boolean rollSatisfiesParameters(Roll r,RollReason reasonForRoll, int playerNumber, ITileProperties tileToRollFor, int numRolls)
	{
		return r.getRollingPlayerID() == playerNumber && r.getRollReason()==reasonForRoll && (r.getRollTarget()==null? tileToRollFor==null : r.getRollTarget().equals(tileToRollFor)) && (r.getDiceCount() - r.getBaseRolls().size())>=numRolls;
	}

	public int getTargetValue() {
		return targetValue;
	}
	
	@Override
	public String toString(){
		return "Reason: " + rollReason + ", Value: " + baseRolls + ", Size: " + baseRolls.size();
	}
}

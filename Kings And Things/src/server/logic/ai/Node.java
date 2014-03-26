package server.logic.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import common.game.Player;

import server.logic.game.GameState;

public class Node
{
	private long numPlayouts;
	private final HashMap<Integer,Long> numWinsPerPlayer;
	private final GameState state;
	private final Node parent;
	private final HashMap<Action,Node> children;
	
	private static final double C = Math.sqrt(2);
	
	public Node(GameState state, Node parent)
	{
		this.state = state;
		this.parent = parent;
		numWinsPerPlayer = new HashMap<>();
		children = new HashMap<>();
		for(Player p : state.getPlayers())
		{
			numWinsPerPlayer.put(p.getID(), 0L);
		}
	}
	
	public GameState getState()
	{
		return state;
	}
	
	public long getNumPlayouts()
	{
		return numPlayouts;
	}
	
	public long getNumWinsFor(int playerNumber)
	{
		return numWinsPerPlayer.get(playerNumber);
	}
	
	public void recordWinFor(int playerNumber)
	{
		numPlayouts++;
		if(numWinsPerPlayer.containsKey(playerNumber))
		{
			long oldWinCount = numWinsPerPlayer.get(playerNumber);
			numWinsPerPlayer.put(playerNumber, oldWinCount++);
		}
		if(parent!=null)
		{
			parent.recordWinFor(playerNumber);
		}
	}
	
	public void addChild(Action a, Node c)
	{
		children.put(a, c);
	}
	
	public Map<Action,Node> getChildren()
	{
		return Collections.unmodifiableMap(children);
	}
	
	public double calculateUpperConfidenceBound(int playerToMove)
	{
		return numPlayouts==0? Double.MAX_VALUE : (numWinsPerPlayer.get(playerToMove)/numPlayouts) + (C * Math.sqrt( Math.log(parent.getNumPlayouts()) / numPlayouts));
	}
	
	public double calculateAverageWinRateFor(int playerNumber)
	{
		return numWinsPerPlayer.get(playerNumber) / numPlayouts;
	}
}

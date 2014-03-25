package server.logic.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import server.logic.game.GameState;

public class Node
{
	private long numPlayouts;
	private long numWins;
	private final GameState state;
	private final Node parent;
	private final HashMap<Action,Node> children;
	
	private static final double C = Math.sqrt(2);
	
	public Node(GameState state, Node parent)
	{
		this.state = state;
		this.parent = parent;
		children = new HashMap<>();
	}
	
	public GameState getState()
	{
		return state;
	}
	
	public long getNumPlayouts()
	{
		return numPlayouts;
	}
	
	public long getNumWins()
	{
		return numWins;
	}
	
	public void recordWin()
	{
		numWins++;
		numPlayouts++;
	}
	
	public void recordLoss()
	{
		numPlayouts++;
	}
	
	public void addChild(Action a, Node c)
	{
		children.put(a, c);
	}
	
	public Map<Action,Node> getChildren()
	{
		return Collections.unmodifiableMap(children);
	}
	
	public double calculateUpperConfidenceBound()
	{
		return numPlayouts==0? Double.MAX_VALUE : (numWins/numPlayouts) + (C * Math.sqrt( Math.log(parent.getNumPlayouts()) / numPlayouts));
	}
}

package server.logic.ai;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import server.logic.game.GameState;

import common.game.Player;

public class MonteCarloTreeSearch extends Thread
{
	private Node root;
	private volatile Node latestPosition;
	private final boolean isDemoMode;
	
	public MonteCarloTreeSearch(boolean demoMode, GameState game)
	{
		root = new Node(game,null);
		latestPosition = root;
		isDemoMode = demoMode;
	}
	
	@Override
	public void run()
	{
		while(!Thread.currentThread().isInterrupted())
		{
			Node nextNode = latestPosition;
			while(nextNode.getState().getWinningPlayer() == null)
			{
				if(nextNode.getChildren().size() == 0)
				{
					expandNode(nextNode);
					if(nextNode.getChildren().size() == 0)
					{
						break;
					}
				}
				ArrayList<Node> childrenToChooseFrom = new ArrayList<>();
				double highestUCTValue = Double.MIN_VALUE;
				for(Entry<Action,Node> child : nextNode.getChildren().entrySet())
				{
					double myUCTValue = child.getValue().calculateUpperConfidenceBound(child.getKey().getCommand().getID());
					if(myUCTValue == highestUCTValue)
					{
						childrenToChooseFrom.add(child.getValue());
					}
					else if(myUCTValue > highestUCTValue)
					{
						childrenToChooseFrom = new ArrayList<>();
						childrenToChooseFrom.add(child.getValue());
						highestUCTValue = myUCTValue;
					}
				}
				int nextChildIndex = ThreadLocalRandom.current().nextInt(childrenToChooseFrom.size());
				nextNode = childrenToChooseFrom.get(nextChildIndex);
			}
			
			Player winningPlayer = nextNode.getState().getWinningPlayer();
			nextNode.recordWinFor(winningPlayer==null? -1 : winningPlayer.getID());
		}
	}
	
	public void updateCurrentGameState(GameState currentState)
	{
		latestPosition = new Node(currentState,null);
	}
	
	public Action getBestMoveForPlayer(int playerNumber)
	{
		double highestAverageWinRate = Double.MIN_VALUE;
		long highestNumPlayouts = Long.MIN_VALUE;
		ArrayList<Action> possibleMoves = new ArrayList<>();
		for(Entry<Action,Node> child : latestPosition.getChildren().entrySet())
		{
			double myAverageWinRate = child.getValue().calculateAverageWinRateFor(playerNumber);
			long myNumPlayouts = child.getValue().getNumPlayouts();
			if(myAverageWinRate == highestAverageWinRate)
			{
				if(myNumPlayouts == highestNumPlayouts)
				{
					possibleMoves.add(child.getKey());
				}
				else if(myNumPlayouts > highestNumPlayouts)
				{
					possibleMoves = new ArrayList<>();
					possibleMoves.add(child.getKey());
					highestNumPlayouts = myNumPlayouts;
				}
			}
			else if(myAverageWinRate > highestAverageWinRate)
			{
				possibleMoves = new ArrayList<>();
				possibleMoves.add(child.getKey());
				highestAverageWinRate = myAverageWinRate;
				highestNumPlayouts = myNumPlayouts;
			}
		}
		
		return possibleMoves.get(ThreadLocalRandom.current().nextInt(possibleMoves.size()));
	}
	
	private void expandNode(Node n)
	{
		for(Entry<Action,GameState> e : PossibleMoveGenerator.getAllPossibleActionsFromState(isDemoMode, n.getState()).entrySet())
		{
			Node child = new Node(e.getValue(),n);
			n.addChild(e.getKey(), child);
		}
	}
}

package server.logic.ai;

import java.util.HashMap;

import server.event.GameStarted;
import server.event.internal.ApplyHitsCommand;
import server.logic.game.GameState;
import server.logic.game.handlers.CombatCommandHandler;
import common.Constants.CombatPhase;
import common.game.ITileProperties;
import common.game.Player;

public class MonteCarloTreeSearch extends Thread
{
	private final int playerNumber;
	private Node root;
	private volatile Node latestPosition;
	private final boolean isDemoMode;
	
	public MonteCarloTreeSearch(boolean demoMode, int playerNumber, GameState game)
	{
		this.playerNumber = playerNumber;
		root = new Node(game,null);
		latestPosition = root;
		isDemoMode = demoMode;
	}
	
	@Override
	public void run()
	{
		while(!Thread.currentThread().isInterrupted())
		{
			if(latestPosition.getChildren().size() == 0)
			{
				//TODO fully expand node
				
			}
			else
			{
				//TODO select best child (UCT value)
			}
		}
	}
	
	private HashMap<Action,GameState> successorFunction(GameState state)
	{
		HashMap<Action,GameState> possibleMoves = new HashMap<Action,GameState>();
		
		if(state.getCombatHex() != null)
		{
			for(Player p : state.getPlayersStillFightingInCombatHex())
			{
				int hitsToApply = state.getHitsOnPlayer(p.getID());
				if(hitsToApply>0 && (state.getCurrentCombatPhase() == CombatPhase.APPLY_MAGIC_HITS || state.getCurrentCombatPhase() == CombatPhase.APPLY_MELEE_HITS
						|| state.getCurrentCombatPhase() == CombatPhase.APPLY_RANGED_HITS))
				{
					for(ITileProperties tp : state.getCombatHex().getFightingThingsInHex())
					{
						if(p.ownsThingOnBoard(tp))
						{
							try
							{
								GameState clonedState = state.clone();
								ApplyHitsCommand command = new ApplyHitsCommand(1, tp);
								command.setID(p.getID());
								CombatCommandHandler handler = new CombatCommandHandler();
								handler.receiveGameStartedEvent(new GameStarted(isDemoMode,clonedState));
								handler.receiveApplyHitsCommand(command);
								possibleMoves.put(new Action(command), clonedState);
							}
							catch(Throwable t)
							{
								//invalid move
							}
						}
					}
				}
			}
		}
		//TODO generate rest of actions
		return possibleMoves;
	}
}

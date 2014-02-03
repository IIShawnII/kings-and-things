package common.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Constants.SetupPhase;

/**
 * GameState can be described by the board and player info
 */
public class GameState
{
	private final HexBoard board;
	private final HashSet<Player> players;
	private final ArrayList<Integer> playerOrder;
	private final SetupPhase currentSetupPhase;
	private final int activePhasePlayerNumber;
	private final int activeTurnPlayerNumber;

	public GameState(HexBoard board, Set<Player> players, List<Integer> playerOrder, SetupPhase currentSetupPhase, int activeTurnPlayerNumber, int activePhasePlayerNumber)
	{
		this.board = board;
		this.players = new HashSet<Player>(players);
		this.playerOrder = new ArrayList<Integer>(playerOrder);
		this.currentSetupPhase = currentSetupPhase;
		this.activePhasePlayerNumber = activePhasePlayerNumber;
		this.activeTurnPlayerNumber = activeTurnPlayerNumber;
	}
	
	public HexBoard getBoard()
	{
		return board;
	}
	
	public Set<Player> getPlayers()
	{
		return Collections.unmodifiableSet(players);
	}
	
	public List<Integer> getPlayerOrder()
	{
		return Collections.unmodifiableList(playerOrder);
	}
	
	public SetupPhase getCurrentSetupPhase()
	{
		return currentSetupPhase;
	}
	
	public Player getActivePhasePlayer()
	{
		return getPlayerByPlayerNumber(activePhasePlayerNumber);
	}

	public Player getActiveTurnPlayer()
	{
		return getPlayerByPlayerNumber(activeTurnPlayerNumber);
	}
	
	public Player getPlayerByPlayerNumber(int playerNumber)
	{
		for(Player p : getPlayers())
		{
			if(p.getPlayerNumber() == playerNumber)
			{
				return p;
			}
		}
		
		throw new IllegalArgumentException("There is no player with number: " + playerNumber);
	}
}

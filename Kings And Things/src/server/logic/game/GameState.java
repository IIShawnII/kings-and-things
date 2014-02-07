package server.logic.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Player;
import common.Constants.RegularPhase;
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
	private final RegularPhase currentRegularPhase;
	private final int activePhasePlayerNumber;
	private final int activeTurnPlayerNumber;

	/**
	 * Creates a new GameState object
	 * @param board The current game board
	 * @param players The set of players playing the game
	 * @param playerOrder A list of player ids in the order in which players will take turns
	 * @param currentSetupPhase The current setup phase
	 * @param activeTurnPlayerNumber The player id of the player who's turn it is
	 * @param activePhasePlayerNumber The player id of the next player to act in the current phase
	 */
	public GameState(HexBoard board, Set<Player> players, List<Integer> playerOrder, SetupPhase currentSetupPhase, RegularPhase currentRegularPhase, int activeTurnPlayerNumber, int activePhasePlayerNumber)
	{
		this.board = board;
		this.players = new HashSet<Player>(players);
		this.playerOrder = new ArrayList<Integer>(playerOrder);
		this.currentSetupPhase = currentSetupPhase;
		this.currentRegularPhase = currentRegularPhase;
		this.activePhasePlayerNumber = activePhasePlayerNumber;
		this.activeTurnPlayerNumber = activeTurnPlayerNumber;
	}
	
	/**
	 * Gets the current board
	 * @return The game board
	 */
	public HexBoard getBoard()
	{
		return board;
	}
	
	/**
	 * Get the set of players currently playing the game
	 * @return The players of the game
	 */
	public Set<Player> getPlayers()
	{
		return Collections.unmodifiableSet(players);
	}
	
	/**
	 * Get a list of player ids indicating the player order
	 * @return List indicating player order
	 */
	public List<Integer> getPlayerOrder()
	{
		return Collections.unmodifiableList(playerOrder);
	}
	
	/**
	 * Get the current setup phase of the game
	 * @return The setup phase of the game
	 */
	public SetupPhase getCurrentSetupPhase()
	{
		return currentSetupPhase;
	}
	
	/**
	 * Get the current regular phase of the game
	 * @return The regular phase of the game
	 */
	public RegularPhase getCurrentRegularPhase()
	{
		return currentRegularPhase;
	}
	
	/**
	 * Get the player who needs to move next for the current
	 * phase
	 * @return The player who needs to move next for this phase
	 */
	public Player getActivePhasePlayer()
	{
		return getPlayerByPlayerNumber(activePhasePlayerNumber);
	}

	/**
	 * Get the player who's turn it is
	 * @return The player who's turn it is
	 */
	public Player getActiveTurnPlayer()
	{
		return getPlayerByPlayerNumber(activeTurnPlayerNumber);
	}
	
	/**
	 * Given a player id, find the player with that id
	 * @param playerNumber The player id to find
	 * @return The player with the specified id
	 * @throws IllegalArgumentException if playerNumber can
	 * not be found
	 */
	public Player getPlayerByPlayerNumber(int playerNumber)
	{
		for(Player p : getPlayers())
		{
			if(p.getID() == playerNumber)
			{
				return p;
			}
		}
		
		throw new IllegalArgumentException("There is no player with number: " + playerNumber);
	}
}

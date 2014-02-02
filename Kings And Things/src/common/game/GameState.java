package common.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * GameState can be described by the board and player info
 */
public class GameState
{
	private final HexBoard board;
	private final HashSet<Player> players;
	
	public GameState(HexBoard board)
	{
		this(board,new HashSet<Player>());
	}
	
	public GameState(HexBoard board, HashSet<Player> players)
	{
		this.board = board;
		this.players = players;
	}
	
	public HexBoard getBoard()
	{
		return board;
	}
	
	public Set<Player> getPlayers()
	{
		return Collections.unmodifiableSet(players);
	}
}

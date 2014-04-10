package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.Player;

public class PlayerWon extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = -7264737226967745265L;
	private final Player winner;
	
	public PlayerWon(Player winner)
	{
		this.winner = winner;
	}
	
	public Player getWinner()
	{
		return winner;
	}
}

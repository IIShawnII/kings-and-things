package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.Player;

public class PlayerTargetChanged extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = -6135997646442697916L;
	private final Player playerTargetting;
	private final Player playersTarget;
	
	public PlayerTargetChanged(Player playerTargetting, Player playersTarget)
	{
		this.playersTarget = playersTarget;
		this.playerTargetting = playerTargetting;
	}
	
	public Player getTargettingPlayer()
	{
		return playerTargetting;
	}
	
	public Player getPlayersTarget()
	{
		return playersTarget;
	}
}

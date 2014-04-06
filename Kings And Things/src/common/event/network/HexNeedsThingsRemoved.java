package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;
import common.game.Player;

public class HexNeedsThingsRemoved extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = 8824684819932935335L;
	private final HexState hex;
	private final int count;
	private final boolean isFirstCall;
	private final Player p;
	
	public HexNeedsThingsRemoved(HexState hs, int numToRemove, boolean firstNotification, Player playerRemovingThings)
	{
		hex = hs;
		count = numToRemove;
		isFirstCall = firstNotification;
		this.p = playerRemovingThings;
	}
	
	public HexState getHex()
	{
		return hex;
	}
	
	public int getNumToRemove()
	{
		return count;
	}
	
	public boolean isFirstNotificationForThisHex()
	{
		return isFirstCall;
	}
	
	public Player getPlayerRemovingThings()
	{
		return p;
	}
}

package common.event.notifications;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;

public class HexOwnershipChanged extends AbstractNetwrokEvent {
	private static final long serialVersionUID = -8427885692049391183L;
	
	private final HexState hex;
	private final int playerID;
	
	public HexOwnershipChanged(HexState hex, int playerId)
	{
		this.hex = hex;
		this.playerID = playerId;
	}
	
	public int getPlayerId()
	{
		return playerID;
	}
	
	public HexState getChangedHex()
	{
		return hex;
	}
}

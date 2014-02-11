package common.event.notifications;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;

public class HexStatesChanged extends AbstractNetwrokEvent {
	private static final long serialVersionUID = 6608883224374660945L;

	private final HexState[] hexes;
	
	public HexStatesChanged(int count)
	{
		hexes = new HexState[count];
	}

	public HexState[] getArray(){
		return hexes;
	}
	
	@Override
	public String toString(){
		return "Network/HexPlacement: Board";
	}
}

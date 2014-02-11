package common.event.notifications;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;

public class HexPlacement extends AbstractNetwrokEvent {
	
	private static final long serialVersionUID = -6453752681154716711L;
	
	private HexState[] hexes; 
	
	public HexPlacement( int boardSize) {
		hexes = new HexState[boardSize];
	}

	public HexState[] getArray(){
		return hexes;
	}
	
	@Override
	public String toString(){
		return "Network/HexPlacement: Board";
	}
}

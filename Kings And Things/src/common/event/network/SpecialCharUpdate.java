package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.ITileProperties;


public class SpecialCharUpdate extends AbstractNetwrokEvent {

	private static final long serialVersionUID = 1543083248957695508L;

	private ITileProperties[] special;
	
	public SpecialCharUpdate( int boardSize) {
		special = new ITileProperties[boardSize];
	}

	public ITileProperties[] getSpecial(){
		return special;
	}
	
	@Override
	public String toString(){
		return "Network/SpecialCharUpdate: Special Characters";
	}
}

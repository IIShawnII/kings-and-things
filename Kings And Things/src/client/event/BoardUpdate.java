package client.event;

import common.event.AbstractEvent;
import common.game.HexState;

public class BoardUpdate extends AbstractEvent {

	private HexState[] hexes;
	private boolean flipAll;
	
	public BoardUpdate( boolean flipAll){
		this.flipAll = flipAll;
		hexes = null;
	}
	
	public BoardUpdate( HexState[] array) {
		hexes = array;
	}
	
	public HexState[] getHexes(){
		return hexes;
	}
	
	public boolean hasHexes(){
		return hexes!=null;
	}
	
	public boolean flipAll(){
		return flipAll;
	}
}

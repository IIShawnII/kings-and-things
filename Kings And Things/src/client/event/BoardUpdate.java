package client.event;

import common.event.AbstractEvent;
import common.game.HexState;
import common.game.TileProperties;

public class BoardUpdate extends AbstractEvent {

	private HexState[] hexes;
	private TileProperties[] props;
	private boolean flipAll;
	private int[] list;
	
	public BoardUpdate( boolean flipAll){
		this.flipAll = flipAll;
		hexes = null;
	}
	
	public BoardUpdate( TileProperties[] array) {
		props = array;
	}
	
	public BoardUpdate( HexState[] array) {
		hexes = array;
	}
	
	public BoardUpdate( int[] list) {
		this.list = list;
	}

	public TileProperties[] getTileProperties(){
		return props;
	}

	public HexState[] getHexes(){
		return hexes;
	}

	public int[] getPlayerOrder(){
		return list;
	}
	
	public boolean hasHexes(){
		return hexes!=null;
	}
	
	public boolean flipAll(){
		return flipAll;
	}
	
	public boolean isPlayerOder(){
		return list!=null;
	}

	public boolean isRack() {
		return props!=null;
	}
}

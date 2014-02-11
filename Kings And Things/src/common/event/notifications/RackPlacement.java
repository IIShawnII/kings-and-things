package common.event.notifications;

import common.event.AbstractNetwrokEvent;
import common.game.TileProperties;

public class RackPlacement extends AbstractNetwrokEvent {
	
	private static final long serialVersionUID = 3715978233138348268L;
	
	private TileProperties[] props; 
	
	public RackPlacement( int tileCount) {
		props = new TileProperties[tileCount];
	}

	public TileProperties[] getArray(){
		return props;
	}
	
	@Override
	public String toString(){
		return "Network/RackPlacement: Counters";
	}
}

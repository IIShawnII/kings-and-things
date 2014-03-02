package common.event.notifications;

import common.event.AbstractNetwrokEvent;
import common.game.ITileProperties;

public class RackPlacement extends AbstractNetwrokEvent {
	
	private static final long serialVersionUID = 3715978233138348268L;
	
	private ITileProperties[] props; 
	
	public RackPlacement( int tileCount) {
		props = new ITileProperties[tileCount];
	}

	public ITileProperties[] getArray(){
		return props;
	}
	
	@Override
	public String toString(){
		return "Network/RackPlacement: Counters";
	}
}

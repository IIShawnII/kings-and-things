package common.event.notifications;

import common.TileProperties;
import common.event.AbstractEvent;

public final class Flip extends AbstractEvent {
	
	private static final long serialVersionUID = 2681914798988781436L;
	
	TileProperties tile = null;
	
	public boolean flipAll(){
		return tile==null;
	}
	
	public TileProperties getTile(){
		return tile;
	}
}

package common.event.notifications;

import common.TileProperties;

public final class Flip extends AbstractNotification {
	TileProperties tile = null;
	
	public boolean flipAll(){
		return tile==null;
	}
	
	public TileProperties getTile(){
		return tile;
	}
}

package common.event.notifications;

import common.TileProperties;

public final class FlipNotification extends AbstractNotification {
	TileProperties tile=  null;
	
	public FlipNotification(){
		super();
	}
	
	public boolean flipAll(){
		return tile==null;
	}
	
	public TileProperties getTile(){
		return tile;
	}
}

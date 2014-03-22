package server.event;

import java.util.Collections;
import java.util.Set;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class PlayerRemovedThingsFromHex extends AbstractInternalEvent
{
	private final ITileProperties hex;
	private final Set<ITileProperties> thingsToRemove;
	
	public PlayerRemovedThingsFromHex(ITileProperties hex, Set<ITileProperties> thingsToRemove){
		super();
		this.hex = hex;
		this.thingsToRemove = thingsToRemove;
	}

	public ITileProperties getHex(){
		return hex;
	}
	
	public Set<ITileProperties> getThingsToRemove(){
		return Collections.unmodifiableSet(thingsToRemove);
	}
}

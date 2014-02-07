package server.event.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.TileProperties;
import common.event.AbstractEvent;

public class ExchangeThingsCommand extends AbstractEvent{
	
	private final HashSet<TileProperties> things;
	
	public ExchangeThingsCommand(Collection<TileProperties> things){
		this.things = new HashSet<TileProperties>();
		for(TileProperties thing : things){
			this.things.add(thing);
		}
	}
	
	public Set<TileProperties> getThings(){
		return Collections.unmodifiableSet(things);
	}
}

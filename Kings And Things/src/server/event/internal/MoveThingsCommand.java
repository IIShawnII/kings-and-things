package server.event.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class MoveThingsCommand extends AbstractInternalEvent{
	
	private final Set<ITileProperties> things;
	private final List<ITileProperties> hexes;
	
	public MoveThingsCommand(Set<ITileProperties> things, List<ITileProperties> hexes){
		super();
		this.things = new HashSet<ITileProperties>(things);
		this.hexes = new ArrayList<ITileProperties>(hexes);
	}

	public Set<ITileProperties> getThings(){
		return Collections.unmodifiableSet(things);
	}
	
	public List<ITileProperties> getHexes(){
		return Collections.unmodifiableList(hexes);
	}
}

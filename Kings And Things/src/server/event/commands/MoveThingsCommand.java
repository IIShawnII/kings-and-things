package server.event.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.game.TileProperties;

public class MoveThingsCommand extends AbstractCommand
{
	private final Set<TileProperties> things;
	private final List<TileProperties> hexes;
	
	public MoveThingsCommand(Set<TileProperties> things, List<TileProperties> hexes)
	{
		this.things = new HashSet<TileProperties>(things);
		this.hexes = new ArrayList<TileProperties>(hexes);
	}

	public Set<TileProperties> getThings()
	{
		return Collections.unmodifiableSet(things);
	}
	
	public List<TileProperties> getHexes()
	{
		return Collections.unmodifiableList(hexes);
	}
}

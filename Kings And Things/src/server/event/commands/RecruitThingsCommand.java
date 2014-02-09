package server.event.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.game.TileProperties;


public class RecruitThingsCommand extends AbstractCommand {
	
	private final int gold;
	private final HashSet<TileProperties> things;
	
	public RecruitThingsCommand(int newGold, Collection<TileProperties> thingsToExchange) {
		this.gold = newGold;
		this.things = new HashSet<TileProperties>(thingsToExchange);
	}
	
	/**
	 * Retrieves gold
	 * @return The gold
	 */
	public int getGold () {
		return gold;
	}
	
	public Set<TileProperties> getThingsToExchange()
	{
		return Collections.unmodifiableSet(things);
	}
}

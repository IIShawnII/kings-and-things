package server.event.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.game.ITileProperties;


public class RecruitThingsCommand extends AbstractCommand {
	
	private final int gold;
	private final HashSet<ITileProperties> things;
	
	public RecruitThingsCommand(int newGold, Collection<ITileProperties> thingsToExchange) {
		this.gold = newGold;
		this.things = new HashSet<ITileProperties>(thingsToExchange);
	}
	
	/**
	 * Retrieves gold
	 * @return The gold
	 */
	public int getGold () {
		return gold;
	}
	
	public Set<ITileProperties> getThingsToExchange()
	{
		return Collections.unmodifiableSet(things);
	}
}

package server.event.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.Player;
import common.event.AbstractEvent;

public class StartGameCommand extends AbstractEvent{
	
	private static final long serialVersionUID = 5919685855315175876L;

	private final boolean demoMode;
	
	private final HashSet<Player> players;
	
	public StartGameCommand(boolean demoMode, Set<Player> players)
	{
		this.demoMode = demoMode;
		this.players = new HashSet<Player>();
		for(Player p : players)
		{
			this.players.add(p);
		}
	}
	
	public Set<Player> getPlayers()
	{
		return Collections.unmodifiableSet(players);
	}
	
	public boolean getDemoMode()
	{
		return demoMode;
	}
}

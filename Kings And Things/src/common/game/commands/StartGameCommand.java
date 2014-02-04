package common.game.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.game.Player;

@XmlRootElement
public class StartGameCommand extends Command
{
	@XmlAttribute
	private final boolean demoMode;
	@XmlElement
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

	@SuppressWarnings("unused")
	private StartGameCommand()
	{
		//required by JAXB
		demoMode = false;
		players = null;
	}
}

package common.game.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class ExchangeThingsCommand extends Command
{
	@XmlAttribute
	private final int playerNumber;
	@XmlElement
	private final HashSet<TileProperties> things;
	
	public ExchangeThingsCommand(Collection<TileProperties> things, int playerNumber)
	{
		this.playerNumber = playerNumber;
		this.things = new HashSet<TileProperties>();
		for(TileProperties thing : things)
		{
			this.things.add(thing);
		}
	}
	
	public Set<TileProperties> getThings()
	{
		return Collections.unmodifiableSet(things);
	}
	
	public int getPlayerNumber()
	{
		return playerNumber;
	}

	@SuppressWarnings("unused")
	private ExchangeThingsCommand()
	{
		//required by JAXB
		playerNumber = 0;
		things = null;
	}
}

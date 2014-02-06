package server.event.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;

@XmlRootElement
public class ExchangeThingsCommand extends Command
{
	@XmlElement
	private final HashSet<TileProperties> things;
	
	public ExchangeThingsCommand(Collection<TileProperties> things)
	{
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

	@SuppressWarnings("unused")
	private ExchangeThingsCommand()
	{
		//required by JAXB
		things = null;
	}
}

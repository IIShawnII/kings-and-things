package server.event.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class DiscardThingsCommand extends AbstractInternalEvent
{
	private final HashSet<ITileProperties> things;
	
	public DiscardThingsCommand(Collection<? extends ITileProperties> thingsToDiscard)
	{
		things = new HashSet<ITileProperties>(thingsToDiscard);
	}

	public Set<ITileProperties> getThingToDiscard()
	{
		return Collections.unmodifiableSet(things);
	}
}

package common.event.network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import common.event.AbstractNetwrokEvent;
import common.game.ITileProperties;

public class GetAvailableHeroesResponse extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = -4760591174852861807L;
	private final HashSet<ITileProperties> heroes;
	
	public GetAvailableHeroesResponse(Collection<ITileProperties> heroes)
	{
		this.heroes = new HashSet<>(heroes);
	}
	
	public Set<ITileProperties> getHeroes()
	{
		return heroes;
	}
}

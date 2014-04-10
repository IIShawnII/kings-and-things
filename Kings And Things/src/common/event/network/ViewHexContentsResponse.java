package common.event.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.Constants.HexContentsTarget;
import common.event.AbstractNetwrokEvent;
import common.game.ITileProperties;

public class ViewHexContentsResponse extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = -8249988103488987369L;
	private final HashSet<ITileProperties> thingsInHex;
	private final HexContentsTarget target;
	
	public ViewHexContentsResponse(Collection<ITileProperties> contents, HexContentsTarget target)
	{
		thingsInHex = new HashSet<ITileProperties>(contents);
		this.target = target;
	}
	
	public Set<ITileProperties> getContents()
	{
		return Collections.unmodifiableSet(thingsInHex);
	}
	
	public HexContentsTarget getTarget()
	{
		return target;
	}
}

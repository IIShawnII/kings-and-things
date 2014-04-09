package server.event.internal;

import common.Constants.HexContentsTarget;
import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ViewHexContentsCommand extends AbstractInternalEvent
{
	private final ITileProperties hex;
	private final HexContentsTarget target;
	
	public ViewHexContentsCommand(ITileProperties hex, HexContentsTarget target)
	{
		this.hex = hex;
		this.target = target;
	}
	
	public ITileProperties getHex()
	{
		return hex;
	}
	
	public HexContentsTarget getTarget()
	{
		return target;
	}
}

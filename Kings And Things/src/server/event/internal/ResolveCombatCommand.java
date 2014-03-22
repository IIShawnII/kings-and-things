package server.event.internal;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ResolveCombatCommand extends AbstractInternalEvent{
	
	private final ITileProperties hex;

	public ResolveCombatCommand(ITileProperties hex){
		super();
		this.hex = hex;
	}

	public ITileProperties getCombatHex(){
		return hex;
	}
}

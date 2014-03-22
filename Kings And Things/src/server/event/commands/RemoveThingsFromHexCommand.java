package server.event.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class RemoveThingsFromHexCommand extends AbstractInternalEvent {
	
	private final ITileProperties hexToRemoveSomethingFrom;
	private final HashSet<ITileProperties> thingsToRemove;
	
	// constructor
	public RemoveThingsFromHexCommand( ITileProperties hexToRemoveSomethingFrom, HashSet<ITileProperties> thingsToRemove){
		super();
		this.hexToRemoveSomethingFrom = hexToRemoveSomethingFrom;
		this.thingsToRemove = thingsToRemove;
	}
	
	/*Getter Methods*/
	
	/**
	 * Retrieves things to remove hexes from
	 */
	public ITileProperties getHexToRemoveSomethingFrom(){
		return hexToRemoveSomethingFrom;
	}
	
	/**
	 * Retrieves things to remove
	 */
	public Set<ITileProperties> getThingsToRemove(){
		return Collections.unmodifiableSet(thingsToRemove);
	}
}

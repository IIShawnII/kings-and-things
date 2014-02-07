package server.event.commands;

import common.event.AbstractEvent;


public class PaidRecruitsCommand extends AbstractEvent {
	
	private int gold;
	
	public PaidRecruitsCommand(int newGold) {
		this.gold = newGold;
	}
	
	/**
	 * Retrieves gold
	 * @return The gold
	 */
	public int getGold () {
		return gold;
	}
}

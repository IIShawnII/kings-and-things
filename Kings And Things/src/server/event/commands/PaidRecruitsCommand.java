package server.event.commands;

import common.event.AbstractEvent;


public class PaidRecruitsCommand extends AbstractEvent {

	private static final long serialVersionUID = -8959418919090060123L;
	
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

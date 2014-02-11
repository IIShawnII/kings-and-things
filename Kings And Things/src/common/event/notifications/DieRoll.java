package common.event.notifications;

import common.event.AbstractNetwrokEvent;
import common.game.Roll;

public class DieRoll extends AbstractNetwrokEvent {
	
	private static final long serialVersionUID = 1732457814465837071L;
	private Roll dieRoll;
	
	// constructor
	public DieRoll(Roll dieRoll) {
		this.dieRoll = dieRoll;
	}
	
	
	/*Getter Methods*/
	
	/*
	 * Retrieves the die roll to notify the client
	 */
	public Roll getDieRoll() {
		return dieRoll;
	}
	
}

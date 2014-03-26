package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.Roll;

public class DieRoll extends AbstractNetwrokEvent {
	
	private static final long serialVersionUID = 1732457814465837071L;
	private Roll roll;
	
	public DieRoll(Roll roll) {
		this.roll = roll;
	}
	
	/**
	 * Retrieves the die roll to notify the client
	 */
	public Roll getDieRoll() {
		return roll;
	}
	
	@Override
	public String toString(){
		return "Network/DieRoll: " + roll;
	}
}

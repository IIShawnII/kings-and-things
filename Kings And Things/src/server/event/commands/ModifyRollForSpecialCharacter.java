package server.event.commands;

import common.event.AbstractCommand;

public class ModifyRollForSpecialCharacter extends AbstractCommand {
	
	private final boolean hasRolled; 
	private final int goldAmount;
	
	public ModifyRollForSpecialCharacter (boolean hasRolled, int goldAmount) {
		this.hasRolled = hasRolled;
		this.goldAmount = goldAmount;
	}
	
	/*Getter Methods*/
	
	// checks if the player has rolled
	public boolean checkRolled () {
		return this.hasRolled;
	}
	
	// gets the amount of gold the player wants to pay
	public int retrieveGoldAmount () {
		return this.goldAmount;
	}

}

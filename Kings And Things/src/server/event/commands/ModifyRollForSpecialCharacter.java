package server.event.commands;

import common.event.AbstractInternalEvent;

public class ModifyRollForSpecialCharacter extends AbstractInternalEvent {
	
	private final boolean hasRolled; 
	private final int goldAmount;
	
	public ModifyRollForSpecialCharacter (boolean hasRolled, int goldAmount, final Object OWNER){
		super( OWNER);
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

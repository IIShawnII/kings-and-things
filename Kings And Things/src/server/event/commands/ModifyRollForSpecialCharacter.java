package server.event.commands;

import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ModifyRollForSpecialCharacter extends AbstractInternalEvent {
	
	private final int goldAmount;
	private final ITileProperties target;
	
	public ModifyRollForSpecialCharacter (int goldAmount, ITileProperties target){
		super();
		this.goldAmount = goldAmount;
		this.target = target;
	}
	
	/*Getter Methods*/
	
	// gets the amount of gold the player wants to pay
	public int retrieveGoldAmount(){
		return this.goldAmount;
	}

	public ITileProperties getTarget(){
		return target;
	}
}

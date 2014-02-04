package common.game.commands;

public class PaidRecruitsCommand extends Command {
	private int gold;
	private int playerNumber;
	
	public PaidRecruitsCommand(int newGold, int newPlayerNumber) {
		this.gold = newGold;
		this.playerNumber = newPlayerNumber;
	}
	
	// Getter Methods
	
	/*
	 * Retrieves gold
	 */
	public int getGold () {
		return gold;
	}
	
	/*
	 * Retrieves player number
	 */
	public int getplayerNumber () {
		return playerNumber;
	}
	
	
}

package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PaidRecruitsCommand extends Command {
	@XmlAttribute
	private int gold;
	@XmlAttribute
	private int playerNumber;
	
	public PaidRecruitsCommand(int newGold, int newPlayerNumber) {
		this.gold = newGold;
		this.playerNumber = newPlayerNumber;
	}
	
	// Getter Methods
	
	/**
	 * Retrieves gold
	 * @return The gold
	 */
	public int getGold () {
		return gold;
	}
	
	/**
	 * Retrieves player number
	 * @return The player number
	 */
	public int getplayerNumber () {
		return playerNumber;
	}
	
	
}

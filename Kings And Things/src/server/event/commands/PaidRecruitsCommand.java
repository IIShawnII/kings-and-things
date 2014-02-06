package server.event.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PaidRecruitsCommand extends AbstractCommand {
	@XmlAttribute
	private int gold;
	
	public PaidRecruitsCommand(int newGold) {
		this.gold = newGold;
	}
	
	// Getter Methods
	
	/**
	 * Retrieves gold
	 * @return The gold
	 */
	public int getGold () {
		return gold;
	}
}

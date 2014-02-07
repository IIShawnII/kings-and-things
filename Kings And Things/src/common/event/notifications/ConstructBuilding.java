package common.event.notifications;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;
import common.Constants.BuildableBuilding;
import common.event.AbstractEvent;

@XmlRootElement
public class ConstructBuilding extends AbstractEvent{
	
	@XmlElement
	private final BuildableBuilding building;
	@XmlAttribute
	private final int playerNumber;
	@XmlElement
	private final TileProperties hex;
	
	public ConstructBuilding(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		this.building = building;
		this.hex = hex;
		this.playerNumber = playerNumber;
	}
	
	public BuildableBuilding getBuilding()
	{
		return building;
	}
	
	public TileProperties getHex()
	{
		return hex;
	}
	
	public int getPlayerNumber()
	{
		return playerNumber;
	}
	
	@SuppressWarnings("unused")
	private ConstructBuilding()
	{
		//required by JAXB
		building = null;
		hex = null;
		playerNumber = 0;
	}
}

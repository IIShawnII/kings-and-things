package common.game.notifications;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.TileProperties;
import common.Constants.BuildableBuilding;

@XmlRootElement
public class ConstructBuildingNotification extends Notification
{
	@XmlElement
	private final BuildableBuilding building;
	@XmlAttribute
	private final int playerNumber;
	@XmlElement
	private final TileProperties hex;
	
	public ConstructBuildingNotification(BuildableBuilding building, int playerNumber, TileProperties hex)
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
	private ConstructBuildingNotification()
	{
		//required by JAXB
		building = null;
		hex = null;
		playerNumber = 0;
	}
}

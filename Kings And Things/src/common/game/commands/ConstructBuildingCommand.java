package common.game.commands;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.Constants.BuildableBuilding;
import common.TileProperties;

@XmlRootElement
public class ConstructBuildingCommand extends Command
{
	@XmlElement
	private final BuildableBuilding building;
	@XmlAttribute
	private final int playerNumber;
	@XmlElement
	private final TileProperties hex;
	
	public ConstructBuildingCommand(BuildableBuilding building, int playerNumber, TileProperties hex)
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
	private ConstructBuildingCommand()
	{
		//required by JAXB
		building = null;
		hex = null;
		playerNumber = 0;
	}
}

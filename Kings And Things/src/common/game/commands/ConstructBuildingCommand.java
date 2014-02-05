package common.game.commands;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import common.Constants.BuildableBuilding;
import common.TileProperties;

@XmlRootElement
public class ConstructBuildingCommand extends Command
{
	@XmlElement
	private final BuildableBuilding building;
	@XmlElement
	private final TileProperties hex;
	
	public ConstructBuildingCommand(BuildableBuilding building, TileProperties hex)
	{
		this.building = building;
		this.hex = hex;
	}
	
	public BuildableBuilding getBuilding()
	{
		return building;
	}
	
	public TileProperties getHex()
	{
		return hex;
	}
	
	@SuppressWarnings("unused")
	private ConstructBuildingCommand()
	{
		//required by JAXB
		building = null;
		hex = null;
	}
}

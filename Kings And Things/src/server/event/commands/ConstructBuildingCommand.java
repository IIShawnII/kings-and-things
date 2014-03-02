package server.event.commands;

import common.Constants.BuildableBuilding;
import common.game.ITileProperties;

public class ConstructBuildingCommand extends AbstractCommand{
	
	private final BuildableBuilding building;
	private final ITileProperties hex;
	
	public ConstructBuildingCommand(BuildableBuilding building, ITileProperties hex){
		this.building = building;
		this.hex = hex;
	}
	
	public BuildableBuilding getBuilding(){
		return building;
	}
	
	public ITileProperties getHex(){
		return hex;
	}
}

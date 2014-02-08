package server.event.commands;

import common.Constants.BuildableBuilding;
import common.event.AbstractEvent;
import common.game.TileProperties;

public class ConstructBuildingCommand extends AbstractEvent{
	
	private final BuildableBuilding building;
	private final TileProperties hex;
	
	public ConstructBuildingCommand(BuildableBuilding building, TileProperties hex){
		this.building = building;
		this.hex = hex;
	}
	
	public BuildableBuilding getBuilding(){
		return building;
	}
	
	public TileProperties getHex(){
		return hex;
	}
}

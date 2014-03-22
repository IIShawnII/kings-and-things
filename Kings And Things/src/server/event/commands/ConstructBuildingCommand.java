package server.event.commands;

import common.Constants.BuildableBuilding;
import common.event.AbstractInternalEvent;
import common.game.ITileProperties;

public class ConstructBuildingCommand extends AbstractInternalEvent{
	
	private final BuildableBuilding building;
	private final ITileProperties hex;
	
	public ConstructBuildingCommand(BuildableBuilding building, ITileProperties hex){
		super();
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

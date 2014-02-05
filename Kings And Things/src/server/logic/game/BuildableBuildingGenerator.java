package server.logic.game;

import common.Constants;
import common.Constants.BuildableBuilding;
import common.TileProperties;

abstract class BuildableBuildingGenerator
{
	private static int id = 2;
	
	public static TileProperties createBuildingTileForType(BuildableBuilding building)
	{
		TileProperties baseBuilding = null;
		
		for(TileProperties tp : Constants.BUILDING.values())
		{
			if(tp.getName().equals(building.name()))
			{
				baseBuilding = tp;
				break;
			}
		}
		
		if(baseBuilding == null)
		{
			throw new IllegalArgumentException("No building tiles found for type: " + building);
		}
		
		return new TileProperties(baseBuilding,id++);
	}
}

package server.logic.game;

import common.Constants;
import common.Constants.BuildableBuilding;
import common.game.ITileProperties;
import common.game.TileProperties;

public abstract class BuildableBuildingGenerator
{
	private static int id = 2;
	
	public static ITileProperties createBuildingTileForType(BuildableBuilding building)
	{
		ITileProperties baseBuilding = null;
		
		for(ITileProperties tp : Constants.BUILDING.values())
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
		
		return new TileProperties((TileProperties) baseBuilding,id++);
	}
}

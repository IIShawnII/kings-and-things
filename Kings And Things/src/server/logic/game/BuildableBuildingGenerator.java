package server.logic.game;

import common.Constants;
import common.Constants.Ability;
import common.Constants.BuildableBuilding;
import common.game.ITileProperties;
import common.game.TileProperties;
import common.game.TwoSidedTileProperties;

public abstract class BuildableBuildingGenerator
{
	private static int id = 2;
	
	public static ITileProperties createBuildingTileForType(BuildableBuilding building)
	{
		TileProperties buildingFaceUp = null;
		TileProperties buildingFaceDown = null;
		
		for(TileProperties tp : Constants.BUILDING.values())
		{
			if(tp.getName().equals(building.name()))
			{
				if(tp.hasAbility(Ability.Neutralised))
				{
					buildingFaceDown = tp;
				}
				else
				{
					buildingFaceUp = tp;
				}
			}
		}
		
		if(buildingFaceUp == null || buildingFaceDown == null)
		{
			throw new IllegalArgumentException("No building tiles found for type: " + building);
		}
		
		return new TwoSidedTileProperties(new TileProperties(buildingFaceUp,id++),new TileProperties(buildingFaceUp,id++));
	}
}

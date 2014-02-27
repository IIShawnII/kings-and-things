package server.logic.game;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import common.Constants;
import common.game.TileProperties;
import common.game.TwoSidedTileProperties;

public class SpecialCharacterManager extends AbstractTileManager
{
	protected SpecialCharacterManager(Collection<? extends TileProperties> tiles)
	{
		super(getSpecialCharacterSetFromCollection(tiles), "Special Character");
	}

	private static Set<TwoSidedTileProperties> getSpecialCharacterSetFromCollection(Collection<? extends TileProperties> tiles)
	{
		LinkedHashSet<TwoSidedTileProperties> heroes = new LinkedHashSet<TwoSidedTileProperties>();
		for(TileProperties tp : tiles)
		{
			if(Constants.HERO_PAIRINGS.containsKey(tp.getName()))
			{
				TileProperties correspondingTile = null;
				for(TileProperties tp2 : tiles)
				{
					if(tp2.getName().equals(Constants.HERO_PAIRINGS.get(tp.getName())))
					{
						correspondingTile = tp2;
						break;
					}
				}
				heroes.add(new TwoSidedTileProperties(tp,correspondingTile));
			}
		}
		
		return heroes;
	}
}

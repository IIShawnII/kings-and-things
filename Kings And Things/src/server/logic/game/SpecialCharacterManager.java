package server.logic.game;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.game.TileProperties;
import common.game.TwoSidedTileProperties;

public class SpecialCharacterManager extends AbstractTileManager
{
	protected SpecialCharacterManager(Collection<? extends TileProperties> tiles)
	{
		super(getSpecialCharacterSetFromCollection(tiles), "special character");
	}
	
	public TwoSidedTileProperties drawTileByName(String heroName) throws NoMoreTilesException
	{
		TileProperties tileToDraw = null;
		for(TileProperties tp : tiles)
		{
			if(tp.getName().equals(heroName))
			{
				tileToDraw = tp;
			}
		}
		if(tileToDraw==null || !tiles.remove(tileToDraw))
		{
			throw new NoMoreTilesException("Unable to draw special character named: " + heroName + ", because there are no more tiles with that name.");
		}
		
		return (TwoSidedTileProperties) tileToDraw;
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
		
		for(TwoSidedTileProperties tp : heroes)
		{
			if(Math.random()<0.5d)
			{
				tp.flip();
			}
		}
		
		return heroes;
	}
}

package server.logic.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.game.ITileProperties;
import common.game.TileProperties;
import common.game.TwoSidedTileProperties;

public class SpecialCharacterManager extends AbstractTileManager
{
	public SpecialCharacterManager(boolean demoMode)
	{
		super(getSpecialCharacterSetFromCollection(Constants.SPECIAL.values(), demoMode), "special character");
	}
	
	public SpecialCharacterManager(SpecialCharacterManager other)
	{
		super(Constants.deepCloneCollection(other.tiles,new ArrayList<ITileProperties>()),"special character");
	}
	
	@Override
	public SpecialCharacterManager clone()
	{
		return new SpecialCharacterManager(this);
	}
	
	public TwoSidedTileProperties drawTileByName(String heroName) throws NoMoreTilesException
	{
		ITileProperties tileToDraw = null;
		for(ITileProperties tp : tiles)
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
	
	public boolean heroIsAvailable(String heroName)
	{
		for(ITileProperties tp : tiles)
		{
			if(tp.getName().equals(heroName))
			{
				return true;
			}
		}
		return false;
	}
	
	public List<ITileProperties> getAvailableHeroes()
	{
		return Collections.unmodifiableList(tiles);
	}

	private static Set<TwoSidedTileProperties> getSpecialCharacterSetFromCollection(Collection<? extends TileProperties> tiles, boolean demoMode)
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
		
		if(!demoMode)
		{
			for(TwoSidedTileProperties tp : heroes)
			{
				if(Math.random()<0.5d)
				{
					tp.flip();
				}
			}
		}
		
		return heroes;
	}
}

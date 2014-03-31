package server.logic.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import server.logic.exceptions.NoMoreTilesException;
import common.game.ITileProperties;


/**
 * this class encapsulates the logic of drawing tiles from somewhere, and placing
 * previously drawn ones back.
 */
public abstract class AbstractTileManager implements Serializable
{
	private static final long serialVersionUID = 2365752916758745643L;
	
	protected final ArrayList<ITileProperties> tiles;
	private final String tileType;

	/**
	 * Create new AbstractTileManager.
	 * @param tiles Our list of available tiles
	 * @param tileType A string indicated the kind of tiles we're managing, for exceptions
	 */
	protected AbstractTileManager(Collection<? extends ITileProperties> tiles, String tileType)
	{
		this.tiles = new ArrayList<ITileProperties>(tiles);
		this.tileType = tileType;
	}

	/**
	 * Call this method to draw a tile from the list of tiles.
	 * @return A tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	public ITileProperties drawTile() throws NoMoreTilesException
	{
		synchronized(tiles)
		{
			if(tiles.size()==0)
			{
				throw new NoMoreTilesException("Unable to draw "+ tileType +" tile because there are no more tiles.");
			}
			//draw a random tile
			int index = (int) Math.round(Math.random() * (tiles.size() - 1));
			return tiles.remove(index);
		}
	}

	/**
	 * Use this method to re-add a previously drawn out tile.
	 * @param tile The tile to add back in.
	 * @throws IllegalArgumentException if tile is null.
	 */
	public void reInsertTile(ITileProperties tile)
	{
		if(tile==null)
		{
			throw new IllegalArgumentException("Can not insert null "+ tileType +" tile.");
		}
		synchronized(tiles)
		{
			tiles.add(tile);
		}
	}
}

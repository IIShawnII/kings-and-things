package server.logic.game;

import java.util.ArrayList;
import java.util.Collection;

import server.logic.exceptions.NoMoreTilesException;
import common.game.TileProperties;


/**
 * this class encapsulates the logic of drawing tiles from somewhere, and placing
 * previously drawn ones back.
 */
abstract class AbstractTileManager
{
	protected final ArrayList<TileProperties> tiles;
	private final String tileType;

	/**
	 * Create new AbstractTileManager.
	 * @param tiles Our list of available tiles
	 * @param tileType A string indicated the kind of tiles we're managing, for exceptions
	 */
	protected AbstractTileManager(Collection<? extends TileProperties> tiles, String tileType)
	{
		this.tiles = new ArrayList<TileProperties>(tiles);
		this.tileType = tileType;
	}

	/**
	 * Call this method to draw a tile from the list of tiles.
	 * @return A tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	public TileProperties drawTile() throws NoMoreTilesException
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
	public void reInsertTile(TileProperties tile)
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

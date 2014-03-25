package server.logic.game;

import java.util.ArrayList;
import java.util.Iterator;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.Constants.Biome;
import common.game.ITileProperties;

/**
 * this class encapsulates the logic of drawing hex tiles from the bank, and placing
 * previously drawn ones back into the bank.
 */
public class HexTileManager extends AbstractTileManager
{
	private final boolean isDemoMode;
	private int numDraws;
	
	/**
	 * Create new HexTileManager.
	 * @param isDemoMode Set to true if we should stack the deck of hex tiles
	 * to match the demo script board.
	 */
	public HexTileManager(boolean isDemoMode)
	{
		super(Constants.HEX.values(), "hex");
		this.isDemoMode = isDemoMode;
		numDraws = 0;
	}
	
	public HexTileManager(HexTileManager other)
	{
		super(Constants.deepCloneCollection(other.tiles,new ArrayList<ITileProperties>()),"hex");
		isDemoMode = other.isDemoMode;
		numDraws = other.numDraws;
	}
	
	@Override
	public HexTileManager clone()
	{
		return new HexTileManager(this);
	}
	
	/**
	 * Call this method to draw a hex tile from the bank.
	 * @return A hex tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	@Override
	public ITileProperties drawTile() throws NoMoreTilesException
	{
		if(!isDemoMode)
		{
			return super.drawTile();
		}
		else
		{
			synchronized(tiles)
			{
				//In demo mode we stack the deck to match the test script
				switch(numDraws)
				{
					case 0:
					case 8:
					case 10:
					case 27:
					case 35:
						numDraws++;
						return drawHexTileByType(Biome.Frozen_Waste);
					case 1:
					case 5:
					case 14:
					case 24:
					case 26:
					case 34:
						numDraws++;
						return drawHexTileByType(Biome.Forest);
					case 2:
					case 18:
					case 21:
					case 28:
					case 32:
						numDraws++;
						return drawHexTileByType(Biome.Jungle);
					case 3:
					case 7:
					case 16:
					case 25:
					case 31:
						numDraws++;
						return drawHexTileByType(Biome.Plains);
					case 4:
						numDraws++;
						return drawHexTileByType(Biome.Sea);
					case 6:
					case 11:
					case 13:
					case 19:
					case 22:
						numDraws++;
						return drawHexTileByType(Biome.Swamp);
					case 9:
					case 17:
					case 20:
					case 29:
					case 33:
						numDraws++;
						return drawHexTileByType(Biome.Mountain);
					case 12:
					case 15:
					case 23:
					case 30:
					case 36:
						numDraws++;
						return drawHexTileByType(Biome.Desert);
					default:
						throw new NoMoreTilesException("A hex tile could not be drawn because there are no more available.");
				}
			}
		}
	}
	
	/**
	 * Call this method to draw a particular type of hex tile from the bank
	 * @param hexType The type of hex to draw
	 * @return A hex tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	public ITileProperties drawHexTileByType(Biome hexType) throws NoMoreTilesException
	{
		synchronized(tiles)
		{
			Iterator<ITileProperties> it = tiles.iterator();
			while(it.hasNext())
			{
				ITileProperties hex = it.next();
				if(hex.getName().equals(hexType.name()))
				{
					it.remove();
					return hex;
				}
			}
			
			throw new NoMoreTilesException("Unable to draw hex tile of type: " + hexType + ", because there are no more tiles of that type.");
		}
	}
	

	/**
	 * Use this method to re-add a previously drawn out tile.
	 * @param tile The tile to add back in.
	 * @throws IllegalArgumentException if tile is null or is not a hex
	 */
	@Override
	public void reInsertTile(ITileProperties tile)
	{
		if(tile==null)
		{
			throw new IllegalArgumentException("Can not insert null hex tile.");
		}
		if(!tile.isHexTile())
		{
			throw new IllegalArgumentException("Can not insert non hex tile.");
		}
		super.reInsertTile(tile);
	}
}

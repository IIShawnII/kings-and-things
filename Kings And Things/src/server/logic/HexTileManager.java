package server.logic;

import java.util.ArrayList;
import java.util.Iterator;

import server.exceptions.NoMoreTilesException;
import common.Constants;
import common.Constants.Biome;
import common.TileProperties;

/**
 * this class encapsulates the logic of drawing hex tiles from the bank, and placing
 * previously drawn ones back into the bank.
 */
public class HexTileManager
{
	private final boolean isDemoMode;
	private int numDraws;
	private final ArrayList<TileProperties> hexes;
	
	/**
	 * Create new HexTileManager.
	 * @param isDemoMode Set to true if we should stack the deck of hex tiles
	 * to match the demo script board.
	 */
	public HexTileManager(boolean isDemoMode)
	{
		this.isDemoMode = isDemoMode;
		numDraws = 0;
		hexes = new ArrayList<TileProperties>();
		for(TileProperties tp : Constants.HEX.values())
		{
			hexes.add(tp);
		}
	}
	
	/**
	 * Call this method to draw a hex tile from the bank.
	 * @return A hex tile.
	 * @throws NoMoreTilesException If there are no more tiles left to draw.
	 */
	public TileProperties drawHexTile() throws NoMoreTilesException
	{
		synchronized(hexes)
		{
			if(isDemoMode)
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
						return removeHexTileByType(Biome.Frozen_Waste);
					case 1:
					case 5:
					case 14:
					case 24:
					case 26:
					case 34:
						numDraws++;
						return removeHexTileByType(Biome.Forest);
					case 2:
					case 18:
					case 21:
					case 28:
					case 32:
						numDraws++;
						return removeHexTileByType(Biome.Jungle);
					case 3:
					case 7:
					case 16:
					case 25:
					case 31:
						numDraws++;
						return removeHexTileByType(Biome.Plains);
					case 4:
						numDraws++;
						return removeHexTileByType(Biome.Sea);
					case 6:
					case 11:
					case 13:
					case 19:
					case 22:
						numDraws++;
						return removeHexTileByType(Biome.Swamp);
					case 9:
					case 17:
					case 20:
					case 29:
					case 33:
						numDraws++;
						return removeHexTileByType(Biome.Mountain);
					case 12:
					case 15:
					case 23:
					case 30:
					case 36:
						numDraws++;
						return removeHexTileByType(Biome.Desert);
				}
				
				throw new NoMoreTilesException("A hex tile could not be drawn because there are no more available.");
			}
			else
			{
				//if not in demo mode draw a random hex
				int index = (int) Math.round(Math.random() * (hexes.size() - 1));
				TileProperties hex = hexes.remove(index);
				return hex;
			}
		}
	}
	
	/**
	 * Use this method to re-add a previously drawn out hex, can be used to implement
	 * the rules for exchanging sea hexes later on.
	 * @param hex The hex to add back in.
	 * @throws IllegalArgumentException if hex is null.
	 */
	public void reInsertHexTile(TileProperties hex)
	{
		if(hex==null)
		{
			throw new IllegalArgumentException("Can not insert null hex.");
		}
		synchronized(hexes)
		{
			hexes.add(hex);
		}
	}
	
	private TileProperties removeHexTileByType(Biome hexType) throws NoMoreTilesException
	{
		synchronized(hexes)
		{
			Iterator<TileProperties> it = hexes.iterator();
			while(it.hasNext())
			{
				TileProperties hex = it.next();
				if(hex.getName().equals(hexType.name()))
				{
					it.remove();
					return hex;
				}
			}
			
			throw new NoMoreTilesException("Unable to draw hex tile of type: " + hexType + ", because there are no more tiles of that type.");
		}
	}
}

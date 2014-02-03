package common.game;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.TileProperties;

public class HexState
{
	private TileProperties hex;
	private final HashSet<TileProperties> thingsInHex;
	
	public HexState(TileProperties hex)
	{
		this(hex, new HashSet<TileProperties>());
	}
	
	public HexState(TileProperties hex, Collection<TileProperties> thingsInHex)
	{
		validateTileNotNull(hex);
		validateIsHexTile(hex);
		if(thingsInHex==null)
		{
			throw new IllegalArgumentException("The entered list of things must not be null");
		}
		
		this.hex = hex;
		this.thingsInHex = new HashSet<TileProperties>();
		for(TileProperties tp : thingsInHex)
		{
			addThingToHex(tp);
		}
	}
	
	public TileProperties getHex()
	{
		return hex;
	}
	
	public void setHex(TileProperties hex)
	{
		validateTileNotNull(hex);
		validateIsHexTile(hex);
		this.hex = hex;
	}
	
	public Set<TileProperties> getThingsInHex()
	{
		return Collections.unmodifiableSet(thingsInHex);
	}
	
	public boolean addThingToHex(TileProperties tile)
	{
		validateTileNotNull(tile);
		if(tile.isHexTile())
		{
			throw new IllegalArgumentException("Can not place hex into another hex");
		}
		if(tile.isBuilding() && hasBuilding())
		{
			throw new IllegalArgumentException("Can not add more than one building to a hex");
		}
		
		return thingsInHex.add(tile);
	}
	
	public boolean hasBuilding()
	{
		for(TileProperties tp : getThingsInHex())
		{
			if(tp.isBuilding())
			{
				return true;
			}
		}
		return false;
	}
	
	public TileProperties getBuilding()
	{
		for(TileProperties tp : getThingsInHex())
		{
			if(tp.isBuilding())
			{
				return tp;
			}
		}
		return null;
	}
	
	public void removeBuildingFromHex()
	{
		if(hasBuilding())
		{
			thingsInHex.remove(getBuilding());
		}
	}
	
	public boolean removeThingFromHex(TileProperties tile)
	{
		validateTileNotNull(tile);
		return thingsInHex.remove(tile);
	}
	
	public Set<TileProperties> getThingsInHexOwnedByPlayer(Player p)
	{
		if(p==null)
		{
			throw new IllegalArgumentException("The entered player must not be null");
		}
		
		HashSet<TileProperties> returnSet = new HashSet<TileProperties>();
		
		for(TileProperties tp : getThingsInHex())
		{
			if(p.ownsThingOnBoard(tp))
			{
				returnSet.add(tp);
			}
		}
		
		return Collections.unmodifiableSet(returnSet);
	}
	
	private static void validateTileNotNull(TileProperties tile)
	{
		if(tile==null)
		{
			throw new IllegalArgumentException("The entered tile must not be null");
		}
	}
	
	private static void validateIsHexTile(TileProperties hex)
	{
		if(!hex.isHexTile())
		{
			throw new IllegalArgumentException("Must enter a hex tile");
		}
	}
}
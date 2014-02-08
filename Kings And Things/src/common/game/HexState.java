package common.game;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.Constants.Category;

import server.logic.game.Player;

/**
 * This class acts as a container for
 * both a hex tile, and all the things
 * inside of that hex tile
 */
public class HexState implements Serializable{

	private static final long serialVersionUID = -1871329628938580400L;
	
	private TileProperties hex;
	private final HashSet<TileProperties> thingsInHex;
	
	//only used by Client GUI for display purpose
	private boolean fake = false;
	
	public HexState(){
		fake = true;
		thingsInHex = new HashSet<TileProperties>();
		hex = new TileProperties( Category.Hex);
	}
	
	/**
	 * Create a new hex state with nothing in the hex
	 * @param hex The hex of this hexState
	 * @throws IllegalArgumentException if hex is null or
	 * not a hex tile
	 */
	public HexState(TileProperties hex){
		this(hex, new HashSet<TileProperties>());
	}
	
	/**
	 * Create a new hex state with a bunch of things inside it
	 * @param hex The hex of this hexState
	 * @param thingsInHex A list of things inside this hex
	 * @throws IllegalArgumentException if hex is null or not
	 * a hex tile, or if ThingsInHex is invalid
	 */
	public HexState(TileProperties hex, Collection<TileProperties> thingsInHex)
	{
		fake = false;
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
	
	public boolean isFake(){
		return fake;
	}
	
	/**
	 * Get the hex of this hexState
	 * @return The hex of this hex state
	 */
	public TileProperties getHex()
	{
		return hex;
	}
	
	/**
	 * Change the hex of this HexState
	 * @param hex The new hex for this hex state
	 * @throws IllegalArgumentException if hex is null
	 * or is not a hex tile
	 */
	public void setHex(TileProperties hex)
	{
		validateTileNotNull(hex);
		validateIsHexTile(hex);
		this.hex = hex;
	}
	
	/**
	 * Gets a non-modifiable view of the things in this
	 * hex state
	 * @return Set of things in this hex
	 */
	public Set<TileProperties> getThingsInHex()
	{
		return Collections.unmodifiableSet(thingsInHex);
	}
	
	/**
	 * Gets all of the 'creature' tiles included in this hex.
	 * @return Set of all creatures in this hex
	 */
	public Set<TileProperties> getCreaturesInHex()
	{
		HashSet<TileProperties> things = new HashSet<>();
		for(TileProperties tp : thingsInHex)
		{
			if(tp.isCreature())
			{
				things.add(tp);
			}
		}
		
		return Collections.unmodifiableSet(things);
	}
	
	/**
	 * Add something to this hexState
	 * @param tile The thing to add
	 * @return true if tile was not already present and got
	 * added, false otherwise
	 * @throws IllegalArgumentException if tile is null,
	 * or can not be added due to game rules
	 */
	public boolean addThingToHex(TileProperties tile)
	{
		validateCanAddThingToHex(tile);
		
		return thingsInHex.add(tile);
	}
	
	/**
	 * This method checks if a tile can be added to this hex and throws exceptions if not.
	 * @param tile The tile to add
	 * @throws IllegalArgumentException if tile is null,
	 * or can not be added due to game rules
	 */
	public void validateCanAddThingToHex(TileProperties tile)
	{
		validateTileNotNull(tile);
		if(!tile.isCreature() && !tile.isSpecialIncomeCounter() && !tile.isBuilding())
		{
			throw new IllegalArgumentException("Can not place " + tile.getName() + "onto the board");
		}
		if(tile.isBuilding() && hasBuilding())
		{
			throw new IllegalArgumentException("Can not add more than one building to a hex");
		}
		if(tile.isSpecialIncomeCounter() && hasSpecialIncomeCounter())
		{
			throw new IllegalArgumentException("Can not add more than one special income counter to a hex");
		}
	}
	
	/**
	 * Check if this hex state has a special income counter
	 * @return True if this hex has a special income counter
	 */
	public boolean hasSpecialIncomeCounter()
	{
		return getSpecialIncomeCounter()!=null;
	}
	
	/**
	 * Gets the special income counter in this hex, if one exists
	 * @return The special income counter in this hex, if one
	 * exists, null otherwise
	 */
	public TileProperties getSpecialIncomeCounter()
	{
		for(TileProperties tp : getThingsInHex())
		{
			if(tp.isSpecialIncomeCounter())
			{
				return tp;
			}
		}
		
		return null;
	}
	
	/**
	 * This method removes any special income counter
	 * that might be in this hex
	 */
	public void removeSpecialIncomeCounterFromHex()
	{
		if(hasSpecialIncomeCounter())
		{
			thingsInHex.remove(getSpecialIncomeCounter());
		}
	}
	
	/**
	 * Checks if there is a building in this hex
	 * @return True if this hex has a building, false otherwise
	 */
	public boolean hasBuilding()
	{
		return getBuilding()!=null;
	}
	
	/**
	 * Get the building in this hex if one exists
	 * @return The building in this hex if one exists,
	 * null otherwise
	 */
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
	
	/**
	 * Use this method to remove any building that might
	 * be in this hex
	 */
	public void removeBuildingFromHex()
	{
		if(hasBuilding())
		{
			thingsInHex.remove(getBuilding());
		}
	}
	
	/**
	 * Use this method to remove something from this hex
	 * @param tile The thing to remove
	 * @return True if tile was removed from this hex,
	 * false  if it was not present to begin with
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean removeThingFromHex(TileProperties tile)
	{
		validateTileNotNull(tile);
		return thingsInHex.remove(tile);
	}
	
	/**
	 * Gets all of the things in this hex that are owned by a particular player
	 * @param p The player to check for
	 * @return Set of all things in this hex owned by the entered player
	 * @throws IllegalArgumentException if p is null
	 */
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
	
	@Override
	public boolean equals(Object other)
	{
		if(other==null || !getClass().equals(other.getClass()))
		{
			return false;
		}
		
		HexState hs = (HexState) other;
		return hex.equals(hs.hex) && thingsInHex.equals(hs.thingsInHex);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + hex.hashCode();
		result = prime * result + thingsInHex.hashCode();
		return result;
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

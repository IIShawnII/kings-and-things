package common.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.TileProperties;

public class Player
{
	private String name;
	private final int id;
	private int gold;
	
	private final HashSet<TileProperties> ownedHexes;
	private final HashSet<TileProperties> ownedThingsOnBoard;
	private final HashSet<TileProperties> tray;
	
	public Player(String name, int playerNumber)
	{
		this.name = name;
		id = playerNumber;
		gold = 0;
		
		ownedHexes = new HashSet<TileProperties>();
		ownedThingsOnBoard = new HashSet<TileProperties>();
		tray = new HashSet<TileProperties>();
	}
	
	public String getPlayerName()
	{
		return name;
	}
	
	public int getPlayerNumber()
	{
		return id;
	}
	
	public int getGold()
	{
		return gold;
	}
	
	public void setGold(int newVal)
	{
		if(newVal < 0)
		{
			throw new IllegalArgumentException("Player gold value must not be negative.");
		}
		gold = newVal;
	}
	
	public void addGold(int amount)
	{
		gold+=amount;
	}
	
	public void removeGold(int amount)
	{
		gold-=amount;
	}
	
	public Set<TileProperties> getOwnedHexes()
	{
		return Collections.unmodifiableSet(ownedHexes);
	}
	
	public boolean addOwnedHex(TileProperties tile)
	{
		validateIsHex(tile);
		return ownedHexes.add(tile);
	}
	
	public boolean removeHexFromOwnership(TileProperties tile)
	{
		validateIsHex(tile);
		return ownedHexes.remove(tile);
	}

	public Set<TileProperties> getOwnedThingsOnBoard()
	{
		return Collections.unmodifiableSet(ownedThingsOnBoard);
	}
	
	public boolean addOwnedThingOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.add(tile);
	}
	
	public boolean removeOwnedThingOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.remove(tile);
	}

	public Set<TileProperties> getTrayThings()
	{
		return Collections.unmodifiableSet(tray);
	}
	
	public boolean addThingToTray(TileProperties tile)
	{
		validateNotNull(tile);
		return tray.add(tile);
	}
	
	public boolean removeThingFromTray(TileProperties tile)
	{
		validateNotNull(tile);
		return tray.remove(tile);
	}
	
	public void placeThingFromTrayOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		if(!tray.contains(tile))
		{
			throw new IllegalArgumentException("The entered tile is not in this player's tray");
		}
		removeThingFromTray(tile);
		addOwnedThingOnBoard(tile);
	}
	
	public boolean ownsThingOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.contains(tile);
	}
	
	public boolean ownsHex(TileProperties hex)
	{
		validateNotNull(hex);
		validateIsHex(hex);
		return ownedHexes.contains(hex);
	}
	
	public boolean ownsThingInTray(TileProperties tile)
	{
		validateNotNull(tile);
		return tray.contains(tile);
	}
	
	public boolean ownsTile(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.contains(tile) || ownedHexes.contains(tile) || tray.contains(tile);
	}
	
	@Override
	public String toString()
	{
		return getPlayerName() + ", #: " + getPlayerNumber();
	}
	
	private static void validateIsHex(TileProperties tile)
	{
		validateNotNull(tile);
		if(!tile.isHexTile())
		{
			throw new IllegalArgumentException("The entered tile must be a hex tile");
		}
	}
	
	private static void validateNotNull(TileProperties tile)
	{
		if(tile == null)
		{
			throw new IllegalArgumentException("The entered tile must not be null");
		}
	}
}

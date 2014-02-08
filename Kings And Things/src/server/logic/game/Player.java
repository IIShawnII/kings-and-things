package server.logic.game;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.game.PlayerInfo;
import common.game.TileProperties;

/**
 * This class represents a player in the game
 */
public class Player implements Serializable{

	private static final long serialVersionUID = -458021976956323899L;
	
	private PlayerInfo info;

	private final HashSet<TileProperties> tray;
	private final HashSet<TileProperties> ownedHexes;
	private final HashSet<TileProperties> ownedThingsOnBoard;
	
	/**
	 * Create a new player with the given info
	 * @param player The player's info
	 * @throws IllegalArgumentException if player is null
	 */
	public Player( PlayerInfo player){
		if(player == null)
		{
			throw new IllegalArgumentException("The entered player info must not be null");
		}
		this.info = player;
		ownedHexes = new HashSet<TileProperties>();
		ownedThingsOnBoard = new HashSet<TileProperties>();
		tray = new HashSet<TileProperties>();
	}

	public void setIsPlaying( boolean ready) {
		info.setReady( ready);
	}
	
	public boolean isPlaying(){
		return info.isReady();
	}

	public boolean isConnected() {
		return info.isConnected();
	}

	public void setConnected( boolean connected) {
		info.setConnected( connected);
	}

	public PlayerInfo getPlayerInfo() {
		return info;
	}
	
	/**
	 * Get the player name
	 * @return The player's name
	 */
	public String getName(){
		return info.getName();
	}
	
	/**
	 * Get the player's number
	 * @return The player's number
	 */
	public int getID()
	{
		return info.getID();
	}
	
	/**
	 * Get the player's current gold amount
	 * @return The gold owned by this player
	 */
	public int getGold()
	{
		return info.getGold();
	}
	
	/**
	 * Set the player's gold to a new amount
	 * @param newVal The new gold amount
	 * @throws IllegalArgumentException if newVal is negative
	 */
	private void setGold(int newVal){
		validateEnteredGoldPositive(newVal);
		info.setGold( newVal);
	}
	
	/**
	 * Add to this player's gold amount
	 * @param amount The amount of gold to add
	 * @throws IllegalArgumentException if amount is
	 * negative
	 */
	public void addGold(int amount){
		validateEnteredGoldPositive(amount);
		setGold( info.getGold()+amount);
	}
	
	/**
	 * Reduce this player's gold amount
	 * @param amount The amount to reduce
	 * @throws IllegalArgumentException if amount
	 * is higher then the player's current gold amount,
	 * or if amount is negative
	 */
	public void removeGold(int amount){
		validateEnteredGoldPositive(amount);
		setGold( info.getGold()-amount);
	}
	
	/**
	 * Get a set of all hexes owned by this player
	 * @return Set of all hexes owned by this player
	 */
	public Set<TileProperties> getOwnedHexes()
	{
		return Collections.unmodifiableSet(ownedHexes);
	}
	
	/**
	 * Add a hex to this player's owned list of hexes
	 * @param tile The hex to add
	 * @return true if hex was added successfully, false
	 * if it was already in this player's list
	 * @throws IllegalArgumentException if tile is null
	 * or is not a hex tile
	 */
	public boolean addOwnedHex(TileProperties tile)
	{
		validateIsHex(tile);
		return ownedHexes.add(tile);
	}
	
	/**
	 * Remove a hex from this player's list of owned hexes
	 * @param tile The hex to remove
	 * @return True if the hex was successfully removed,
	 * false if this player did not own the hex
	 * @throws IllegalArgumentException if tile is null
	 * or is not a hex tile
	 */
	public boolean removeHexFromOwnership(TileProperties tile)
	{
		validateIsHex(tile);
		return ownedHexes.remove(tile);
	}

	/**
	 * Gets a list of all the things that this player owns and has
	 * placed on the board, this includes stuff like buildings and
	 * special income counters, not just creatures.
	 * @return Set of all things on the board owned by this player
	 */
	public Set<TileProperties> getOwnedThingsOnBoard()
	{
		return Collections.unmodifiableSet(ownedThingsOnBoard);
	}
	
	/**
	 * Add something to this player's list of things owned on the board
	 * @param tile The thing to add
	 * @return true if the thing was added successfully, false if it 
	 * was already in the list
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean addOwnedThingOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.add(tile);
	}
	
	/**
	 * remove something from this player's list of things owned
	 * on the board.
	 * @param tile The thing to remove
	 * @return True if tile was removed successfully, false
	 * if it was not in the list to begin with
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean removeOwnedThingOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.remove(tile);
	}

	/**
	 * Get list of things in this players tray
	 * @return Set of all things in this player's tray
	 */
	public Set<TileProperties> getTrayThings()
	{
		return Collections.unmodifiableSet(tray);
	}
	
	/**
	 * Add something to this player's tray
	 * @param tile The thing to add
	 * @return true if tile was added successfully,
	 * false if this player already had it in their tray
	 * @throws IllegalArgumentException if tile is null
	 * or if player has 10 things in his tray
	 */
	public boolean addThingToTray(TileProperties tile)
	{
		if (tray.size() >= 10) {
			throw new IllegalArgumentException("You cannot have more than 10 things in your tray!");
		}
		validateNotNull(tile);
		return tray.add(tile);
	}
	
	/**
	 * Remove something from this player's tray
	 * @param tile The thing to remove
	 * @return True if tile was removed from this player's
	 * tray, false if it was not on the tray to begin with
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean removeThingFromTray(TileProperties tile)
	{
		validateNotNull(tile);
		return tray.remove(tile);
	}
	
	/**
	 * Remove something from this player's tray and place it in
	 * their list of owned things on the board.
	 * @param tile The tile to remove from the tray and place on
	 * the board
	 * @throws IllegalArgumentException if tile is null, or is
	 * not in this player's tray
	 */
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
	
	/**
	 * Check if this player owns something on the board
	 * @param tile The tile to check
	 * @return True if this player owns the thing on the board,
	 * false otherwise
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean ownsThingOnBoard(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.contains(tile);
	}
	
	/**
	 * Check if this player owns a particular hex
	 * @param hex The hex to check
	 * @return True if this player owns the hex,
	 * false otherwise
	 * @throws IllegalArgumentException if tile is null
	 * or is not a hex
	 */
	public boolean ownsHex(TileProperties hex)
	{
		validateNotNull(hex);
		validateIsHex(hex);
		return ownedHexes.contains(hex);
	}
	
	/**
	 * Check if this player has a particular card in their tray
	 * @param tile The tile to look for
	 * @return True if this player has the tile in their tray,
	 * false otherwise
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean ownsThingInTray(TileProperties tile)
	{
		validateNotNull(tile);
		return tray.contains(tile);
	}
	
	/**
	 * Check if this player owns a particular tile, in any
	 * of their lists
	 * @param tile The tile to check for
	 * @return True if this player owns the tile, false otherwise
	 * @throws IllegalArgumentException if tile is null
	 */
	public boolean ownsTile(TileProperties tile)
	{
		validateNotNull(tile);
		return ownedThingsOnBoard.contains(tile) || ownedHexes.contains(tile) || tray.contains(tile);
	}
	/**
	 * Determines income during the gold collection phase
	 * @return
	 */
	public int getIncome()
	{
		return getIncome(false);
	}
	
	/**
	 * Determines income during the special events phase
	 * @return
	 */
	public int getSpecialEventIncome()
	{
		return getIncome(true);
	}
	
	/**
	 * Return a string representation of this player
	 */
	@Override
	public String toString(){
		return info.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + ((ownedHexes == null) ? 0 : ownedHexes.hashCode());
		result = prime * result + ((ownedThingsOnBoard == null) ? 0 : ownedThingsOnBoard.hashCode());
		result = prime * result + ((tray == null) ? 0 : tray.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object other){
		if ( this == other) {
			return true;
		}
		if( other==null){
			return false;
		}
		if( other instanceof Player || other instanceof PlayerInfo){
			return info.equals( (other instanceof PlayerInfo)? other: ((Player)other).info);
		}
		return false;
	}

	// determines player's income
	private int getIncome( boolean event)
	{
		//   1 gold per land hex
		//+  gold per combat value of each fort
		//+  gold per special income counter on the board
		//+  1 gold per special character
			
		int buildingGold = 0;		//keeps track of gold pieces for each fort player controls
		int specialIncomeGold = 0;	//keeps track of gold pieces for each special income counter
			
			//
		for (TileProperties thing : ownedThingsOnBoard) {
			if( !event && thing.isSpecialIncomeCounter()) {
				specialIncomeGold += thing.getValue();
			} else if (thing.isBuildableBuilding()) {
				buildingGold += thing.getValue();
			}
		}
			
		return ownedHexes.size() + buildingGold + specialIncomeGold;
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
	
	private void validateEnteredGoldPositive(int amount)
	{
		if(amount < 0)
		{
			throw new IllegalArgumentException("The entered gold amount must be positive");
		}
	}
}

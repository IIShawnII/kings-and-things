package common.game;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.Constants;
import common.Constants.Category;
import common.Constants.Restriction;

/**
 * This class acts as a container for
 * both a hex tile, and all the things
 * inside of that hex tile
 */
public class HexState implements Serializable{
	
	private static final Image BATLLE_IMAGE = Constants.IMAGES.get( Constants.STATE.get( Restriction.Battle).hashCode());
	
	private static final long serialVersionUID = -1871329628938580400L;

	private transient Image markerImage;
	private ITileProperties marker;
	private ITileProperties hex;
	private final HashSet<ITileProperties> thingsInHex;
	private boolean isInBattle = false;
	private final Point location;
	
	private final int hashCode;
	
	//only used by Client GUI for display purpose
	private boolean isFake;
	
	public HexState(){
		location = new Point();
		isFake = true;
		thingsInHex = new HashSet<ITileProperties>();
		hex = new TileProperties( Category.Hex);
		hashCode = calculateHashCode();
	}
	
	/**
	 * used in moving stacks, to copy all info into new hex
	 */
	public HexState( HexState state){
		this( state.hex, state.thingsInHex);
		setMarker( state.marker);
	}
	
	private HexState(HexState other, boolean deepCopy)
	{
		location = new Point( other.location);
		markerImage = other.markerImage;
		marker = other.marker.clone();
		hex = other.hex.clone();
		thingsInHex = Constants.deepCloneCollection(other.thingsInHex, new HashSet<ITileProperties>());
		isInBattle = other.isInBattle;
		hashCode = other.hashCode;
		//only used by Client GUI for display purpose
		isFake = other.isFake;
	}
	
	@Override
	public HexState clone()
	{
		return new HexState(this,true);
	}
	
	/**
	 * Create a new hex state with nothing in the hex
	 * @param hex The hex of this hexState
	 * @throws IllegalArgumentException if hex is null or
	 * not a hex tile
	 */
	public HexState(ITileProperties hex){
		this(hex, new HashSet<ITileProperties>());
	}
	
	/**
	 * Create a new hex state with a bunch of things inside it
	 * @param hex The hex of this hexState
	 * @param thingsInHex A list of things inside this hex
	 * @throws IllegalArgumentException if hex is null or not
	 * a hex tile, or if ThingsInHex is invalid
	 */
	public HexState(ITileProperties hex, Collection<ITileProperties> thingsInHex)
	{
		location = new Point();
		isFake = false;
		validateTileNotNull(hex);
		validateIsHexTile(hex);
		if(thingsInHex==null)
		{
			throw new IllegalArgumentException("The entered list of things must not be null");
		}
		
		this.hex = hex;
		this.thingsInHex = new HashSet<ITileProperties>();
		for(ITileProperties tp : thingsInHex)
		{
			addThingToHex(tp);
		}
		hashCode = calculateHashCode();
	}
	
	public Point getLocation(){
		return new Point( location);
	}
	
	public ArrayList<Point> getAdjacentLocations()
	{
		int x = location.x;
		int y = location.y;
			
		ArrayList<Point> coordsToTest = new ArrayList<>();
		coordsToTest.add(new Point(x,y-2));
		coordsToTest.add(new Point(x,y+2));
		coordsToTest.add(new Point(x-1,y-1));
		coordsToTest.add(new Point(x-1,y+1));
		coordsToTest.add(new Point(x+1,y-1));
		coordsToTest.add(new Point(x+1,y+1));
		
		return coordsToTest;
	}
	
	public HexState setLocation( int x, int y){
		location.setLocation( x, y);
		return this;
	}
	
	public void addHex( HexState state){
		for(ITileProperties tp : state.thingsInHex){
			addThingToHex(tp);
		}
		if( !marker.equals( state.marker)){
			isInBattle = true;
		}
	}
	
	public boolean hasThings(){
		return thingsInHex.size()>=1;
	}
	
	public boolean isInBattle(){
		return isInBattle;
	}
	
	public void setInBattle( boolean battle){
		isInBattle = battle;
	}
	
	public boolean hasMarker(){
		return marker!=null;
	}
	
	public void setMarker( ITileProperties marker){
		this.marker = marker;
		markerImage = Constants.IMAGES.get( marker.hashCode());
	}
	
	public void removeMarker(){
		this.marker = null;
		markerImage = null;
	}
	
	public boolean hasMarkerForPlayer(int id)
	{
		return marker!=null && marker.equals(Constants.getPlayerMarker(id));
	}
	
	public void paint( Graphics g, Point point){
		if(marker!=null && markerImage==null){
			markerImage = Constants.IMAGES.get( marker.hashCode());
		}
		if( hasMarker() && isInBattle()){
			g.drawImage( markerImage, point.x+5, point.y+5, Constants.TILE_SIZE_BOARD.width, Constants.TILE_SIZE_BOARD.height, null);
			g.drawImage( BATLLE_IMAGE, point.x-5, point.y-5, Constants.TILE_SIZE_BOARD.width, Constants.TILE_SIZE_BOARD.height, null);
		}else if( hasMarker()){
			g.drawImage( markerImage, point.x, point.y, Constants.TILE_SIZE_BOARD.width, Constants.TILE_SIZE_BOARD.height, null);
		}else if( isInBattle()){
			g.drawImage( BATLLE_IMAGE, point.x, point.y, Constants.TILE_SIZE_BOARD.width, Constants.TILE_SIZE_BOARD.height, null);
		}
	}
	
	public boolean isFake(){
		return isFake;
	}
	
	/**
	 * Get the hex of this hexState
	 * @return The hex of this hex state
	 */
	public ITileProperties getHex()
	{
		return hex;
	}
	
	/**
	 * Change the hex of this HexState
	 * @param hex The new hex for this hex state
	 * @throws IllegalArgumentException if hex is null
	 * or is not a hex tile
	 */
	public void setHex(ITileProperties hex)
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
	public Set<ITileProperties> getThingsInHex()
	{
		return Collections.unmodifiableSet(thingsInHex);
	}
	
	/**
	 * Gets all of the 'creature' tiles included in this hex.
	 * @return Set of all creatures in this hex
	 */
	public Set<ITileProperties> getCreaturesInHex()
	{
		HashSet<ITileProperties> things = new HashSet<>();
		for(ITileProperties tp : thingsInHex)
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
	public boolean addThingToHex( ITileProperties tile)
	{
		if(tile.isBuilding())
		{
			validateCanAddThingToHex(tile, true);
		}
		else
		{
			validateCanAddThingToHex(tile, false);
		}
		return thingsInHex.add(tile);
	}
	

	
	/**
	 * Add something to this hexState ONLY for use in GUI side
	 * @param tile The thing to add
	 * @return current HexSate
	 */
	public HexState addThingToHexGUI( ITileProperties tile){
		switch( tile.getRestriction(0)){
			case Battle:
				setInBattle( true);
				break;
			case Yellow:
			case Gray:
			case Green:
			case Red:
				setMarker( tile);
				break;
			default:
				thingsInHex.add(tile);
		}
		return this;
	}
	
	/**
	 * This method checks if a tile can be added to this hex and throws exceptions if not.
	 * @param tile The tile to add
	 * @param checkBuildingLimit Set to true to check fort number limit
	 * @throws IllegalArgumentException if tile is null,
	 * or can not be added due to game rules
	 */
	public void validateCanAddThingToHex(ITileProperties tile, boolean checkBuildingLimit)
	{
		validateTileNotNull(tile);
		if(!tile.isCreature() && !tile.isSpecialIncomeCounter() && !tile.isBuilding())
		{
			throw new IllegalArgumentException("Can not place " + tile.getName() + " onto the board");
		}
		if(checkBuildingLimit && tile.isBuilding() && hasBuilding())
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
	public ITileProperties getSpecialIncomeCounter()
	{
		for(ITileProperties tp : getThingsInHex())
		{
			if(tp.isSpecialIncomeCounter())
			{
				return tp;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the set of all things added to this hex that are
	 * capable of participating in combat
	 * @return Set of things that can fight
	 */
	public Set<ITileProperties> getFightingThingsInHex()
	{
		HashSet<ITileProperties> fightingThings = new HashSet<ITileProperties>();
		for(ITileProperties thing : getThingsInHex())
		{
			if((thing.isCreature() || thing.isBuilding()) && thing.getValue()>0)
			{
				fightingThings.add(thing);
			}
		}
		
		return Collections.unmodifiableSet(fightingThings);
	}

	/**
	 * Gets the set of all things added to this hex that are
	 * capable of participating in combat
	 * @return Set of things that can fight
	 */
	public Set<ITileProperties> getFightingThingsInHexOwnedByPlayer(Player p)
	{
		HashSet<ITileProperties> things = new HashSet<ITileProperties>();
		for(ITileProperties thing : getFightingThingsInHex())
		{
			if(p.ownsThingOnBoard(thing))
			{
				things.add(thing);
			}
		}
		
		return Collections.unmodifiableSet(things);
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
	public ITileProperties getBuilding()
	{
		for(ITileProperties tp : getThingsInHex())
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
	public boolean removeThingFromHex(ITileProperties tile)
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
	public Set<ITileProperties> getThingsInHexOwnedByPlayer(Player p)
	{
		if(p==null)
		{
			throw new IllegalArgumentException("The entered player must not be null");
		}
		
		HashSet<ITileProperties> returnSet = new HashSet<ITileProperties>();
		
		for(ITileProperties tp : getThingsInHex())
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
		return hashCode == hs.hashCode;
	}
	
	@Override
	public int hashCode()
	{
		return hashCode;
	}
	
	@Override
	public String toString(){
		return location.toString();
	}
	
	private int calculateHashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + hex.hashCode();
		result = prime * result + thingsInHex.hashCode();
		return result;
	}
	
	private static void validateTileNotNull(ITileProperties tile)
	{
		if(tile==null)
		{
			throw new IllegalArgumentException("The entered tile must not be null");
		}
	}
	
	private static void validateIsHexTile(ITileProperties hex)
	{
		if(!hex.isHexTile())
		{
			throw new IllegalArgumentException("Must enter a hex tile");
		}
	}
}

package common.game;

import static common.Constants.FACE_DOWN;
import static common.Constants.FACE_UP;
import static common.Constants.INFINITE_TILE;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.Restriction;

public class TileProperties implements Serializable{

	private static final long serialVersionUID = 3896952672735323992L;
	
	private int number;
	private int value;
	private int moveSpeed;
	private String name;
	private boolean hasFlip;
	private boolean specialFlip;
	private boolean isFaceUp;
	private ArrayList< Ability> abilities;
	private ArrayList< Restriction> restrictions;
	
	public TileProperties(){
		this( 1, 0, "none", null, null);
	}
	
	public TileProperties( TileProperties tile, int number){
		this( number, tile.value, tile.name, tile.abilities, tile.restrictions);
		hasFlip = tile.hasFlip;
		specialFlip = tile.specialFlip;
		moveSpeed = tile.moveSpeed;
	}
	
	private TileProperties( int number, int attack, String name, ArrayList< Ability> abilities, ArrayList< Restriction> restrictions){
		this.name = name;
		this.hasFlip = true;
		this.value = attack;
		this.number = number;
		this.specialFlip = false;
		this.abilities = abilities==null? new ArrayList<Ability>() : new ArrayList<>( abilities);
		this.restrictions = restrictions==null? new ArrayList<Restriction>() : new ArrayList<>( restrictions);
		isFaceUp = specialFlip;
	}

	public int getNumber() {
		return number;
	}

	protected void setNumber( int number) {
		this.number = number;
	}

	public int getValue() {
		return value;
	}
	
	protected void setValue( int value) {
		this.value = value;
	}
	
	// retrieves moveSpeed
	public int getMoveSpeed () {
		return moveSpeed;
	}
	
	// assigns new moveSpeed
	public void setMoveSpeed(int moveSpeed) {
		this.moveSpeed = moveSpeed;
	}
	
	public String getName() {
		return name;
	}
	
	protected void setName( String name) {
		this.name = name;
	}
	
	public Restriction[] getRestriction() {
		Restriction[] array = new Restriction[ restrictions.size()];
		restrictions.toArray( array);
		return array;
	}
	
	protected void addRestriction( Restriction restriction) {
		restrictions.add( restriction);
	}
	
	public Ability[] getAbilities() {
		Ability[] array = new Ability[ abilities.size()];
		abilities.toArray( array);
		return array;
	}
	
	protected void addAbilities( Ability ability) {
		abilities.add( ability);
	}

	protected void setSpecialFlip() {
		specialFlip = true;
	}
	
	public Rectangle getFlip(){
		return isFaceUp? FACE_UP: FACE_DOWN;
	}
	
	public void flip(){
		isFaceUp = !isFaceUp;
	}
	
	public boolean isFaceUp()
	{
		return isFaceUp;
	}

	protected void setNoFlip() {
		hasFlip = false;
	}
	
	public boolean hasFlip(){
		return hasFlip;
	}

	protected void setInfinite() {
		number = INFINITE_TILE;
	}
	
	public boolean isInfinit(){
		return number == INFINITE_TILE;
	}
	
	public boolean isHexTile()
	{
		for(Biome b : Biome.values())
		{
			if(b.name().equals(getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isBuilding()
	{
		for(Building b : Building.values())
		{
			if(b.name().equals(getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isBuildableBuilding()
	{
		for(BuildableBuilding b : BuildableBuilding.values())
		{
			if(b.name().equals(getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isRestrictedToBiome()
	{
		return getBiomeRestriction()!=null;
	}
	
	public boolean isEvent()
	{
		return restrictions.contains(Restriction.Event);
	}
	
	public boolean isMagicItem()
	{
		return restrictions.contains(Restriction.Magic);
	}
	
	public boolean isTreasure()
	{
		return restrictions.contains(Restriction.Treasure) && !isRestrictedToBiome();
	}
	
	public boolean isCreature()
	{
		return !isBuilding() && !isEvent() && !isMagicItem() && !isTreasure() && !isSpecialIncomeCounter();
	}
	
	public boolean hasAbility(Ability ability)
	{
		for(Ability a : getAbilities())
		{
			if(a == ability)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isSpecialCreatureWithAbility(Ability ability)
	{
		return isCreature() && hasAbility(ability);
	}
	
	public Biome getBiomeRestriction()
	{
		for(Restriction r : restrictions)
		{
			if(r == Restriction.Desert || r == Restriction.Forest || r == Restriction.Frozen_Waste ||
				r == Restriction.Jungle || r == Restriction.Mountain || r == Restriction.Plains ||
				r == Restriction.Sea || r == Restriction.Swamp)
			{
				return Biome.valueOf(r.name());
			}
		}
		
		return null;
	}
	
	public boolean isSpecialIncomeCounter()
	{
		
		return (restrictions.contains(Restriction.Treasure) && isRestrictedToBiome()) || Building.Village.name().equals(getName()) || Building.City.name().equals(getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		result = prime * result + value;
		result = prime * result + name.hashCode();
		result = prime * result + abilities.hashCode();
		result = prime * result + restrictions.hashCode();
		return result;
	}

	@Override
	public boolean equals( Object obj) {
		if ( this == obj) {
			return true;
		}
		if ( obj == null || !(obj instanceof TileProperties)) {
			return false;
		}
		TileProperties other = (TileProperties) obj;
		if ( number != other.number || value != other.value || !name.equals( other.name)  || !(restrictions.equals( other.restrictions)) || !(abilities.equals( other.abilities))) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(){
		return "-n " + name + " -a " + value + " -c " + number;
	}
}
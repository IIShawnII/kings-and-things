package common.game;

import static common.Constants.INFINITE_TILE;

import java.io.Serializable;
import java.util.ArrayList;

import common.Constants.Biome;
import common.Constants.Ability;
import common.Constants.Category;
import common.Constants.Restriction;

public class TileProperties implements Serializable{

	private static final long serialVersionUID = 3896952672735323992L;
	
	private int number;
	private int value;
	private final int baseValue;
	private int moveSpeed;
	private String name;
	private boolean hasFlip;
	private boolean specialFlip;
	private boolean isFaceUp;
	private ArrayList< Ability> abilities;
	private ArrayList< Restriction> restrictions;
	private Category tileType;
	private Biome biome;
	
	private boolean fake = false;
	
	public TileProperties( Category category){
		fake = true;
		tileType = category;
		baseValue = 0;
	}
	
	TileProperties(){
		this( 1, 0, "none", null, null);
	}
	
	public TileProperties( TileProperties tile, int number){
		this( number, tile.value, tile.name, tile.abilities, tile.restrictions);
		hasFlip = tile.hasFlip;
		specialFlip = tile.specialFlip;
		moveSpeed = tile.moveSpeed;
		tileType = tile.tileType;
		isFaceUp = tile.isFaceUp;
		biome = tile.biome;
	}
	
	private TileProperties( int number, int attack, String name, ArrayList< Ability> abilities, ArrayList< Restriction> restrictions){
		this.name = name;
		this.hasFlip = true;
		this.value = attack;
		baseValue = value;
		this.number = number;
		this.specialFlip = false;
		this.abilities = abilities==null? new ArrayList<Ability>() : new ArrayList<>( abilities);
		this.restrictions = restrictions==null? new ArrayList<Restriction>() : new ArrayList<>( restrictions);
		isFaceUp = specialFlip;
		biome = null;
	}
	
	public boolean isFake(){
		return fake;
	}
	
	public void setCategory( Category category){
		tileType = category;
	}
	
	public Category getCategory(){
		return tileType;
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
	
	public void setValue( int value) {
		this.value = value;
	}

	public void resetValue()
	{
		value = baseValue;
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
	
	protected void addRestriction( Restriction restriction) {
		try{
			biome = Biome.valueOf( restriction.name());
		}catch( IllegalArgumentException ex){}
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
		return tileType == Category.Hex;
	}
	
	public boolean isBuilding()
	{
		return tileType == Category.Building || isBuildableBuilding();
	}
	
	public boolean isBuildableBuilding()
	{
		return tileType == Category.Buildable;
	}
	
	public boolean isRestrictedToBiome()
	{
		return getBiomeRestriction()!=null;
	}
	
	public boolean isEvent()
	{
		return tileType == Category.Event;
	}
	
	public boolean isMagicItem()
	{
		return tileType == Category.Magic;
	}
	
	public boolean isTreasure()
	{
		return tileType == Category.Treasure;
	}
	
	public boolean isCreature()
	{
		return tileType == Category.Creature;
	}
	
	public boolean hasAbility(Ability ability)
	{
		return abilities.contains( ability);
	}
	
	public boolean isSpecialCreatureWithAbility(Ability ability)
	{
		return isCreature() && hasAbility(ability);
	}
	
	public Biome getBiomeRestriction()
	{
		return biome;
	}
	
	public boolean isSpecialIncomeCounter()
	{
		
		return (restrictions.contains(Restriction.Treasure) && isRestrictedToBiome()) || (isBuilding() && !isBuildableBuilding());
	}
	
	public Restriction getRestriction( int index){
		return restrictions.get( index);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		result = prime * result + value;
		result = prime * result + (name==null?0:name.hashCode());
		result = prime * result + (abilities==null?0:abilities.hashCode());
		result = prime * result + (restrictions==null?0:restrictions.hashCode());
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

	public boolean hasRestriction() {
		return restrictions.size()>=1;
	}
}
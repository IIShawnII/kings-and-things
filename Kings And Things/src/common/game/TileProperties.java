package common.game;

import static common.Constants.INFINITE_TILE;

import java.util.ArrayList;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.Category;
import common.Constants.Restriction;

public class TileProperties implements ITileProperties{

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
	
	/**
	 * fake constructor will be removed in complete game
	 * @param category - tile category to fake
	 */
	public TileProperties( Category category){
		fake = true;
		tileType = category;
		baseValue = 0;
		this.abilities = new ArrayList<>();
		this.restrictions = new ArrayList<>();
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
	
	@Override
	public boolean isFake(){
		return fake;
	}
	
	protected void setCategory( Category category){
		tileType = category;
	}
	
	@Override
	public Category getCategory(){
		return tileType;
	}

	@Override
	public int getNumber() {
		return number;
	}

	protected void setNumber( int number) {
		this.number = number;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue( int value) {
		this.value = value;
	}

	@Override
	public void resetValue()
	{
		value = baseValue;
	}

	@Override
	// retrieves moveSpeed
	public int getMoveSpeed () {
		return moveSpeed;
	}

	@Override
	// assigns new moveSpeed
	public void setMoveSpeed(int moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	@Override
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

	@Override
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

	@Override
	public void flip(){
		isFaceUp = !isFaceUp;
	}

	@Override
	public boolean isFaceUp()
	{
		return isFaceUp;
	}

	protected void setNoFlip() {
		hasFlip = false;
	}

	@Override
	public boolean hasFlip(){
		return hasFlip;
	}

	protected void setInfinite() {
		number = INFINITE_TILE;
	}

	@Override
	public boolean isInfinit(){
		return number == INFINITE_TILE;
	}

	@Override
	public boolean isHexTile()
	{
		return tileType == Category.Hex;
	}

	@Override
	public boolean isBuilding()
	{
		return tileType == Category.Building || isBuildableBuilding();
	}

	@Override
	public boolean isBuildableBuilding()
	{
		return tileType == Category.Buildable;
	}

	@Override
	public boolean isRestrictedToBiome()
	{
		return getBiomeRestriction()!=null;
	}

	@Override
	public boolean isEvent()
	{
		return tileType == Category.Event;
	}

	@Override
	public boolean isMagicItem()
	{
		return tileType == Category.Magic;
	}

	@Override
	public boolean isTreasure()
	{
		return tileType == Category.Treasure;
	}

	@Override
	public boolean isCreature()
	{
		return tileType == Category.Creature || tileType == Category.Special;
	}

	@Override
	public boolean isSpecialCharacter()
	{
		return tileType == Category.Special;
	}

	@Override
	public boolean hasAbility(Ability ability)
	{
		return abilities.contains( ability);
	}

	@Override
	public boolean isSpecialCreatureWithAbility(Ability ability)
	{
		return isCreature() && hasAbility(ability);
	}

	@Override
	public Biome getBiomeRestriction()
	{
		return biome;
	}

	@Override
	public boolean isSpecialIncomeCounter()
	{
		
		return (restrictions.contains(Restriction.Treasure) && isRestrictedToBiome()) || (isBuilding() && !isBuildableBuilding());
	}

	@Override
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
		if ( number != other.number || value != other.value || (name!=null&&!name.equals( other.name))  || !(restrictions.equals( other.restrictions)) || !(abilities.equals( other.abilities))) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(){
		return "-n " + name + " -a " + value + " -c " + number;
	}

	@Override
	public boolean hasRestriction() {
		return restrictions.size()>=1;
	}
}
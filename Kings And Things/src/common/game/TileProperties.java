package common.game;

import java.util.ArrayList;

import common.Constants;
import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.Category;
import common.Constants.Restriction;

public class TileProperties implements ITileProperties{

	private static final long serialVersionUID = 3896952672735323992L;
	private static long counter;
	
	private int number;
	private int value;
	private int baseValue;
	private int moveSpeed;
	private String name;
	private boolean hasFlip;
	private boolean specialFlip;
	private boolean isFaceUp;
	private ArrayList< Ability> abilities;
	private ArrayList< Restriction> restrictions;
	private Category tileType;
	private BuildableBuilding buildable;
	private Building building;
	private Biome biome;
	
	private final long id;
	
	private boolean fake = false;
	
	/**
	 * fake constructor will be removed in complete game
	 * @param category - tile category to fake
	 */
	public TileProperties( Category category){
		fake = true;
		setCategory( category);
		baseValue = 0;
		this.abilities = new ArrayList<>();
		this.restrictions = new ArrayList<>();
		isFaceUp = true;
		id = counter++;
	}
	
	TileProperties(){
		this( 1, 0, "none", null, null);
	}
	
	public TileProperties( TileProperties tile, int number){
		this( number, tile.value, tile.name, tile.abilities, tile.restrictions);
		hasFlip = tile.hasFlip;
		specialFlip = tile.specialFlip;
		moveSpeed = tile.moveSpeed;
		setCategory( tile.tileType);
		isFaceUp = tile.isFaceUp;
		biome = tile.biome;
	}
	
	private TileProperties( int number, int attack, String name, ArrayList< Ability> abilities, ArrayList< Restriction> restrictions){
		this.name = name;
		this.hasFlip = true;
		this.value = attack;
		baseValue = attack;
		this.number = number;
		this.specialFlip = false;
		this.abilities = abilities==null? new ArrayList<Ability>() : new ArrayList<>( abilities);
		this.restrictions = restrictions==null? new ArrayList<Restriction>() : new ArrayList<>( restrictions);
		isFaceUp = specialFlip;
		biome = null;
		id = counter++;
	}
	
	private TileProperties(TileProperties other)
	{
		number = other.number;
		value = other.value;
		baseValue = other.baseValue;
		moveSpeed = other.moveSpeed;
		name = other.name;
		hasFlip = other.hasFlip;
		specialFlip = other.specialFlip;
		isFaceUp = other.isFaceUp;
		abilities = new ArrayList<>(other.abilities);
		restrictions = new ArrayList<>(other.restrictions);
		setCategory( other.tileType);
		biome = other.biome;
		id = other.id;
		fake = other.fake;
	}
	
	@Override
	public TileProperties clone()
	{
		return new TileProperties(this);
	}
	
	@Override
	public boolean isFake(){
		return fake;
	}
	
	protected void setCategory( Category category){
		tileType = category;
		switch( tileType){
			case Buildable:
				buildable = BuildableBuilding.valueOf( name);
				break;
			case Building:
				building = Building.valueOf( name);
				break;
			default:
				break;
		}
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
	
	void setBaseValue(int value)
	{
		baseValue = value;
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
		number = Constants.INFINITE_TILE;
	}

	@Override
	public boolean isInfinit(){
		return number == Constants.INFINITE_TILE;
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
	public boolean hasAbility()
	{
		return abilities.size()>=1;
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
		return hasRestriction()?restrictions.get( index):Restriction.None;
	}
	
	@Override
	public int hashCode() {
		return new Long(id).hashCode();
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
		return id==other.id;
	}
	
	@Override
	public String toString(){
		return "-n " + name + " -a " + value + " -c " + number;
	}

	@Override
	public boolean hasRestriction() {
		return restrictions.size()>=1;
	}
	
	@Override
	public boolean hasRestriction( Restriction restriction){
		return restrictions.contains( restriction);
	}

	@Override
	public BuildableBuilding getBuildable() {
		return buildable;
	}

	@Override
	public Building getBuilding() {
		return building;
	}

	@Override
	public BuildableBuilding getNextBuilding() {
		int next = buildable.ordinal()+1;
		BuildableBuilding[] bb = BuildableBuilding.values();
		if( next >= bb.length ){
			return null;
		}
		return bb[next];
	}
}
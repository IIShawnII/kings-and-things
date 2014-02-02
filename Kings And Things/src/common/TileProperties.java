package common;

import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.Building;
import common.Constants.Restriction;
import static common.Constants.FACE_UP;
import static common.Constants.FACE_DOWN;
import static common.Constants.INFINITE_TILE;

public class TileProperties {

	private int number;
	private int attack;
	private Image image;
	private String name;
	private boolean hasFlip;
	private boolean specialFlip;
	private ArrayList< Ability> abilities;
	private ArrayList< Restriction> restrictions;
	
	public TileProperties(){
		this( 1, 0, null, "none", null, null);
	}
	
	public TileProperties( TileProperties tile, int number){
		this( number, tile.attack, tile.image, tile.name, tile.abilities, tile.restrictions);
	}
	
	private TileProperties( int number, int attack, Image image, String name, ArrayList< Ability> abilities, ArrayList< Restriction> restrictions){
		this.name = name;
		this.image = image;
		this.hasFlip = true;
		this.attack = attack;
		this.number = number;
		this.specialFlip = false;
		this.abilities = abilities==null? new ArrayList<Ability>() : new ArrayList<>( abilities);
		this.restrictions = restrictions==null? new ArrayList<Restriction>() : new ArrayList<>( restrictions);
	}

	public int getNumber() {
		return number;
	}

	protected void setNumber( int number) {
		this.number = number;
	}

	public int getAttack() {
		return attack;
	}
	
	protected void setAttack( int attack) {
		this.attack = attack;
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
	
	public Image getImage(){
		return image;
	}
	
	protected void setImage( Image image){
		this.image = image;
	}

	protected void setSpecialFlip() {
		specialFlip = true;
	}
	
	public Rectangle getFlip(){
		return specialFlip? FACE_UP: FACE_DOWN;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		result = prime * result + attack;
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
		if ( number != other.number || attack != other.attack || !name.equals( other.name)  || !(restrictions.equals( other.restrictions)) || !(abilities.equals( other.abilities))) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(){
		return name + ", " + attack;
	}
}
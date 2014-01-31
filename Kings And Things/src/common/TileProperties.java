package common;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;

import common.Constants.Ability;
import common.Constants.Restriction;

public class TileProperties {

	private int attack;
	private Image image;
	private String name;
	private Restriction restriction;
	private ArrayList< Ability> abilities;
	
	public TileProperties(){
		this( 0, null, "none", null, (Ability)null);
	}
	
	public TileProperties( int attack, Image image, String name, Restriction restriction, Ability...ability) {
		super();
		this.attack = attack;
		this.image = image;
		this.name = name;
		this.restriction = restriction;
		if( ability==null || ability.length==0){
			abilities = new ArrayList<>();
		}else{
			abilities = new ArrayList<>( Arrays.asList(ability));
		}
	}

	public int getAttack() {
		return attack;
	}
	
	public void setAttack( int attack) {
		this.attack = attack;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( String name) {
		this.name = name;
	}
	
	public Restriction getRestriction() {
		return restriction;
	}
	
	public void setRestriction( Restriction restriction) {
		this.restriction = restriction;
	}
	
	public Ability[] getAbilities() {
		Ability[] array = new Ability[ abilities.size()];
		abilities.toArray( array);
		return array;
	}
	
	public void setAbilities( Ability ability) {
		abilities.add( ability);
	}
	
	public Image getImage(){
		return image;
	}
	
	public void setImage( Image image){
		this.image = image;
	}
}
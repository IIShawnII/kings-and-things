package common.game;

import java.awt.Point;
import java.io.Serializable;

public class PlayerInfo implements Serializable {
	
	private static final long serialVersionUID = 4318444709892949244L;
	
	private final int ID;
	private int gold;
	private String name;
	private int cardsOnRack;
	private boolean isReady;
	private boolean isConnected;
	private boolean isActive;
	private Point start;
	
	public PlayerInfo( String name, final int ID, boolean ready){
		if( name==null || name.length()==0){
			throw new IllegalArgumentException("The player name must not be null");
		}
		this.name = name;
		this.ID = ID;
		this.gold = 0;
		this.isReady = ready;
		this.start = null;
		isActive = false;
	}
	
	public PlayerInfo(PlayerInfo other)
	{
		ID = other.ID;
		gold = other.gold;
		name = other.name;
		cardsOnRack = other.cardsOnRack;
		isReady = other.isReady;
		isConnected = other.isConnected;
		isActive = other.isActive;
		start = new Point(other.start.x,other.start.y);
	}
	
	@Override
	public PlayerInfo clone()
	{
		return new PlayerInfo(this);
	}
	
	public void setCardonRack( int count){
		cardsOnRack = count;
	}
	
	public boolean hasStartPoint(){
		return start!=null;
	}
	
	public Point getStart(){
		return new Point( start);
	}
	
	public PlayerInfo( PlayerInfo player, final int ID){
		this( player.name, ID, player.isReady);
		this.gold = player.gold;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public void setReady( boolean isReady) {
		this.isReady = isReady;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isActive()
	{
		return isActive;
	}
	
	public void setIsActive(boolean newVal)
	{
		isActive = newVal;
	}
	
	public void setConnected( boolean connected) {
		this.isConnected = connected;
	}
	
	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}
	
	public int getGold() {
		return gold;
	}
	
	public void setGold( int gold) {
		this.gold = gold;
	}
	
	@Override
	public String toString(){
		return name + ", ID: " + ID + ", Ready: " + isReady + ", Connected: " + isConnected + ", Gold: " + gold;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object other) {
		if ( this == other) {
			return true;
		}
		if ( other == null) {
			return false;
		}
		if ( !(other instanceof PlayerInfo)) {
			return false;
		}
		PlayerInfo player = (PlayerInfo) other;
		if ( ID != player.ID) {
			return false;
		}
		if ( name == null) {
			if ( player.name != null) {
				return false;
			}
		} else if ( !name.equals( player.name)) {
			return false;
		}
		return true;
	}

	public int getCradsOnRack() {
		return cardsOnRack;
	}
}

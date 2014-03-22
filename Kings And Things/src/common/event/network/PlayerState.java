package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.PlayerInfo;

public class PlayerState extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = -4123363630559056214L;
	
	private PlayerInfo player;
	
	public PlayerState(){
		super();
	}
	
	public PlayerState( PlayerInfo player){
		this.player = player;
	}
	
	public PlayerState( String name, final int ID, boolean ready){
		this( new PlayerInfo( name, ID, ready));
	}
	
	public PlayerState( String name, boolean ready){
		this( name, -1, ready);
	}

	public PlayerInfo getPlayer(){
		return player;
	}

	@Override
	public String toString(){
		return "Network/PlayerReady: " + player;
	}
}

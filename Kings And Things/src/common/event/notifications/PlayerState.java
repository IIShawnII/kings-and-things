package common.event.notifications;

import common.PlayerInfo;
import common.event.AbstractNetwrokEvent;

public class PlayerState extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = -4342116267397416570L;
	
	private PlayerInfo player;
	
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

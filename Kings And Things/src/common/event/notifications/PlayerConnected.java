package common.event.notifications;

import java.util.HashSet;

import common.Player;
import common.event.AbstractNetwrokEvent;

public class PlayerConnected extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = -2436121223229723837L;
	
	HashSet<Player> players = new HashSet<>();
	
	public PlayerConnected(){
		players = new HashSet<>();
	}
	
	public void addPlayer( Player player){
		players.add( player);
	}
	
	public HashSet<Player> getPlayers(){
		return players;
	}
}

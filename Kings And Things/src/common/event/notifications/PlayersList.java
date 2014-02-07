package common.event.notifications;

import java.util.HashSet;

import common.Player;
import common.event.AbstractNetwrokEvent;

public class PlayersList extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = 1901897562363128873L;
	
	HashSet<Player> players = new HashSet<>();
	
	public PlayersList(){
		players = new HashSet<>();
	}
	
	public void addPlayer( Player player){
		players.add( player);
	}
	
	public HashSet<Player> getPlayers(){
		return players;
	}
	
	@Override
	public String toString(){
		return "Network/PlayerList: " + players;
	}
}

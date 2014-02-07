package common.event.notifications;

import java.util.ArrayList;

import common.PlayerInfo;
import common.event.AbstractNetwrokEvent;

public class PlayersList extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = 1901897562363128873L;
	
	ArrayList<PlayerInfo> players;
	
	public PlayersList(){
		players = new ArrayList<>();
	}
	
	public void addPlayer( PlayerInfo player){
		players.add( player);
	}
	
	public ArrayList<PlayerInfo> getPlayers(){
		return players;
	}
	
	@Override
	public String toString(){
		return "Network/PlayerList: " + players;
	}
}

package common.event.network;

import java.util.Set;
import java.util.ArrayList;

import common.game.Player;
import common.game.PlayerInfo;
import common.event.AbstractNetwrokEvent;

public class PlayersList extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = 1901897562363128873L;
	
	ArrayList<PlayerInfo> players;
	
	public PlayersList( Set<Player> players){
		this();
		for( Player p : players){
			addPlayer( p.getPlayerInfo());
		}
	}
	
	public PlayersList(){
		players = new ArrayList<PlayerInfo>();
	}
	
	public void addPlayer( PlayerInfo player){
		players.add( player);
	}
	
	public PlayerInfo[] getPlayers(){
		PlayerInfo[] array = new PlayerInfo[players.size()];
		for( int i=0; i<array.length;i++){
			array[i] = players.get( i);
		}
		return array;
	}
	
	@Override
	public String toString(){
		return "Network/PlayerList: " + players;
	}
}

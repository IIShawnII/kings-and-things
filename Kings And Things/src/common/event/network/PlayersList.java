package common.event.network;

import java.util.ArrayList;

import common.event.AbstractNetwrokEvent;
import common.game.PlayerInfo;

public class PlayersList extends AbstractNetwrokEvent{
	
	private static final long serialVersionUID = 1901897562363128873L;
	
	ArrayList<PlayerInfo> players;
	
	public PlayersList(){
		players = new ArrayList<>();
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

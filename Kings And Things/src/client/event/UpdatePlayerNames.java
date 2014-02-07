package client.event;

import java.util.ArrayList;

import common.PlayerInfo;
import common.event.AbstractEvent;

public class UpdatePlayerNames extends AbstractEvent {
	
	private ArrayList<PlayerInfo> players;
	
	public UpdatePlayerNames( ArrayList<PlayerInfo> players) {
		this.players = players;
	}
	
	public ArrayList<PlayerInfo> getPlayers(){
		return players;
	}
}

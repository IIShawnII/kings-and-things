package client.event;

import java.util.ArrayList;

import common.event.AbstractEvent;
import common.game.PlayerInfo;

public class UpdatePlayerNames extends AbstractEvent {
	
	private ArrayList<PlayerInfo> players;
	
	public UpdatePlayerNames( ArrayList<PlayerInfo> players) {
		this.players = players;
	}
	
	public ArrayList<PlayerInfo> getPlayers(){
		return players;
	}
}

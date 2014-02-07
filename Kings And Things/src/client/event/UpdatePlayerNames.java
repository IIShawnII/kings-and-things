package client.event;

import java.util.HashSet;

import common.Player;
import common.event.AbstractEvent;

public class UpdatePlayerNames extends AbstractEvent {
	
	HashSet<Player> players;
	
	public UpdatePlayerNames( HashSet<Player> players) {
		this.players = players;
	}
	
	public HashSet<Player> getPlayers(){
		return players;
	}
}

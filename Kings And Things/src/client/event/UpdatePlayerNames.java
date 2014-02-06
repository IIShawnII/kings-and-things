package client.event;

import java.util.HashSet;

import common.Player;
import common.event.AbstractEvent;

public class UpdatePlayerNames extends AbstractEvent {
	
	private static final long serialVersionUID = -4621893742217122759L;
	
	HashSet< Player> players;
	
	public UpdatePlayerNames( HashSet< Player> players) {
		this.players = players;
	}
	
	public HashSet< Player> getPlayers(){
		return players;
	}
}

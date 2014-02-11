package client.event;

import common.event.AbstractEvent;
import common.game.PlayerInfo;

public class UpdatePlayer extends AbstractEvent {
	
	private PlayerInfo current;
	private PlayerInfo[] players;
	
	public UpdatePlayer( PlayerInfo[] players) {
		this.players = players;
	}
	
	public UpdatePlayer( PlayerInfo[] players, PlayerInfo current) {
		this.players = players;
		this.current = current;
	}

	public PlayerInfo[] getPlayers(){
		return players;
	}

	public PlayerInfo getCurrent(){
		return current;
	}
}

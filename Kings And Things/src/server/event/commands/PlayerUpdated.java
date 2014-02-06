package server.event.commands;

import common.Player;
import common.event.AbstractEvent;

public class PlayerUpdated extends AbstractEvent {

	private static final long serialVersionUID = -6795903370044472872L;
	
	Player player;
	
	public PlayerUpdated( Player player) {
		this.player = player;
	}

	public Player getPlayer(){
		return player;
	}
}

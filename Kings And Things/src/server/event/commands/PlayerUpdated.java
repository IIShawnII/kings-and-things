package server.event.commands;

import server.logic.game.Player;
import common.event.AbstractEvent;

public class PlayerUpdated extends AbstractEvent {
	
	Player player;
	
	public PlayerUpdated( Player player) {
		this.player = player;
	}

	public Player getPlayer(){
		return player;
	}
}

package server.event.commands;

import server.logic.game.Player;

public class PlayerUpdated extends AbstractCommand {
	
	Player player;
	
	public PlayerUpdated( Player player) {
		this.player = player;
	}

	public Player getPlayer(){
		return player;
	}
}

package server.event.commands;

import common.event.AbstractInternalEvent;
import server.logic.game.Player;

public class PlayerUpdated extends AbstractInternalEvent {
	
	Player player;
	
	public PlayerUpdated( Player player, final Object OWNER){
		super( OWNER);
		this.player = player;
	}

	public Player getPlayer(){
		return player;
	}
}

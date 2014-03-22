package server.event;

import common.event.AbstractInternalEvent;
import server.logic.game.Player;

public class PlayerUpdated extends AbstractInternalEvent {
	
	private final Player player;
	
	public PlayerUpdated( Player player){
		super();
		this.player = player;
	}

	public Player getPlayer(){
		return player;
	}
}

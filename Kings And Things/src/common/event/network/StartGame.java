package common.event.network;

import common.event.AbstractNetwrokEvent;

public class StartGame extends AbstractNetwrokEvent{

	private static final long serialVersionUID = -7949177339580975129L;
	
	private int playerCount = 0;
	
	public StartGame( int playerCount){
		this.playerCount = playerCount;
	}

	@Override
	public String toString(){
		return "Network/StartGame: Start Game";
	}

	public int getPlayerCount() {
		return playerCount;
	}
}

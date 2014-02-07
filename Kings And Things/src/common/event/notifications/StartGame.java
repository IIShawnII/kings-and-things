package common.event.notifications;

import common.event.AbstractNetwrokEvent;

public class StartGame extends AbstractNetwrokEvent{

	private static final long serialVersionUID = -7949177339580975129L;

	@Override
	public String toString(){
		return "Network/StartGame: Start Game";
	}
}

package client.event;

import common.Constants.NetwrokAction;
import common.event.AbstractEvent;

public class ConnectionState extends AbstractEvent {
	
	private NetwrokAction action;
	
	private String message;
	private int playerCount = 0;
	
	public ConnectionState( int playerCount, String message, NetwrokAction action){
		this.message = message;
		this.action = action;
		this.playerCount = playerCount;
	}
	
	public ConnectionState( NetwrokAction action){
		this( 0, null, action);
	}
	
	public ConnectionState( int playerCount){
		this( playerCount, null, NetwrokAction.StartGame);
	}
	
	public String getMessage() {
		return message;
	}
	
	public NetwrokAction getAction(){
		return action;
	}

	public int getPlayerCount() {
		return playerCount;
	}
}

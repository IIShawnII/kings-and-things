package client.event;

import common.event.AbstractEvent;

public class ConnectionState extends AbstractEvent {
	
	private String message;
	private boolean startGame = false;
	private boolean isConnected = false;
	
	public ConnectionState( String message, boolean isConnected){
		this.message = message;
		this.isConnected = isConnected;
	}
	
	public ConnectionState( boolean startGame){
		this( null, true);
		this.startGame = startGame;
	}
	
	public String getMessage() {
		return message;
	}

	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean startGame(){
		return startGame;
	}
}

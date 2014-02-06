package client.event;

import common.event.AbstractEvent;

public class ConnectionState extends AbstractEvent {
	
	private static final long serialVersionUID = 8361038387154903329L;
	
	private String message;
	private boolean isConnected = false;
	
	public ConnectionState( String message, boolean isConnected){
		this.message = message;
		this.isConnected = isConnected;
	}
	
	public String getMessage() {
		return message;
	}

	public boolean isConnected() {
		return isConnected;
	}
}

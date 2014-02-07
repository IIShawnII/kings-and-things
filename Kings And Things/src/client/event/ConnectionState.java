package client.event;

import common.Constants.NetwrokAction;
import common.event.AbstractEvent;

public class ConnectionState extends AbstractEvent {
	
	private NetwrokAction action;
	
	private String message;
	
	public ConnectionState( String message, NetwrokAction action){
		this.message = message;
		this.action = action;
	}
	
	public ConnectionState( NetwrokAction action){
		this( null, action);
	}
	
	public String getMessage() {
		return message;
	}
	
	public NetwrokAction getAction(){
		return action;
	}
}

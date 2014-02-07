package client.event;

import common.Constants.NetwrokAction;
import common.event.AbstractEvent;

public class ConnectionAction extends AbstractEvent {
	
	private int port;
	private String address, name;
	private NetwrokAction action;
	
	private ConnectionAction( String name, String address, int port, NetwrokAction action){
		this.port = port;
		this.address = address;
		this.name = name;
		this.action = action;
	}
	
	public ConnectionAction( String name, String address, int port){
		this( name, address, port, NetwrokAction.Connect);
	}
	
	public ConnectionAction( NetwrokAction action){
		this( null, null, -1, action);
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}
	
	public NetwrokAction getAction(){
		return action;
	}
	
	public String getName(){
		return name;
	}
}

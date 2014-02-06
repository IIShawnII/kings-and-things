package client.event;

import common.event.AbstractEvent;

public class ConnectionAction extends AbstractEvent {
	
	private static final long serialVersionUID = 3228653739084697727L;
	
	private int port;
	private String address;
	private boolean shouldConnect;
	
	public ConnectionAction( String address, int port){
		this.port = port;
		this.address = address;
		shouldConnect = true;
	}
	
	public ConnectionAction(){
		shouldConnect = false;
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}
	
	public boolean shouldConnect(){
		return shouldConnect;
	}
}

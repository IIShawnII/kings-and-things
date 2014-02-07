package client.event;

import common.event.AbstractEvent;

public class ConnectionAction extends AbstractEvent {
	
	private int port;
	private String address, name;
	private boolean shouldConnect;
	
	public ConnectionAction( String name, String address, int port){
		this.port = port;
		this.address = address;
		this.name = name;
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
	
	public String getName(){
		return name;
	}
}

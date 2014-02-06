package client.event;

public class ConnectionAction extends AbstractCommand {
	
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

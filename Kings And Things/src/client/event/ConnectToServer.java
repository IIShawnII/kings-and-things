package client.event;

public class ConnectToServer extends AbstractCommand {
	
	private int port;
	private String address;
	private boolean isConnected = false;
	
	public ConnectToServer( String address, int port){
		this.port = port;
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected( boolean isConnected) {
		this.isConnected = isConnected;
	}
}

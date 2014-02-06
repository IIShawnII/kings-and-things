package client.event;

public class ConnectToServer extends AbstractCommand {
	
	private int port;
	private String address;
	
	public ConnectToServer( String address, int port){
		super();
		this.port = port;
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}
}

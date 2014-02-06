package client.event;

public class ConnectionState extends AbstractCommand {
	
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

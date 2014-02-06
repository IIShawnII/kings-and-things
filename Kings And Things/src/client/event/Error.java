package client.event;


public class Error extends AbstractCommand {

	private String message;
	
	public Error( String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}

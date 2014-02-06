package client.event;


public class Error extends AbstractCommand {

	private String message, title;
	
	public Error( String title, String message){
		this.title = title;
		this.message = message;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getMessage(){
		return message;
	}
}

package common.event.notifications;

public class Error extends AbstractNotification {
	private final String message;
	
	public Error( String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}

package common.event.notifications;

public class ErrorNotification extends AbstractNotification {
	private final String message;
	
	public ErrorNotification( String message){
		super();
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}

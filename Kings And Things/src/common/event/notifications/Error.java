package common.event.notifications;

import common.event.AbstractEvent;

public class Error extends AbstractEvent {
	
	private static final long serialVersionUID = 1860898514523397849L;
	
	final String message, title;
	
	public Error( String message, String title){
		this.title = title;
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getTitle(){
		return title;
	}
}

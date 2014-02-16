package common.event;

import common.Constants.Level;

public class ConsoleMessage extends AbstractEvent {

	private String message;
	private Level level;
	
	public ConsoleMessage( String message, Level level){
		this.message = message; 
		this.level = level;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Level getLevel(){
		return level;
	}
}

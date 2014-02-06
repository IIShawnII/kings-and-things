package common.event.notifications;

public class PlayerReady extends AbstractNotification{
	
	private String name;
	
	public PlayerReady( String playerName){
		name = playerName;
	}

	public String getName(){
		return name;
	}
}

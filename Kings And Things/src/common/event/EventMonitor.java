package common.event;

import java.util.HashMap;
import static common.Constants.Level;

public class EventMonitor {
	
	private static final EventMonitor dispatch;
	
	private HashMap< Integer, EventHandler> handlers;
	
	static{
		dispatch = new EventMonitor();
	}

	private EventMonitor() {
		handlers = new HashMap<>();
	}
	
	public synchronized static void register( final int ID, EventHandler handler){
		dispatch.handlers.put( ID, handler);
	}
	
	public synchronized static void fireEvent( final int ID, String message, Level level){
		dispatch.handlers.get( ID).handle( message, level);
	}
}

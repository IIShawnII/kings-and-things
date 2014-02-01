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
	
	public synchronized static void unRegister( final int ID){
		dispatch.handlers.remove( ID);
	}
	
	public synchronized static void fireEvent( final int ID, Object obj, Level level){
		dispatch.handlers.get( ID).handle( obj, level);
	}
}

package common.event;

import static common.Constants.Level;

/**
 * any GUI object that must responds to events must implement this interface
 */
public interface EventHandler {
	
	/**
	 * will be called by EventDispatch when there is need for GUI update
	 * @param message - any message to be sent to GUI, can be null
	 */
	public void handel( String message, Level level); 
}

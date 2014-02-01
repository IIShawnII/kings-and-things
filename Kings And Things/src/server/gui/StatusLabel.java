package server.gui;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;

import common.event.EventHandler;
import common.event.EventMonitor;
import static common.Constants.Level;

/**
 * GUI block to show the state of a function being done by server
 */
@SuppressWarnings("serial")
public class StatusLabel extends JLabel implements EventHandler{
	
	private boolean isOff = true;
	private Color ON, OFF;

	protected StatusLabel() {
		super();
	}
	
	/**
	 * create complete StatusLabel GUI, and register to receive even updates
	 * @param tooltip - description of label, if null no tooltip
	 * @param label - text to be displayed, optimized for on letter
	 * @param on - color for on status or active, cannot be null
	 * @param off - color for off status or inactive, cannot be null
	 * @param font - new font to be used for display, if null default font will be used
	 * @param EVENT_ID - must be a valid positive integer
	 * @param size - preferred size of the component
	 * @throws NullPointerException if either of on or off argument are null
	 */
	protected void initialize( String tooltip, String label, Color on, Color off, Font font, final int EVENT_ID, Dimension size){
		String message = null;
		if( EVENT_ID < 0){
			message = "ERROR - EVENT_ID must be a positve integer";
		}else if( on==null || off==null){
			message = "ERROR - StatusLabel colors for on/off cannot be null";
		}
		if(message!=null){
			throw new NullPointerException( message);
		}
		
		setPreferredSize( size);
		ON = on;
		OFF = off;
		setOpaque( true);
		if( font!=null){
			setFont( font);
		}
		if( label!=null){
			setText( label);
		}
		setBackground( OFF);
		if( tooltip!=null){
			setToolTipText( tooltip);
		}
		setVerticalAlignment( JLabel.CENTER);
		setHorizontalAlignment( JLabel.CENTER);
		EventMonitor.register( EVENT_ID, this);
	}
	
	/**
	 * change the state of Label to on if off and vice versa
	 */
	protected void changeState(){
		isOff = !isOff;
		setBackground( isOff?OFF:ON);
	}

	/**
	 * triggered when appropriate event is fired
	 */
	@Override
	public void handle( Object obj, Level level) {
		changeState();
	}
}
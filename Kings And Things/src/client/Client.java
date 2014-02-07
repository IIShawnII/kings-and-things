package client;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import org.apache.log4j.PropertyConfigurator;

import client.logic.Logic;
import client.gui.ClientGUI;
import common.event.EventDispatch;
import common.network.Connection;
import static common.Constants.GAME_TITLE;

/**
 * main class for starting the server
 */
public class Client {
	
	public static void main( String[] args) {
		PropertyConfigurator.configure("Log Settings\\clientLog4j.properties");
		//update look and feeling of application to its operating system
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
		} catch ( Exception e) {
			//failed to change look and feel
		}
		
		try {
			Logic logic = new Logic( new Connection());
			EventDispatch.registerForCommandEvents( logic);
			new Thread( logic, "Client Logic").start();
			ClientGUI clientGUI = new ClientGUI( GAME_TITLE);
			SwingUtilities.invokeLater( clientGUI);
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}
}

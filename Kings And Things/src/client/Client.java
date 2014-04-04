package client;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import org.apache.log4j.PropertyConfigurator;

import common.Constants;

import client.logic.ConnectionLogic;
import client.gui.ClientGUI;

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
		
		ConnectionLogic logic = new ConnectionLogic();
		new Thread( logic, "Client Logic").start();
		ClientGUI clientGUI = new ClientGUI( Constants.GAME_TITLE);
		SwingUtilities.invokeLater( clientGUI);
	}
}

package client;

import static common.Constants.CONSOLE;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;

import client.gui.ClientGUI;

import common.network.Connection;

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
		
		ClientGUI clientGUI;
		Connection connection = new Connection( CONSOLE);
		if( args!=null && args.length>=1){
			clientGUI = new ClientGUI( args[0]);
		}else{
			clientGUI = new ClientGUI( "Kings And Things");
		}
		clientGUI.setConnection( connection);
		//start GUI on AWT Thread
		SwingUtilities.invokeLater( clientGUI);
	}
}

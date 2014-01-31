package client;

import static common.Constants.CONSOLE;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import common.network.Connection;

import client.gui.ClientGUI;

/**
 * main class for starting the server
 */
public class Client {
	
	public static void main( String[] args) {
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

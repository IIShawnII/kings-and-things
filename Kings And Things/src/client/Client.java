package client;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

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
		
		ClientGUI ClientGUI;
		if( args!=null && args.length>=1){
			ClientGUI = new ClientGUI( args[0]);
		}else{
			ClientGUI = new ClientGUI( "Kings And Things");
		}
		//start GUI on AWT Thread
		SwingUtilities.invokeLater( ClientGUI);
	}
}

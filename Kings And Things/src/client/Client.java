package client;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import org.apache.log4j.PropertyConfigurator;

import client.logic.Logic;
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
		if( args!=null && args.length>=1){
			clientGUI = new ClientGUI( args[0]);
		}else{
			clientGUI = new ClientGUI( "Kings And Things");
		}
		//start GUI on AWT Thread
		SwingUtilities.invokeLater( clientGUI);
		while( !clientGUI.isVisible()){
			try {
				//wait for server to become visible
				Thread.sleep( 500);
			} catch ( InterruptedException e) {}
		}
		
		//CommandEventBus.BUS.register( new Logic( new Connection()));
		
		try {
			Logic logic = new Logic( new Connection());
			new Thread( logic, "Client Logic").start();
		} catch ( Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep( 2000);
			} catch ( InterruptedException e1) {}
			clientGUI.dispose();
		}
	}
}

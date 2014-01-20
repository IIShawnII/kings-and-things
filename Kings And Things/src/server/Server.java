package server;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import server.logic.Logic;
import server.gui.ServerGUI;

/**
 * main class for starting the server
 */
public class Server {
	
	public static void main( String[] args) {
		//update look and feeling of application to its operating system
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
		} catch ( Exception e) {
			//failed to change look and feel
		}
		
		ServerGUI serverGUI;
		if( args!=null && args.length>=1){
			serverGUI = new ServerGUI( args[0]);
		}else{
			serverGUI = new ServerGUI( "Kings And Things Server");
		}
		
		//start GUI on AWT Thread
		SwingUtilities.invokeLater( serverGUI);
		while( !serverGUI.isVisible()){
			try {
				//wait for server to become visible
				Thread.sleep( 500);
			} catch ( InterruptedException e) {}
		}

		try {
			Logic logic = new Logic();
			new Thread( logic, "GAME LOGIC").start();
		} catch ( Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep( 2000);
			} catch ( InterruptedException e1) {}
			serverGUI.dispose();
		}
	}
}

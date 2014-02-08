package server;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;

import server.gui.ServerGUI;
import server.logic.ConnectionLobby;

/**
 * main class for starting the server
 */
public class Server {
	
	public static void main( String[] args) {
		PropertyConfigurator.configure("Log Settings\\serverLog4j.properties");
		//update look and feeling of application to its operating system
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
		} catch ( Exception e) {
			//failed to change look and feel
		}
		
		//TODO change true to false
		boolean isDemoMode = true;
		String serverGUITitle = "Kings And Things Server";
		
		ServerGUI serverGUI;
		if( args!=null && args.length>=1)
		{
			for(int i=0; i<args.length; i++)
			{
				switch(args[i])
				{
					case "-title":
						serverGUITitle = args[i+1];
						break;
					case "-demo":
						isDemoMode = true;
						break;
					default:
						break;
				}
			}
			serverGUI = new ServerGUI( args[0]);
		}

		serverGUI = new ServerGUI( serverGUITitle);
		//start GUI on AWT Thread
		SwingUtilities.invokeLater( serverGUI);
		while( !serverGUI.isVisible()){
			try {
				//wait for server to become visible
				Thread.sleep( 500);
			} catch ( InterruptedException e) {}
		}

		try {
			ConnectionLobby lobby = new ConnectionLobby(isDemoMode);
			new Thread( lobby, "GAME LOGIC").start();
		} catch ( Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep( 2000);
			} catch ( InterruptedException e1) {}
			serverGUI.dispose();
		}
	}
}

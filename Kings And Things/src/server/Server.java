package server;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;

import common.event.EventDispatch;
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

		/*try {
			new StateGenerator( "save", false);
		} catch ( ClassNotFoundException | IOException e2) {
			e2.printStackTrace();
		}*/
		
		boolean isDemoMode = false;
		boolean generateStateFile = false;
		boolean loadStateFile = false;
		String stateFileName = null;
		boolean generateAll = false;
		
		String serverGUITitle = "Kings And Things Server";
		if( args!=null){
			for(int i=0; i<args.length; i++){
				if( args[i].equals("-t")){
					serverGUITitle = args[++i];
				}else if( args[i].equals("-demo")){
					isDemoMode = true;
				}if( args[i].equals("-gsf")){
					generateStateFile = true;
					stateFileName = args[++i];
				}if( args[i].equals("-all")){
					generateAll = true;
				}if( args[i].equals("-lst")){
					loadStateFile = true;
					stateFileName = args[++i];
				}
				/*switch(args[i]){
					case "-t":
						serverGUITitle = args[++i];
						break;
					case "-demo":
						isDemoMode = true;
						break;
					case "-gsf":
						generateStateFile = true;
						stateFileName = args[++i];
						break;
					case "-all":
						generateAll = true;
						break;
					case "-lsf":
						loadStateFile = true;
						stateFileName = args[++i];
						break;
					default:
						break;
				}*/
			}
		}
		ServerGUI serverGUI = new ServerGUI( serverGUITitle);
		EventDispatch.registerOnInternalEvents( serverGUI);
		//start GUI on AWT Thread
		SwingUtilities.invokeLater( serverGUI);
		while( !serverGUI.isVisible()){
			try {
				//wait for server to become visible
				Thread.sleep( 500);
			} catch ( InterruptedException e) {}
		}

		try {
			ConnectionLobby lobby = new ConnectionLobby(isDemoMode, loadStateFile, generateStateFile, stateFileName, generateAll);
			EventDispatch.registerOnInternalEvents(lobby);
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

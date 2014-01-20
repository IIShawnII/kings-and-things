package server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import common.Constants.Level;
import common.network.Connection;
import common.event.EventHandler;
import common.event.EventMonitor;
import static common.Constants.ENDGAME;
import static common.Constants.CONSOLE;
import static common.Constants.SERVER_PORT;
import static common.Constants.SERVER_TIMEOUT;

public class Logic implements Runnable, EventHandler {

	private boolean close = false;
	private ServerSocket serverSocket;
	
	public Logic() throws IOException{
		try {
			serverSocket = new ServerSocket( SERVER_PORT);
            serverSocket.setSoTimeout( SERVER_TIMEOUT*1000);
			EventMonitor.fireEvent( CONSOLE, "Listening on port " + SERVER_PORT, Level.Notice);
		} catch ( IOException e) {
			EventMonitor.fireEvent( CONSOLE, "Failed to open port " + SERVER_PORT, Level.Error);
			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
			throw e;
		}
	}

	@Override
	public void run() {
		EventMonitor.register( ENDGAME, this);
		while( !close){
            try (Connection connection = new Connection( serverSocket.accept(), CONSOLE)){
	    		EventMonitor.fireEvent( CONSOLE, "Recieved connection from " + connection, Level.Notice);
	    		String str;
	    		while ((str = connection.recieve())!=null){
	    			connection.send( new StringBuilder( str).reverse().toString());
	    		}
            } catch( SocketTimeoutException ex){
                //try again for incoming connections
            } catch ( IOException e) {
    			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
				e.printStackTrace();
			}
        }
		try {
			serverSocket.close();
		} catch ( IOException e) {
			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
			e.printStackTrace();
		}
	}

	@Override
	public void handel( String message, Level level) {
		close = true;
	}
}

package server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import common.Constants.Level;
import common.network.Connection;
import common.event.EventHandler;
import common.event.EventMonitor;
import static common.Constants.PLAYER;
import static common.Constants.ENDGAME;
import static common.Constants.CONSOLE;
import static common.Constants.PLAYER_INC;
import static common.Constants.MIN_PLAYERS;
import static common.Constants.MAX_PLAYERS;
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
		int count=0, playerID = PLAYER;
		while( !close && count<MAX_PLAYERS){
            try {
            	Connection connection = new Connection( serverSocket.accept(), playerID+CONSOLE);
            	EventMonitor.fireEvent( CONSOLE, "Player count is " + count + " out of " + MAX_PLAYERS + " players", Level.Notice);
            	EventMonitor.fireEvent( CONSOLE, "Still need minimum of " + (MIN_PLAYERS-count) + " players", Level.Notice);
	    		EventMonitor.fireEvent( CONSOLE, "Recieved connection from " + connection, Level.Notice);
	    		EventMonitor.fireEvent( CONSOLE, connection + " is assigned to Player " + count, Level.Notice);
	    		new PlayerConnection( playerID, connection).start();
            	count++;
            	playerID+=PLAYER_INC;
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
	public void handle( String message, Level level) {
		close = true;
	}
}

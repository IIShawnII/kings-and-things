package server.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import common.Constants.Level;
import common.Logger;
import common.network.Connection;
import common.event.EventHandler;
import common.event.EventMonitor;
import static common.Constants.PLAYER;
import static common.Constants.ENDGAME;
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
			Logger.getStandardLogger().info("Listening on port " + SERVER_PORT);
		} catch ( IOException e) {
			Logger.getErrorLogger().error("Failed to open port " + SERVER_PORT, e);
			throw e;
		}
	}

	@Override
	public void run() {
		EventMonitor.register( ENDGAME, this);
		int count=0, playerID = PLAYER;
		while( !close && count<MAX_PLAYERS){
            try {
            	Connection connection = new Connection( serverSocket.accept());
            	
            	Logger.getStandardLogger().info("Player count is " + count + " out of " + MAX_PLAYERS + " players");
            	Logger.getStandardLogger().info("Still need minimum of " + (MIN_PLAYERS-count) + " players");
            	Logger.getStandardLogger().info("Recieved connection from " + connection);
            	Logger.getStandardLogger().info(connection + " is assigned to Player " + count);
            	
	    		new PlayerConnection( playerID, connection).start();
            	count++;
            	playerID+=PLAYER_INC;
            } catch( SocketTimeoutException ex){
                //try again for incoming connections
            } catch ( IOException e) {
            	Logger.getErrorLogger().error("Problem with player connections: ", e);
			}
        }
		try {
			serverSocket.close();
		} catch ( IOException e) {
			Logger.getErrorLogger().error("Problem closing player connections: ", e);
		}
	}

	@Override
	public void handle( Object obj, Level level) {
		close = true;
	}
}

package server.logic;

import static common.Constants.PLAYER;
import static common.Constants.MAX_PLAYERS;
import static common.Constants.MIN_PLAYERS;
import static common.Constants.PLAYER_INC;
import static common.Constants.SERVER_PORT;
import static common.Constants.SERVER_TIMEOUT;

import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import com.google.common.eventbus.Subscribe;

import server.event.commands.EndServer;
import server.event.commands.PlayerUpdated;
import server.event.commands.StartGameCommand;
import server.logic.game.GameFlowManager;
import common.Logger;
import common.LoadResources;
import common.network.Connection;
import common.event.EventDispatch;
import common.event.notifications.PlayerConnected;

public class ConnectionLobby implements Runnable {

	private boolean close = false;
	private ServerSocket serverSocket;
	private final GameFlowManager game;
	private final ArrayList<PlayerConnection> connectedPlayers;
	private final boolean demoMode;
	
	public ConnectionLobby(boolean isDemoMode) throws IOException{
		if(isDemoMode)
		{
			Logger.getStandardLogger().info("Server started in demo mode.");
		}
		LoadResources lr = new LoadResources();
		lr.run();
		
		demoMode = isDemoMode;
		connectedPlayers = new ArrayList<>();
		game = new GameFlowManager();
		
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
		EventDispatch.registerForCommandEvents(this);
		game.initialize();
		int count=0, playerID = PLAYER;
		while( !close && count<MAX_PLAYERS){
            try {
            	Connection connection = new Connection( serverSocket.accept());
            	
            	Logger.getStandardLogger().info("Player count is " + count + " out of " + MAX_PLAYERS + " players");
            	Logger.getStandardLogger().info("Still need minimum of " + (MIN_PLAYERS-count) + " players");
            	Logger.getStandardLogger().info("Recieved connection from " + connection);
            	Logger.getStandardLogger().info(connection + " is assigned to Player " + count);
            	
            	PlayerConnection pc = new PlayerConnection("Player " + playerID, playerID, connection);
            	pc.start();
            	if( connectedPlayers.size()>=1){
            		//update player list for new players who just joined but not ready yet
            		playerUpdated( null);
            	}
            	connectedPlayers.add(pc);
            	
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
	
	@Subscribe
	public void playerUpdated( PlayerUpdated player){
		boolean unreadyPlayerConnected = false;
		PlayerConnected connections = new PlayerConnected();
		for( PlayerConnection pc : connectedPlayers){
			unreadyPlayerConnected = !pc.isReadyToStart();
			connections.addPlayer( pc.getPlayer());
		}
		connections.postCommand();
		if( !unreadyPlayerConnected){
			new StartGameCommand( demoMode, connections.getPlayers()).postCommand();
		}
	}
	
	@Subscribe
	public void endServer( EndServer end){
		close = true;
	}
}
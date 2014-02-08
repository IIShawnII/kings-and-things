package server.logic;

import static common.Constants.PLAYER;
import static common.Constants.MAX_PLAYERS;
import static common.Constants.MIN_PLAYERS;
import static common.Constants.PLAYER_INC;
import static common.Constants.SERVER_PORT;
import static common.Constants.SERVER_TIMEOUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import com.google.common.eventbus.Subscribe;

import server.logic.game.GameFlowManager;
import server.logic.game.Player;
import server.event.commands.EndServer;
import server.event.commands.PlayerUpdated;
import server.event.commands.StartGameCommand;
import common.Logger;
import common.network.Connection;
import common.event.EventDispatch;
import common.event.notifications.PlayerState;
import common.event.notifications.PlayersList;
import common.event.notifications.StartGame;
import common.game.LoadResources;
import common.game.PlayerInfo;

public class ConnectionLobby implements Runnable {

	private boolean close = false;
	private ServerSocket serverSocket;
	private final GameFlowManager game;
	private final ArrayList< PlayerConnection> connectedPlayers;
	private final boolean demoMode;
	
	public ConnectionLobby(boolean isDemoMode) throws IOException{
		if(isDemoMode){
			Logger.getStandardLogger().info("Server started in demo mode.");
		}
		LoadResources lr = new LoadResources( false);
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
		boolean oldClient = false;
		while( !close && count<MAX_PLAYERS){
            try {
            	oldClient = false;
            	Connection connection = new Connection( serverSocket.accept());
            	PlayerInfo info = ((PlayerState)connection.recieve()).getPlayer();
            	for( PlayerConnection pc : connectedPlayers){
            		if( pc.equals( info)){
            			oldClient = true;
            			pc.setConnection( connection);
            			startTask( pc, pc.getName());
            			Logger.getStandardLogger().info("Restablished connection from " + connection + ", assigned to " + pc.getPlayer());
            			break;
            		}
            	}
            	if( !oldClient){
            		info = new PlayerInfo( info, playerID);
	            	Player player = new Player( new PlayerInfo( info, playerID));
	            	PlayerConnection pc = new PlayerConnection( player, connection);
	            	EventDispatch.registerForNotificationEvents( pc);
	            	startTask( pc, pc.getName());
	            	pc.sendNotificationToClient( new PlayerState( info));
	            	connectedPlayers.add( pc);
	            	count++;
	            	playerID+=PLAYER_INC;
	            	Logger.getStandardLogger().info("Recieved connection from " + connection + ", assigned to " + pc.getPlayer());
            	}
            	Logger.getStandardLogger().info("Player count is " + count + " out of " + MAX_PLAYERS + " players, need players: " + (MAX_PLAYERS-count));
            	playerUpdated( null);
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
	
	private void startTask( Runnable task, String name){
		new Thread( task, name).start();
	}
	
	@Subscribe
	public void playerUpdated( PlayerUpdated player){
		boolean anyUnReady = false;
		PlayersList connections = new PlayersList();
		for( PlayerConnection pc : connectedPlayers){
			anyUnReady = anyUnReady? true : !pc.isReadyToStart();
			if( pc.isConnected()){
				connections.addPlayer( pc.getPlayerInfo());
			}
		}
		if( connections.getPlayers().size()>0){
			connections.postNotification();
		}
		if( !anyUnReady && connectedPlayers.size()>=MIN_PLAYERS && connectedPlayers.size()<=MAX_PLAYERS){
			HashSet< Player> set = new HashSet<>();
			for( PlayerConnection pc : connectedPlayers){
				set.add( pc.getPlayer());
			}
			new StartGame().postNotification();
			new StartGameCommand( demoMode, set).postCommand();
		}
	}
	
	@Subscribe
	public void endServer( EndServer end){
		close = true;
	}
}
package server.logic;

import static common.Constants.PLAYER_START_ID;
import static common.Constants.PLAYER_ID_MULTIPLIER;
import static common.Constants.MAX_PLAYERS;
import static common.Constants.SERVER_PORT;
import static common.Constants.SERVER_TIMEOUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.google.common.eventbus.Subscribe;

import server.logic.game.Player;
import server.logic.game.CommandHandlerManager;
import server.event.EndServer;
import server.event.PlayerUpdated;
import server.event.commands.StartSetupPhaseCommand;
import common.Logger;
import common.Constants;
import common.Constants.Level;
import common.game.PlayerInfo;
import common.game.LoadResources;
import common.network.Connection;
import common.event.EventDispatch;
import common.event.ConsoleMessage;
import common.event.notifications.StartGame;
import common.event.notifications.PlayerState;
import common.event.notifications.PlayersList;

public class ConnectionLobby implements Runnable {

	private boolean close = false;
	private ServerSocket serverSocket;
	private final CommandHandlerManager game;
	private final ArrayList< PlayerConnection> connectedPlayers;
	private final boolean demoMode;
	
	public ConnectionLobby( boolean isDemoMode) throws IOException{
		if( isDemoMode){
			Logger.getStandardLogger().info("Server started in demo mode.");
			new ConsoleMessage( "Starting in demo mode.", Level.Notice, this).postInternalEvent();
		}
		
		demoMode = isDemoMode;
		connectedPlayers = new ArrayList<>();
		game = new CommandHandlerManager();
	}

	@Override
	public void run() {
		new ConsoleMessage( "Loading Resources", Level.Plain, this).postInternalEvent();
		new LoadResources( false).run();
		new ConsoleMessage( "Loaded Resources", Level.Plain, this).postInternalEvent();
		try {
			serverSocket = new ServerSocket( SERVER_PORT);
            serverSocket.setSoTimeout( SERVER_TIMEOUT*1000);
			Logger.getStandardLogger().info("Listening on port " + SERVER_PORT);
			new ConsoleMessage( "Listening on port " + SERVER_PORT, Level.Notice, this).postInternalEvent();
		} catch ( IOException e) {
			Logger.getErrorLogger().error("Failed to open port " + SERVER_PORT, e);
			new ConsoleMessage( "Failed to open port " + SERVER_PORT + ", Restart Server", Level.Error, this).postInternalEvent();
			return;
		}
		game.initialize();
		int count=0, playerID = PLAYER_START_ID;
		boolean oldClient = false;
		Socket socket = null;
		Connection connection = null;
		PlayerState playerState = null;
		PlayerInfo info = null;
		PlayerConnection pc = null;
		Player player = null;
		while( !close && count<MAX_PLAYERS){
            try {
            	oldClient = false;
            	connection = new Connection();
            	socket = serverSocket.accept();
            	if( connection.connectTo( socket)){
            		playerState = (PlayerState)connection.recieve();
            	}else{
            		new ConsoleMessage( "Connection to: " + socket + " failed", Level.Warning,this).postInternalEvent();
            		Logger.getStandardLogger().warn("Connection to: " + socket + " failed");
            		connection.disconnect();
            		socket.close();
            		continue;
            	}
            	info = playerState.getPlayer();
            	for( PlayerConnection playerConnection : connectedPlayers){
            		if( playerConnection.equals( info)){
            			oldClient = true;
            			playerConnection.setConnection( connection);
            			startTask( playerConnection, playerConnection.getName());
            			new ConsoleMessage( "Restablished connection from " + connection + ", assigned to " + playerConnection.getPlayer(), Level.Notice, this).postInternalEvent();
            			Logger.getStandardLogger().info("Restablished connection from " + connection + ", assigned to " + playerConnection.getPlayer());
            			break;
            		}
            	}
            	if( !oldClient){
            		info = new PlayerInfo( info, playerID);
	            	player = new Player( new PlayerInfo( info, playerID));
	            	pc = new PlayerConnection( player, connection);
	            	EventDispatch.registerOnNetwrokEvents( pc);
	            	startTask( pc, pc.getName());
	            	//send PlayerInfo object to connected player
	            	pc.sendNotificationToClient( new PlayerState( info));
	            	connectedPlayers.add( pc);
	            	count++;
	            	playerID*=PLAYER_ID_MULTIPLIER;
        			new ConsoleMessage( "Recieved connection from " + connection + ", assigned to " + pc.getPlayer(), Level.Notice, this).postInternalEvent();
	            	Logger.getStandardLogger().info("Recieved connection from " + connection + ", assigned to " + pc.getPlayer());
            	}
    			new ConsoleMessage( "Player count is " + count + " out of " + MAX_PLAYERS + " players, need players: " + (MAX_PLAYERS-count), Level.Notice, this).postInternalEvent();
            	Logger.getStandardLogger().info("Player count is " + count + " out of " + MAX_PLAYERS + " players, need players: " + (MAX_PLAYERS-count));
            	playerUpdated( null);
            } catch( SocketTimeoutException ex){
                //try again for incoming connections
            } catch ( IOException ex) {
    			new ConsoleMessage( "Problem with player connection", Level.Error, this).postInternalEvent();
            	Logger.getErrorLogger().error("Problem with player connection: ", ex);
			} catch ( ClassNotFoundException ex) {
    			new ConsoleMessage( "Recieved Invalid Package: " + ex.getMessage(), Level.Error, this).postInternalEvent();
            	Logger.getErrorLogger().error("Recieved Invalid Package: ", ex);
			}
        }
		try {
			serverSocket.close();
		} catch ( IOException e) {
			new ConsoleMessage( "Problem closing player connections", Level.Error, this).postInternalEvent();
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
		if( connections.getPlayers().length>0){
			connections.postNetworkEvent();
		}
		if( !anyUnReady && ((connectedPlayers.size()==MAX_PLAYERS)||Constants.BYPASS_MIN_PLAYER)){
			HashSet< Player> set = new HashSet<>();
			for( PlayerConnection pc : connectedPlayers){
				set.add( pc.getPlayer());
			}
			new StartGame( Constants.BYPASS_MIN_PLAYER? MAX_PLAYERS:set.size()).postNetworkEvent();
			new StartSetupPhaseCommand( demoMode, set, this).postInternalEvent();
		}
	}
	
	@Subscribe
	public void endServer( EndServer end){
		close = true;
	}
}
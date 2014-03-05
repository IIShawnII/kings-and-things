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
import java.net.SocketTimeoutException;

import com.google.common.eventbus.Subscribe;

import server.logic.game.Player;
import server.logic.game.CommandHandlerManager;
import server.event.commands.EndServer;
import server.event.commands.PlayerUpdated;
import server.event.commands.StartGameCommand;
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
			new ConsoleMessage( "Starting in demo mode.", Level.Notice).postCommand();
		}
		
		demoMode = isDemoMode;
		connectedPlayers = new ArrayList<>();
		game = new CommandHandlerManager();
	}

	@Override
	public void run() {
		new ConsoleMessage( "Loading Resources", Level.Plain).postCommand();
		new LoadResources( false).run();
		new ConsoleMessage( "Loaded Resources", Level.Plain).postCommand();
		try {
			serverSocket = new ServerSocket( SERVER_PORT);
            serverSocket.setSoTimeout( SERVER_TIMEOUT*1000);
			Logger.getStandardLogger().info("Listening on port " + SERVER_PORT);
			new ConsoleMessage( "Listening on port " + SERVER_PORT, Level.Notice).postCommand();
		} catch ( IOException e) {
			Logger.getErrorLogger().error("Failed to open port " + SERVER_PORT, e);
			new ConsoleMessage( "Failed to open port " + SERVER_PORT + ", Restart Server", Level.Error).postCommand();
			return;
		}
		game.initialize();
		int count=0, playerID = PLAYER_START_ID;
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
            			new ConsoleMessage( "Restablished connection from " + connection + ", assigned to " + pc.getPlayer(), Level.Notice).postCommand();
            			Logger.getStandardLogger().info("Restablished connection from " + connection + ", assigned to " + pc.getPlayer());
            			break;
            		}
            	}
            	if( !oldClient){
            		info = new PlayerInfo( info, playerID);
	            	Player player = new Player( new PlayerInfo( info, playerID));
	            	PlayerConnection pc = new PlayerConnection( player, connection);
	            	EventDispatch.registerOnNetwrokEvents( pc);
	            	startTask( pc, pc.getName());
	            	//send PlayerInfo object to connected player
	            	pc.sendNotificationToClient( new PlayerState( info));
	            	connectedPlayers.add( pc);
	            	count++;
	            	playerID*=PLAYER_ID_MULTIPLIER;
        			new ConsoleMessage( "Recieved connection from " + connection + ", assigned to " + pc.getPlayer(), Level.Notice).postCommand();
	            	Logger.getStandardLogger().info("Recieved connection from " + connection + ", assigned to " + pc.getPlayer());
            	}
    			new ConsoleMessage( "Player count is " + count + " out of " + MAX_PLAYERS + " players, need players: " + (MAX_PLAYERS-count), Level.Notice).postCommand();
            	Logger.getStandardLogger().info("Player count is " + count + " out of " + MAX_PLAYERS + " players, need players: " + (MAX_PLAYERS-count));
            	playerUpdated( null);
            } catch( SocketTimeoutException ex){
                //try again for incoming connections
            } catch ( IOException e) {
    			new ConsoleMessage( "Problem closing player connections", Level.Error).postCommand();
            	Logger.getErrorLogger().error("Problem with player connections: ", e);
			}
        }
		try {
			serverSocket.close();
		} catch ( IOException e) {
			new ConsoleMessage( "Problem closing player connections", Level.Error).postCommand();
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
			connections.postNotification();
		}
		if( !anyUnReady && ((connectedPlayers.size()==MAX_PLAYERS)||Constants.BYPASS_MIN_PLAYER)){
			HashSet< Player> set = new HashSet<>();
			for( PlayerConnection pc : connectedPlayers){
				set.add( pc.getPlayer());
			}
			new StartGame( Constants.BYPASS_MIN_PLAYER? MAX_PLAYERS:set.size()).postNotification();
			new StartGameCommand( demoMode, set).postCommand();
		}
	}
	
	@Subscribe
	public void endServer( EndServer end){
		close = true;
	}
}
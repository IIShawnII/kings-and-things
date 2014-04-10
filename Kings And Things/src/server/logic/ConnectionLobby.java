package server.logic;

import static common.Constants.MAX_PLAYERS;
import static common.Constants.SERVER_PORT;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;

import server.event.EndServer;
import server.event.GameStarted;
import server.event.PlayerUpdated;
import server.event.internal.StartGameCommand;
import server.event.internal.StartSetupPhaseCommand;
import server.logic.game.CommandHandlerManager;
import server.logic.game.GameState;
import server.logic.game.StateGenerator;
import server.logic.game.StateGenerator.GeneratorType;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.Level;
import common.Constants.UpdateKey;
import common.Logger;
import common.event.ConsoleMessage;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.CommandRejected;
import common.event.network.PlayerState;
import common.event.network.PlayersList;
import common.event.network.StartGame;
import common.game.LoadResources;
import common.game.Player;
import common.game.PlayerInfo;
import common.network.Connection;

public class ConnectionLobby implements Runnable {

	private boolean close = false;
	private ServerSocket serverSocket;
	private final CommandHandlerManager game;
	private final ArrayList< PlayerConnection> connectedPlayers;
	private final boolean demoMode;
	private final boolean generateStateFile;
	private final boolean loadStateFile;
	private final String stateFileName;
	private final boolean generateAll;
	
	public ConnectionLobby( boolean isDemoMode, boolean loadStateFile, boolean generateStateFile, String stateFileName, boolean generateAll) throws IOException{
		if( isDemoMode){
			Logger.getStandardLogger().info("Server started in demo mode.");
			new ConsoleMessage( "Starting in demo mode.", Level.Notice, this).postInternalEvent();
		}
		this.generateAll = generateAll;
		demoMode = isDemoMode;
		this.generateStateFile = generateStateFile;
		this.loadStateFile = loadStateFile;
		this.stateFileName = stateFileName;
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
            serverSocket.setSoTimeout( Constants.SERVER_TIMEOUT*1000);
			Logger.getStandardLogger().info("Listening on port " + SERVER_PORT);
			new ConsoleMessage( "Listening on port " + SERVER_PORT, Level.Notice, this).postInternalEvent();
		} catch ( IOException e) {
			Logger.getErrorLogger().error("Failed to open port " + SERVER_PORT, e);
			new ConsoleMessage( "Failed to open port " + SERVER_PORT + ", Restart Server", Level.Error, this).postInternalEvent();
			return;
		}
		game.initialize();
		int count=0, playerID = Constants.PLAYER_START_ID;
		boolean oldClient = false;
		Socket socket = null;
		Connection connection = null;
		UpdatePackage playerState = null;
		PlayerInfo info = null;
		PlayerConnection pc = null;
		Player player = null;
		while( !close && count<MAX_PLAYERS){
            try {
            	oldClient = false;
            	connection = new Connection();
            	socket = serverSocket.accept();
            	if( connection.connectTo( socket)){
            		playerState = (UpdatePackage)connection.recieve();
            	}else{
            		new ConsoleMessage( "Connection to: " + socket + " failed", Level.Warning,this).postInternalEvent();
            		Logger.getStandardLogger().warn("Connection to: " + socket + " failed");
            		connection.disconnect();
            		socket.close();
            		continue;
            	}
            	info = (PlayerInfo)playerState.getData( UpdateKey.Player);
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
	            	pc.sendNotificationToClient( new PlayerState( info, playerID));
	            	connectedPlayers.add( pc);
	            	count++;
	            	playerID*=Constants.PLAYER_ID_MULTIPLIER;
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
			connections.postNetworkEvent( Constants.ALL_PLAYERS_ID);
		}
	}
	
	@Subscribe
	public void starGame( StartGameCommand command){
		if( connectedPlayers.size() >= Constants.MIN_PLAYERS && connectedPlayers.size() <= Constants.MAX_PLAYERS){
			HashSet< Player> set = new HashSet<>();
			for( PlayerConnection pc : connectedPlayers){
				set.add( pc.getPlayer());
			}
			new StartGame( set.size()).postNetworkEvent( Constants.ALL_PLAYERS_ID);
			if(generateAll)
			{
				try
				{
					new StateGenerator("MinimalDemo", false, GeneratorType.MINIMAL_DEMO).getGeneratedState();
					new StateGenerator("AverageDemo", false, GeneratorType.AVERAGE_DEMO).getGeneratedState();
					GameState state = new StateGenerator("SuperiorDemo", false, GeneratorType.SUPERIOR_DEMO).getGeneratedState();
					new StateGenerator("Construction", false, GeneratorType.CONSTRUCTION).getGeneratedState();
					new StateGenerator("Exploration", false, GeneratorType.EXPLORATION).getGeneratedState();
					new StateGenerator("Movement", false, GeneratorType.MOVEMENT).getGeneratedState();

					new GameStarted(demoMode, state).postInternalEvent();
					state.notifyClientsOfState();
				}
				catch (ClassNotFoundException | IOException e)
				{
					Logger.getErrorLogger().error("Unable to " + (loadStateFile? "load" : "save") +" game state "+ (loadStateFile? "from" : "to") +" file: " + stateFileName + ", due to: ", e);
					new CommandRejected(null, null, null, "Unable to " + (loadStateFile? "load" : "save") +" game state "+ (loadStateFile? "from" : "to") +" file: " + stateFileName + ", due to: " + e,null).postNetworkEvent(Constants.ALL_PLAYERS_ID);
				}
			}
			else if(loadStateFile || generateStateFile)
			{
				try
				{
					GameState state = new StateGenerator(stateFileName, loadStateFile, GeneratorType.SUPERIOR_DEMO).getGeneratedState();
					new GameStarted(demoMode, state).postInternalEvent();
					state.notifyClientsOfState();
				}
				catch (ClassNotFoundException | IOException e)
				{
					Logger.getErrorLogger().error("Unable to " + (loadStateFile? "load" : "save") +" game state "+ (loadStateFile? "from" : "to") +" file: " + stateFileName + ", due to: ", e);
					new CommandRejected(null, null, null, "Unable to " + (loadStateFile? "load" : "save") +" game state "+ (loadStateFile? "from" : "to") +" file: " + stateFileName + ", due to: " + e,null).postNetworkEvent(Constants.ALL_PLAYERS_ID);
				}
			}
			else
			{
				new StartSetupPhaseCommand( demoMode, set).postInternalEvent();
			}
		}
	}
	
	@Subscribe
	public void endServer( EndServer end){
		close = true;
	}
}
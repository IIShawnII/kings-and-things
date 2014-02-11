package client.logic;

import client.event.BoardUpdate;
import client.event.EndClient;
import client.event.ConnectionState;
import client.event.ConnectionAction;
import client.event.UpdatePlayer;
import common.Logger;
import common.network.Connection;
import common.Constants.NetwrokAction;
import common.event.AbstractNetwrokEvent;
import common.event.notifications.Flip;
import common.event.notifications.HexPlacement;
import common.event.notifications.PlayerOrderList;
import common.event.notifications.RackPlacement;
import common.event.notifications.StartGame;
import common.event.notifications.PlayersList;
import common.event.notifications.PlayerState;
import common.game.PlayerInfo;

import com.google.common.eventbus.Subscribe;

import static common.Constants.PLAYER_READY;

public class ConnectionLogic implements Runnable {

	private Connection connection;
	private boolean finished = false;
	private PlayerInfo player = null;
	private PlayerInfo[] players;
	
	public ConnectionLogic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		AbstractNetwrokEvent event = null;
		while( !finished && !connection.isConnected()){
			try {
				Thread.sleep( 10);
			} catch ( InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.getStandardLogger().info( "listenning");
		while( !finished && (event = connection.recieve())!=null){
			Logger.getStandardLogger().info( "Received: " + event);
			if( event instanceof PlayersList){
				players = ((PlayersList)event).getPlayers();
				new UpdatePlayer(players).postCommand();
			} else if( event instanceof StartGame){
				new ConnectionState(((StartGame)event).getPlayerCount()).postCommand();
				new UpdatePlayer(players, player).postCommand();
			} else if( event instanceof PlayerState){
				player = ((PlayerState)event).getPlayer();
			} else if( event instanceof HexPlacement){
				new BoardUpdate(((HexPlacement)event).getArray()).postCommand();
			} else if( event instanceof Flip){
				new BoardUpdate(((Flip)event).flipAll()).postCommand();
			} else if( event instanceof PlayerOrderList){
				new BoardUpdate(((PlayerOrderList)event).getList()).postCommand();
			} else if( event instanceof RackPlacement){
				new BoardUpdate(((RackPlacement)event).getArray()).postCommand();
			}
		}
		finished = true;
		Logger.getStandardLogger().warn( "logic disconnected");
	}
	
	@Subscribe
	public void connectionAction( ConnectionAction action){
		NetwrokAction netaction = NetwrokAction.Disconnect;
		String message = "Unable To Connect, Try Again";
		switch( action.getAction()){
			case Connect:
				if( action.getName()==null || action.getName().length()<=0){
					message += "\nThere Must Be a Name";
				}else{ 
					try{
						if( connection.connectTo( action.getAddress(), action.getPort())){
							netaction = NetwrokAction.Connect;
							if( finished){
								finished = false;
								startTask( this);
							}
							if( player!=null){
								sendToServer( new PlayerState( player));
							}else{
								sendToServer( new PlayerState( action.getName(), PLAYER_READY));
							}
						}
					}catch(IllegalArgumentException ex){
						message += "\n" + ex.getMessage();
					}
				}
				break;
			case Disconnect:
				connection.disconnect();
				message = null;
				break;
			case ReadyState:
				netaction = NetwrokAction.ReadyState;
				player.setReady( !player.isReady());
				message = !player.isReady()? "Ready":"UnReady";
				sendToServer( new PlayerState( player));
				break;
			default:
				return;
		}
		new ConnectionState( 0, message, netaction).postCommand();
	}
	
	private void startTask( Runnable task){
		new Thread( task, "Client Logic").start();
	}
	
	@Subscribe
	public void sendToServer( AbstractNetwrokEvent event){
		Logger.getStandardLogger().info( "Sent: " + event);
		connection.send( event);
	}
	
	@Subscribe
	public void endClient( EndClient end){
		connection.disconnect();
		finished = true;
	}
}

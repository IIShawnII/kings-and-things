package client.logic;

import client.event.EndClient;
import client.event.ConnectionState;
import client.event.ConnectionAction;
import client.event.UpdatePlayerNames;
import common.Logger;
import common.PlayerInfo;
import common.network.Connection;
import common.Constants.NetwrokAction;
import common.event.AbstractNetwrokEvent;
import common.event.notifications.StartGame;
import common.event.notifications.PlayersList;
import common.event.notifications.PlayerState;

import com.google.common.eventbus.Subscribe;

import static common.Constants.PLAYER_READY;

public class Logic implements Runnable {

	private Connection connection;
	private boolean finished = false;
	private PlayerInfo player = null;
	
	public Logic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		AbstractNetwrokEvent notification = null;
		while( !finished && !connection.isConnected()){
			try {
				Thread.sleep( 10);
			} catch ( InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.getStandardLogger().info( "listenning");
		while( !finished && (notification = connection.recieve())!=null){
			Logger.getStandardLogger().info( "Received: " + notification);
			if( notification instanceof PlayersList){
				new UpdatePlayerNames( ((PlayersList)notification).getPlayers()).postCommand();
			} else if( notification instanceof StartGame){
				new ConnectionState( NetwrokAction.StartGame).postCommand();
			} else if( notification instanceof PlayerState){
				player = ((PlayerState)notification).getPlayer();
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
				sendToServer( new PlayerState( player));
				message=null;
				break;
			default:
				return;
		}
		new ConnectionState( message, netaction).postCommand();
	}
	
	private void startTask( Runnable task){
		new Thread( task, "Client Logic").start();
	}
	
	@Subscribe
	public void sendToServer( AbstractNetwrokEvent notification){
		Logger.getStandardLogger().info( "Sent: " + notification);
		connection.send( notification);
	}
	
	@Subscribe
	public void endClient( EndClient end){
		connection.disconnect();
		finished = true;
	}
}

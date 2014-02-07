package client.logic;

import client.event.EndClient;
import client.event.ConnectionState;
import client.event.ConnectionAction;
import client.event.UpdatePlayerNames;
import common.Logger;
import common.network.Connection;
import common.event.EventDispatch;
import common.event.AbstractNetwrokEvent;
import common.event.notifications.PlayersList;
import common.event.notifications.PlayerState;

import com.google.common.eventbus.Subscribe;

public class Logic implements Runnable {

	private Connection connection;
	private boolean finish = false;
	
	public Logic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		EventDispatch.registerForCommandEvents( this);
		AbstractNetwrokEvent notification = null;
		while( !finish && !connection.isConnected()){
			try {
				Thread.sleep( 10);
			} catch ( InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.getStandardLogger().info( "listenning");
		while( !finish && (notification = connection.recieve())!=null){
			Logger.getStandardLogger().info( "Received: " + notification);
			if( notification instanceof PlayersList){
				new UpdatePlayerNames( ((PlayersList)notification).getPlayers()).postCommand();
			}
		}
		Logger.getStandardLogger().warn( "logic disconnected");
	}
	
	@Subscribe
	public void connectionAction( ConnectionAction action){
		boolean isConnected = false;
		String message = "Unable To Connect, Try Again";
		if( action.shouldConnect()){
			try{
				isConnected = connection.connectTo( action.getAddress(), action.getPort());
			}catch(IllegalArgumentException ex){
				message += " \n" + ex.getMessage();
			}
		}else{
			connection.disconnect();
			message = null;
		}
		new ConnectionState( message, isConnected).postCommand();
		if( isConnected){
			sendToServer( new PlayerState( action.getName()));
		}
	}
	
	@Subscribe
	public void sendToServer( AbstractNetwrokEvent notification){
		Logger.getStandardLogger().info( "Sent: " + notification);
		connection.send( notification);
	}
	
	@Subscribe
	public void endClient( EndClient end){
		connection.disconnect();
		finish = true;
	}
}

package client.logic;

import client.event.ConnectionAction;
import client.event.ConnectionState;
import client.event.UpdatePlayerNames;
import common.Logger;
import common.network.Connection;
import common.event.AbstractNetwrokEvent;
import common.event.EventDispatch;
import common.event.notifications.PlayerConnected;

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
		while ( !finish){
			notification = connection.recieve();
			if( notification!=null){
				if( notification instanceof PlayerConnected){
					System.out.println( "list");
					new UpdatePlayerNames( ((PlayerConnected)notification).getPlayers()).postCommand();
				}
			}
		}
		Logger.getStandardLogger().info( "logic disconnected");
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
	}
	
	@Subscribe
	public void sendToServer( AbstractNetwrokEvent notification){
		connection.send( notification);
	}
}

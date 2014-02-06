package client.logic;

import client.event.ConnectionAction;
import client.event.ConnectionState;
import common.network.Connection;
import common.event.EventDispatch;
import common.event.notifications.AbstractNotification;
import common.event.notifications.PlayerReady;

import com.google.common.eventbus.Subscribe;

public class Logic implements Runnable {

	private Connection connection;
	
	public Logic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		EventDispatch.COMMAND.register( this);
	}
	
	@Subscribe
	public void connectionAction( ConnectionAction action){
		System.out.println( "test");
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
		new ConnectionState( message, isConnected).post();
	}
	
	@Subscribe
	public void sendToServer( PlayerReady notification){
		connection.send( notification);
		connection.recieve();
	}
}

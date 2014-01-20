package server.logic;

import common.Constants.Level;
import common.event.EventMonitor;
import common.network.Connection;

import static common.Constants.CONSOLE;

public class PlayerThread extends Thread{
	
	private final int PLAYER_ID;
	private Connection connection;
	
	public PlayerThread( final int PLAYER_ID, Connection connection){
		this.PLAYER_ID = PLAYER_ID;
		this.connection = connection;
	}
	
	@Override
	public void run(){
		String str;
		while ((str = connection.recieve())!=null){
			connection.send( new StringBuilder( str).reverse().toString());
		}
		EventMonitor.fireEvent( PLAYER_ID+CONSOLE, null, Level.END);
	}
}

package server.logic;

import com.google.common.eventbus.Subscribe;

import common.Player;
import common.event.AbstractEvent;
import common.event.EventDispatch;
import common.event.notifications.PlayerConnected;
import common.event.notifications.PlayerReady;
import common.event.notifications.PlayerUnReady;
import common.network.Connection;

public class PlayerConnection extends Thread{
	
	private final int PLAYER_ID;
	private Connection connection;
	private boolean readyToStart;
	private String playerName;
	
	public PlayerConnection( final int PLAYER_ID, Connection connection){
		this.PLAYER_ID = PLAYER_ID;
		this.connection = connection;
		readyToStart = false;
		playerName = "Player " + PLAYER_ID;
	}
	
	public boolean isReadyToStart(){
		return readyToStart;
	}
	
	public void setReadyToStart(boolean newVal){
		readyToStart = newVal;
	}
	
	public int getPlayerID(){
		return PLAYER_ID;
	}
	
	public String getPlayerName(){
		return playerName;
	}
	
	public void setPlayerName(String newName){
		playerName = newName;
	}
	
	public Player toPlayerObj(){
		return new Player(playerName, PLAYER_ID);
	}
	
	@Override
	public void run(){
		EventDispatch.registerForCommandEvents(this);
		AbstractEvent notification = null;
		while ((notification = connection.recieve())!=null){
			if( notification instanceof PlayerReady || notification instanceof PlayerUnReady){
				setReadyToStart( !(notification instanceof PlayerUnReady));
				notification.postNotification( getPlayerID());
			}
		}
	}
	
	@Subscribe
	public void sendNotificationToClient( PlayerConnected event){
		connection.send( event);
	}
}

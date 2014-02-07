package server.logic;

import server.event.commands.PlayerUpdated;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.Player;
import common.event.AbstractEvent;
import common.event.AbstractNetwrokEvent;
import common.event.notifications.PlayerState;
import common.network.Connection;

public class PlayerConnection extends Thread{
	
	private Player player;
	private Connection connection;
	
	public PlayerConnection(String name, final int PLAYER_ID, boolean ready, Connection connection){
		player = new Player(name, PLAYER_ID, ready);
		setName( name);
		this.connection = connection;
	}
	
	public boolean isReadyToStart(){
		return player.isPlaying();
	}
	
	public Player getPlayer(){
		return player;
	}
	
	@Override
	public void run(){
		AbstractEvent notification = null;
		while ((notification = connection.recieve())!=null){
			Logger.getStandardLogger().info( "(" + player + ")Received: " +notification);
			if( notification instanceof PlayerState){
				player.setIsPlaying( ((PlayerState)notification).isReady());
			}
			new PlayerUpdated( player).postCommand();
		}
		Logger.getStandardLogger().warn( player + " lost connection");
	}
	
	@Subscribe
	public void sendNotificationToClient( AbstractNetwrokEvent event){
		connection.send( event);
		Logger.getStandardLogger().info( "(" + player + ")Sent: " +event);
	}
}

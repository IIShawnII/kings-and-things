package server.logic;

import server.event.commands.PlayerUpdated;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.Player;
import common.event.AbstractEvent;
import common.event.AbstractNetwrokEvent;
import common.event.EventDispatch;
import common.event.notifications.PlayerReady;
import common.event.notifications.PlayerUnReady;
import common.network.Connection;

public class PlayerConnection extends Thread{
	
	private Player player;
	private Connection connection;
	
	public PlayerConnection( final int PLAYER_ID, Connection connection){
		player = new Player( PLAYER_ID);
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
		EventDispatch.registerForCommandEvents(this);
		AbstractEvent notification = null;
		while ((notification = connection.recieve())!=null){
			Logger.getStandardLogger().info( notification);
			if( notification instanceof PlayerReady){
				player.setName( ((PlayerReady)notification).getName());
				player.setIsPlaying( false);
			}
			if( notification instanceof PlayerUnReady){
				player.setIsPlaying( false);
			}
			new PlayerUpdated( player).postCommand();
		}
	}
	
	@Subscribe
	public void sendNotificationToClient( AbstractNetwrokEvent event){
		connection.send( event);
	}
}

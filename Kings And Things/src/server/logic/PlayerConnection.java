package server.logic;

import server.event.commands.PlayerUpdated;
import server.logic.game.Player;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.PlayerInfo;
import common.event.AbstractEvent;
import common.event.AbstractNetwrokEvent;
import common.event.notifications.PlayerState;
import common.network.Connection;

public class PlayerConnection implements Runnable{
	
	private Player player;
	private Connection connection;
	
	public PlayerConnection( Player player, Connection connection){
		this.player = player;
		this.connection = connection;
		player.setConnected( true);
	}
	
	public boolean isReadyToStart(){
		return player.isPlaying();
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public String getName(){
		return player.getName();
	}
	
	protected void setConnection( Connection connection){
		this.connection.disconnect();
		this.connection = connection;
		player.setConnected( true);
	}
	
	@Override
	public String toString(){
		return connection + ", " + player;
	}

	public boolean isConnected() {
		return player.isConnected();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object other){
		if ( this == other) {
			return true;
		}
		if( other==null){
			return false;
		}
		if( other instanceof PlayerInfo || other instanceof Player){
			return player.equals( other);
		}
		if( other instanceof PlayerConnection){
			return player.equals( ((PlayerConnection)other).player);
		}
		return false;
	}

	@Override
	public void run(){
		AbstractEvent notification = null;
		while ((notification = connection.recieve())!=null){
			Logger.getStandardLogger().info( "(" + player + ")Received: " +notification);
			if( notification instanceof PlayerState){
				player.setIsPlaying( ((PlayerState)notification).getPlayer().isReady());
			}
			new PlayerUpdated( player).postCommand();
		}
		player.setIsPlaying(false);
		player.setConnected( false);
		new PlayerUpdated( player).postCommand();
		Logger.getStandardLogger().warn( player + " lost connection");
	}
	
	@Subscribe
	public void sendNotificationToClient( AbstractNetwrokEvent event){
		connection.send( event);
		Logger.getStandardLogger().info( "(" + player + ")Sent: " +event);
	}

	public PlayerInfo getPlayerInfo() {
		return player.getPlayerInfo();
	}
}

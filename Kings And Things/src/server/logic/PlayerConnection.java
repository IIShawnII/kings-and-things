package server.logic;

import server.event.commands.SendNotificationAcrossNetworkEvent;
import server.logic.game.Player;

import com.google.common.eventbus.Subscribe;

import common.event.EventDispatch;
import common.event.notifications.AbstractNotification;
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
	
	public void initialize()
	{
		EventDispatch.COMMAND.register(this);
	}
	
	public boolean isReadyToStart()
	{
		return readyToStart;
	}
	
	public void setReadyToStart(boolean newVal)
	{
		readyToStart = newVal;
	}
	
	public int getPlayerId()
	{
		return PLAYER_ID;
	}
	
	public String getPlayerName()
	{
		return playerName;
	}
	
	public void setPlayerName(String newName)
	{
		playerName = newName;
	}
	
	public Player toPlayerObj()
	{
		return new Player(playerName, PLAYER_ID);
	}
	
	@Override
	public void run(){
		AbstractNotification notification = null;
		while ((notification = connection.recieve())!=null){
			notification.post();//.dispatch(getPlayerId());
		}
	}
	
	@Subscribe
	public void sendNotificationToClient( SendNotificationAcrossNetworkEvent command)
	{
		connection.send( command.getNotification());
	}
}

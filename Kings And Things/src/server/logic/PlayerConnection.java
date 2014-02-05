package server.logic;

import server.event.SendCommandAcrossNetworkEvent;

import com.google.common.eventbus.Subscribe;

import common.Constants.Level;
import common.event.CommandEventBus;
import common.event.EventMonitor;
import common.game.CommandMarshaller;
import common.game.Player;
import common.network.Connection;
import static common.Constants.CONSOLE;

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
		CommandEventBus.BUS.register(this);
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
		String str;
		while ((str = connection.recieve())!=null){
			CommandMarshaller.unmarshalCommand(str).dispatch(getPlayerId());
		}
		EventMonitor.fireEvent( PLAYER_ID+CONSOLE, null, Level.END);
	}
	
	@Subscribe
	public void sendCommandToClient(SendCommandAcrossNetworkEvent command)
	{
		connection.send(CommandMarshaller.marshalCommand(command.getCommand()));
	}
}

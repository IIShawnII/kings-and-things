package server.logic;

import com.google.common.eventbus.Subscribe;

import common.Constants.Level;
import common.event.CommandEventBus;
import common.event.EventMonitor;
import common.event.SendCommandAcrossNetworkEvent;
import common.game.CommandMarshaller;
import common.network.Connection;
import static common.Constants.CONSOLE;

public class PlayerConnection extends Thread{
	
	private final int PLAYER_ID;
	private Connection connection;
	
	public PlayerConnection( final int PLAYER_ID, Connection connection){
		this.PLAYER_ID = PLAYER_ID;
		this.connection = connection;
	}
	
	public void initialize()
	{
		CommandEventBus.BUS.register(this);
	}
	
	@Override
	public void run(){
		String str;
		while ((str = connection.recieve())!=null){
			CommandMarshaller.unmarshalCommand(str).dispatch();
		}
		EventMonitor.fireEvent( PLAYER_ID+CONSOLE, null, Level.END);
	}
	
	@Subscribe
	public void sendCommandToClient(SendCommandAcrossNetworkEvent command)
	{
		connection.send(CommandMarshaller.marshalCommand(command.getCommand()));
	}
}

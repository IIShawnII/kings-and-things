package server.logic;

import java.io.IOException;

import server.event.DiceRolled;
import server.event.PlayerUpdated;
import server.event.internal.GiveHexToPlayerCommand;
import server.event.internal.RollDiceCommand;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.network.Connection;
import common.Constants.RollReason;
import common.Constants.UpdateKey;
import common.game.ITileProperties;
import common.game.Player;
import common.game.HexState;
import common.game.PlayerInfo;
import common.event.UpdatePackage;
import common.event.AbstractNetwrokEvent;

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
		UpdatePackage event = null;
		try {
			while ((event = (UpdatePackage)connection.recieve())!=null){
				Logger.getStandardLogger().info( "(" + player + ")Received: " +event);
				switch( event.peekFirstInstruction()){
					case State: 
						player.setIsPlaying( ((PlayerInfo)event.getData( UpdateKey.Player)).isReady());
						new PlayerUpdated( player).postInternalEvent( player.getID());
						break;
					case HexOwnership: 
						new GiveHexToPlayerCommand( ((HexState)event.getData( UpdateKey.HexState)).getHex()).postInternalEvent( player.getID());
						break;
					case NeedRoll: 
						new RollDiceCommand( (RollReason)event.getData( UpdateKey.RollReason), (ITileProperties)event.getData( UpdateKey.Tile)).postInternalEvent( player.getID());
						break;
					default:
						throw new IllegalStateException("Error - no support for: " + event.peekFirstInstruction());
				}
			}
		} catch ( ClassNotFoundException | IOException e) {
			Logger.getStandardLogger().warn( e);
		}
		player.setIsPlaying(false);
		player.setConnected( false);
		new PlayerUpdated( player).postInternalEvent( player.getID());
		Logger.getStandardLogger().warn( player + " lost connection");
	}
	
	@Subscribe
	public void sendNotificationToClient( AbstractNetwrokEvent event){
		if( !event.isValidID( player.getPlayerInfo())){
			return;
		}
		try {
			connection.send( event);
		} catch ( IOException e) {
			Logger.getErrorLogger().warn( "Error - ", e);
		}
		Logger.getStandardLogger().info( "(" + player + ")Sent: " +event);
	}

	public PlayerInfo getPlayerInfo() {
		return player.getPlayerInfo();
	}
}

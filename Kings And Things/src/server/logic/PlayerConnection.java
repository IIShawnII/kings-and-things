package server.logic;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import server.event.PlayerUpdated;
import server.event.internal.ApplyHitsCommand;
import server.event.internal.BribeDefenderCommand;
import server.event.internal.CallBluffCommand;
import server.event.internal.ConstructBuildingCommand;
import server.event.internal.ExchangeSeaHexCommand;
import server.event.internal.ExchangeThingsCommand;
import server.event.internal.MoveThingsCommand;
import server.event.internal.PlaceThingOnBoardCommand;
import server.event.internal.RemoveThingsFromHexCommand;
import server.event.internal.ResolveCombatCommand;
import server.event.internal.RetreatCommand;
import server.event.internal.RollDiceCommand;
import server.event.internal.DoneRollingCommand;
import server.event.internal.EndPlayerTurnCommand;
import server.event.internal.GiveHexToPlayerCommand;
import server.event.internal.TargetPlayerCommand;
import server.event.internal.ViewHexContentsCommand;

import com.google.common.eventbus.Subscribe;

import common.Constants.BuildableBuilding;
import common.Constants.HexContentsTarget;
import common.Logger;
import common.network.Connection;
import common.Constants.UpdateKey;
import common.game.Roll;
import common.game.Player;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.ITileProperties;
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

	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		final int ID = player.getID();
		UpdatePackage event = null;
		try {
			while ((event = (UpdatePackage)connection.recieve())!=null){
				Logger.getStandardLogger().info( "Received "+(player!=null?player.getID():"-1") + ": " + event);
				switch( event.peekFirstInstruction()){
					case State: 
						player.setIsPlaying( ((PlayerInfo)event.getData( UpdateKey.Player)).isReady());
						new PlayerUpdated( player).postInternalEvent( ID);
						break;
					case HexOwnership: 
						new GiveHexToPlayerCommand( ((HexState)event.getData( UpdateKey.HexState)).getHex()).postInternalEvent( ID);
						break;
					case NeedRoll: 
						new RollDiceCommand( (Roll)event.getData( UpdateKey.Roll)).postInternalEvent( ID);
						break;
					case DoneRolling:
						new DoneRollingCommand().postInternalEvent( ID);
						break;
					case Skip:
						new EndPlayerTurnCommand().postInternalEvent( ID);
						break;
					case InitiateCombat:
						new ResolveCombatCommand((ITileProperties) event.getData(UpdateKey.Hex)).postInternalEvent(ID);
						break;
					case TargetPlayer:
						new TargetPlayerCommand((int) event.getData(UpdateKey.Player)).postInternalEvent(ID);
						break;
					case SeaHexChanged:
						new ExchangeSeaHexCommand( (HexState) event.getData(UpdateKey.HexState)).postInternalEvent(ID);
						break;
					case ThingChanged:
						new ExchangeThingsCommand((Collection<ITileProperties>) event.getData(UpdateKey.ThingArray)).postInternalEvent(ID);
						break;
					case ApplyHit:
						new ApplyHitsCommand(1, (ITileProperties) event.getData(UpdateKey.ThingArray)).postInternalEvent(ID);
						break;
					case BribeCreature:
						new BribeDefenderCommand(((ITileProperties[])event.getData(UpdateKey.ThingArray))[0]).postInternalEvent(ID);
						break;
					case Retreat:
						new RetreatCommand((ITileProperties) event.getData(UpdateKey.Hex)).postInternalEvent(ID);
						break;
					case RemoveThingsFromHex:
						HashSet<ITileProperties> thingsToRemove = new HashSet<>();
						for(ITileProperties thing : (ITileProperties[]) event.getData(UpdateKey.ThingArray))
						{
							thingsToRemove.add(thing);
						}
						new RemoveThingsFromHexCommand((ITileProperties) event.getData(UpdateKey.Hex), thingsToRemove).postInternalEvent(ID);
						break;
					case MoveThings:
						Collection<ITileProperties> hexes = (Collection<ITileProperties>) event.getData(UpdateKey.Hex);
						Collection<ITileProperties> things = (Collection<ITileProperties>) event.getData(UpdateKey.ThingArray);
						new MoveThingsCommand(things, hexes).postInternalEvent(ID);
						break;
					case ConstructBuilding:
						BuildableBuilding toBuild = (BuildableBuilding) event.getData(UpdateKey.Tile);
						ITileProperties hex = (ITileProperties) event.getData(UpdateKey.Hex);
						new ConstructBuildingCommand(toBuild,hex).postInternalEvent(ID);
						break;
					case CallBluff:
						ITileProperties thing = (ITileProperties) event.getData(UpdateKey.ThingArray);
						new CallBluffCommand(thing).postInternalEvent(ID);
						break;
					case ViewContents:
						new ViewHexContentsCommand((ITileProperties) event.getData(UpdateKey.Hex), (HexContentsTarget) event.getData(UpdateKey.Category)).postInternalEvent(ID);
						break;
					case PlaceBoard:
						new PlaceThingOnBoardCommand((ITileProperties) event.getData(UpdateKey.Tile), (ITileProperties) event.getData(UpdateKey.Hex)).postInternalEvent(ID);
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
		new PlayerUpdated( player).postInternalEvent( ID);
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
		Logger.getStandardLogger().info( "Sent" + (player!=null?player.getID():"-1") + ": " + event);
	}

	public PlayerInfo getPlayerInfo() {
		return player.getPlayerInfo();
	}
}

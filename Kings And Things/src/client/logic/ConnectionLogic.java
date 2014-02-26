package client.logic;

import com.google.common.eventbus.Subscribe;

import client.event.BoardUpdate;
import common.Logger;
import common.game.PlayerInfo;
import common.network.Connection;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.AbstractNetwrokEvent;
import common.event.UpdatePackage;
import common.event.notifications.Flip;
import common.event.notifications.StartGame;
import common.event.notifications.PlayersList;
import common.event.notifications.PlayerState;
import common.event.notifications.CurrentPhase;
import common.event.notifications.HexPlacement;
import common.event.notifications.RackPlacement;
import common.event.notifications.PlayerOrderList;
import common.event.notifications.HexStatesChanged;
import common.event.notifications.HexOwnershipChanged;
import static common.Constants.LOGIC;
import static common.Constants.LOBBY;
import static common.Constants.PLAYER_READY;

public class ConnectionLogic implements Runnable {

	private Connection connection;
	private boolean finished = false;
	private PlayerInfo player = null;
	private PlayerInfo[] players;
	
	public ConnectionLogic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		CurrentPhase phase = null;
		AbstractNetwrokEvent event = null;
		while( !finished && !connection.isConnected()){
			try {
				Thread.sleep( 10);
			} catch ( InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.getStandardLogger().info( "listenning");
		UpdatePackage update = new UpdatePackage("Logic.Run");
		while( !finished && (event = connection.recieve())!=null){
			update.clear();
			Logger.getStandardLogger().info( "Received: " + event);
			if( event instanceof PlayersList){
				update.addInstruction( UpdateInstruction.UpdatePlayers);
				update.putData( UpdateKey.Players, ((PlayersList)event).getPlayers());
			} 
			else if( event instanceof StartGame){
				update.addInstruction( UpdateInstruction.Start);
				update.putData( UpdateKey.PlayerCount, ((StartGame)event).getPlayerCount());
				new BoardUpdate(players, player).postCommand();
			} 
			else if( event instanceof PlayerState){
				player = ((PlayerState)event).getPlayer();
			} 
			else if( event instanceof HexPlacement){
				new BoardUpdate(((HexPlacement)event).getArray()).postCommand();
			} 
			else if( event instanceof Flip){
				new BoardUpdate(((Flip)event).flipAll()).postCommand();
			} 
			else if( event instanceof PlayerOrderList){
				new BoardUpdate(((PlayerOrderList)event).getList()).postCommand();
			} 
			else if( event instanceof RackPlacement){
				new BoardUpdate(((RackPlacement)event).getArray()).postCommand();
			}
			else if( event instanceof CurrentPhase){
				phase = (CurrentPhase) event;
				if( phase.isSetupPhase()){
					new BoardUpdate(phase.getPlayers(),phase.getSetup()).postCommand();
				}else if( phase.isRegularPhase()){
					
				}else if( phase.isCombatPhase()){
					
				}
			}
			else if(event instanceof HexOwnershipChanged){
				//TODO handle
			}
			else if(event instanceof HexStatesChanged){
				//TODO handle
			}
			if( update.isModified()){
				update.postCommand( LOBBY);
			}
		}
		finished = true;
		Logger.getStandardLogger().warn( "logic disconnected");
	}
	
	@Subscribe
	public void receiveUpdate( UpdatePackage action){
		if( action.isPublic() || (action.getID()&LOGIC)!=LOGIC){
			return;
		}
		UpdateInstruction netaction = UpdateInstruction.Disconnect;
		String message = "Unable To Connect, Try Again";
		switch( action.getFirstInstruction()){
			case Connect:
				String name = (String)action.getData( UpdateKey.Name);
				String ip = (String)action.getData( UpdateKey.IP);
				int port = (Integer)action.getData( UpdateKey.Port);
				if( name==null || name.length()<=0){
					message += "\nThere Must Be a Name";
				}else{ 
					try{
						if( connection.connectTo( ip, port)){
							netaction = UpdateInstruction.Connect;
							if( finished){
								finished = false;
								startTask( this);
							}
							if( player!=null){
								sendToServer( new PlayerState( player));
							}else{
								sendToServer( new PlayerState( name, PLAYER_READY));
							}
						}
					}catch(IllegalArgumentException ex){
						message += "\n" + ex.getMessage();
					}
				}
				break;
			case Disconnect:
				connection.disconnect();
				message = "Disconnect";
				break;
			case ReadyState:
				netaction = UpdateInstruction.ReadyState;
				player.setReady( !player.isReady());
				message = !player.isReady()? "Ready":"UnReady";
				sendToServer( new PlayerState( player));
				break;
			case End:
				connection.disconnect();
				finished = true;
				return;
			case Send:
				//TODO add support for sending UpdatePackage
				//sendToServer( action);
				return;
			default:
				return;
		}
		UpdatePackage update = new UpdatePackage("Logic.Receive");
		update.addInstruction( netaction);
		update.putData( UpdateKey.Message, message);
		update.putData( UpdateKey.PlayerCount, 0);
		update.postCommand( LOBBY);
	}
	
	private void startTask( Runnable task){
		new Thread( task, "Client Logic").start();
	}
	
	@Subscribe
	public void sendToServer( AbstractNetwrokEvent event){
		Logger.getStandardLogger().info( "Sent: " + event);
		connection.send( event);
	}
}

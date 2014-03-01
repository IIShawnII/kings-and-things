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
	
	public ConnectionLogic( ) {
		this.connection = new Connection();
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
		Logger.getStandardLogger().info( "listening");
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
					if( name.matches("(-demo)([\\s](\\w+)){2,4}")){
						String[] names = name.split( " ");
						ConnectionLogic logic;
						for( int i=1; i<names.length-1; i++){
							try{
								logic = new ConnectionLogic();
								netaction = logic.connect( ip, port, names[i]);
								startLogic( logic);
							}catch(IllegalArgumentException ex){
								message += "\n" + ex.getMessage();
							}
						}
						name = names[names.length-1];
					}else if( name.startsWith("-demo")){
						message += "\n\"-demo\" must follow with 2-4 unique names";
					}
					try{
						netaction = connect( ip, port, name);
					}catch(IllegalArgumentException ex){
						message += "\n" + ex.getMessage();
					}
				}
				break;
			case Disconnect:
				if( connection!=null){
					connection.disconnect();
				}
				message = "Disconnect";
				break;
			case ReadyState:
				netaction = UpdateInstruction.ReadyState;
				player.setReady( !player.isReady());
				message = !player.isReady()? "Ready":"UnReady";
				sendToServer( new PlayerState( player));
				break;
			case End:
				if( connection!=null){
					connection.disconnect();
				}
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
	
	private UpdateInstruction connect(String ip, int port, String name) throws IllegalArgumentException{
		if( connection.connectTo( ip, port)){
			if( finished){
				finished = false;
				startLogic( this);
			}
			if( player!=null){
				sendToServer( new PlayerState( player));
			}else{
				sendToServer( new PlayerState( name, PLAYER_READY));
			}
			return UpdateInstruction.Connect;
		}
		return UpdateInstruction.Disconnect;
	}
	
	private void startLogic( ConnectionLogic logic){
		new Thread( logic, "Client Logic").start();
	}
	
	@Subscribe
	public void sendToServer( AbstractNetwrokEvent event){
		Logger.getStandardLogger().info( "Sent: " + event);
		connection.send( event);
	}
}

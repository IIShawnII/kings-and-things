package client.logic;

import java.io.IOException;

import client.event.BoardUpdate;
import common.Logger;
import common.game.PlayerInfo;
import common.network.Connection;
import common.Constants.UpdateKey;
import common.Constants.UpdateInstruction;
import common.event.AbstractUpdateReceiver;
import common.event.UpdatePackage;
import common.event.AbstractNetwrokEvent;
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
import static common.Constants.PLAYER_READY;

public class ConnectionLogic implements Runnable {

	private Connection connection;
	private boolean finished = false;
	private PlayerInfo player = null;
	
	public ConnectionLogic( ) {
		this.connection = new Connection();
		new UpdateReceiver();
		new UpdateTransmitter();
	}

	@Override
	public void run() {
		CurrentPhase phase = null;
		AbstractNetwrokEvent event = null;
		Logger.getStandardLogger().info( "Starting");
		while( !finished && !connection.isConnected()){
			try {
				Thread.sleep( 10);
			} catch ( InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.getStandardLogger().info( "listening");
		UpdatePackage update = new UpdatePackage("Logic.Run", this);
		try {
			while( !finished && (event = connection.recieve())!=null){
				update.clear();
				Logger.getStandardLogger().info( "Logic.Process.Receive "+(player!=null?player.getID():"") + event);
				if( event instanceof PlayersList){
					update.addInstruction( UpdateInstruction.UpdatePlayers);
					update.putData( UpdateKey.Players, ((PlayersList)event).getPlayers());
				} 
				else if( event instanceof StartGame){
					update.addInstruction( UpdateInstruction.Start);
					update.putData( UpdateKey.PlayerCount, ((StartGame)event).getPlayerCount());
				} 
				else if( event instanceof PlayerState){
					//first data from server, with PlayerInfo object
					player = ((PlayerState)event).getPlayer();
					update.setSource( "Logic.Run "+player.getID());
				} 
				else if( event instanceof HexPlacement){
					new BoardUpdate(((HexPlacement)event).getArray(), this).postInternalEvent();
				} 
				else if( event instanceof Flip){
					new BoardUpdate(((Flip)event).flipAll(), this).postInternalEvent();
				} 
				else if( event instanceof PlayerOrderList){
					new BoardUpdate(((PlayerOrderList)event).getList(), this).postInternalEvent();
				} 
				else if( event instanceof RackPlacement){
					new BoardUpdate(((RackPlacement)event).getArray(), this).postInternalEvent();
				}
				else if( event instanceof CurrentPhase){
					phase = (CurrentPhase) event;
					if( phase.isSetupPhase()){
						new BoardUpdate(phase.getPlayers(),phase.getSetup()).postInternalEvent();
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
					update.postInternalEvent();
				}
			}
		} catch ( ClassNotFoundException | IOException e) {
			Logger.getStandardLogger().warn( e);
		}
		finished = true;
		Logger.getStandardLogger().warn( "logic disconnected");
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, LOGIC, ConnectionLogic.this);
		}

		@Override
		public void handlePublic( UpdatePackage update) {
			if( update.peekFirstInstruction()==UpdateInstruction.End){
				if( connection!=null){
					connection.disconnect();
				}
				finished = true;
			}
		}

		@Override
		public void handlePrivate( UpdatePackage update) {
			UpdateInstruction[] instructions = update.getInstructions();
			for( UpdateInstruction instruction : instructions){
				process( instruction, update);
			}
		}
		
		@Override
		public boolean verifyPrivate( UpdatePackage update){
			return update.isValidID(ID) || update.isValidID(player);
		}
	}
	
	private void process( UpdateInstruction instruction, UpdatePackage data) {
		UpdateInstruction netaction = UpdateInstruction.Disconnect;
		String message = "Unable To Connect, Try Again";
		switch( instruction){
			case Connect:
				String name = (String)data.getData( UpdateKey.Name);
				String ip = (String)data.getData( UpdateKey.IP);
				int port = (Integer)data.getData( UpdateKey.Port);
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
							}catch(IllegalArgumentException | IOException ex){
								message += "\n" + ex.getMessage();
							}
						}
						name = names[names.length-1];
					}else if( name.startsWith("-demo")){
						message += "\n\"-demo\" must follow with 2-4 unique names";
					}
					try{
						netaction = connect( ip, port, name);
					}catch(IllegalArgumentException | IOException ex){
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
			default:
				throw new IllegalArgumentException( "No handle for instruction: " + instruction);
		}
		UpdatePackage update = new UpdatePackage("Logic.Process.Receive "+(player!=null?player.getID():""), this);
		update.addInstruction( netaction);
		update.putData( UpdateKey.Message, message);
		update.putData( UpdateKey.PlayerCount, 0);
		update.postInternalEvent();
	}

	private UpdateInstruction connect(String ip, int port, String name) throws IllegalArgumentException, IOException{
		Logger.getStandardLogger().info( "Connecting");
		if( connection.connectTo( ip, port)){
			Logger.getStandardLogger().info( "Connected");
			if( finished){
				Logger.getStandardLogger().info( "Starting Thread");
				finished = false;
				startLogic( this);
			}
			if( player!=null){
				Logger.getStandardLogger().info( "Send Old Player");
				sendToServer( new PlayerState( player));
			}else{
				Logger.getStandardLogger().info( "Send New Player");
				sendToServer( new PlayerState( name, PLAYER_READY));
			}
			return UpdateInstruction.Connect;
		}
		Logger.getStandardLogger().info( "Failed Connecting");
		return UpdateInstruction.Disconnect;
	}
	
	private void startLogic( ConnectionLogic logic){
		new Thread( logic, "Client Logic").start();
	}
	
	private class UpdateTransmitter extends AbstractUpdateReceiver<AbstractNetwrokEvent>{

		protected UpdateTransmitter() {
			super( NETWORK, LOGIC, ConnectionLogic.this);
		}

		@Override
		public void handlePrivate( AbstractNetwrokEvent update) {
			sendToServer( update);
		}

		@Override
		public boolean verifyPrivate( AbstractNetwrokEvent update) {
			return update.isValidID(ID) || update.isValidID(player);
		}
	}

	public void sendToServer( AbstractNetwrokEvent event){
		Logger.getStandardLogger().info( "Sent: " + event);
		try {
			connection.send( event);
		} catch ( IOException e) {
			Logger.getStandardLogger().warn( e);
		}
	}
}

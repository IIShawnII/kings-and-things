package client.logic;

import client.event.ConnectToServer;

import com.google.common.eventbus.Subscribe;

import common.event.CommandEventBus;
import common.network.Connection;

public class Logic implements Runnable {

	private Connection connection;
	private boolean stayAlive = true;
	
	public Logic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		CommandEventBus.BUS.register( this);
		while(stayAlive){
			try {
				System.out.println( "Going to sleep");
				Thread.sleep( 1000);
			} catch ( InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Subscribe
	private void connectToServer( ConnectToServer connect){
		System.out.println( "Connecting");
		connection.connectTo( connect.getAddress(), connect.getPort());
	}
	
	/*public void actionPerformed( ActionEvent e) {
		Object source = e.getSource();
		if( source==jbConnect || source==jbDisconnect){
			if ( connection.isConnected()){
				connection.disconnect();
				jbDisconnect.setEnabled( false);
			}else if( connection.connectTo( jtfIP.getText(), Integer.parseInt( jtfPort.getText()))){
				jbDisconnect.setEnabled( true);
			}
			boolean state = !jbDisconnect.isEnabled();
			jbConnect.setEnabled( state);
			jtfIP.setEnabled( state);
			jtfPort.setEnabled( state);
			jtfName.setEnabled( state);
		}else if( e.getActionCommand().equals( "Start")){
			if( connection.isConnected()){
				connection.send( "-start");
				result = true;
				dispose();
			}
		}else if( e.getActionCommand().equals( "Cancel")){
			dispose();
			result = false;
		}
	}*/
}

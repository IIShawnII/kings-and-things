package client.logic;

import client.event.ConnectToServer;
import common.network.Connection;
import common.event.EventDispatch;

import com.google.common.eventbus.Subscribe;

public class Logic implements Runnable {

	private Connection connection;
	
	public Logic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		EventDispatch.COMMAND.register( this);
	}
	
	@Subscribe
	public void connectToServer( ConnectToServer connect){
		System.out.println( "test");
		try{
			connection.connectTo( connect.getAddress(), connect.getPort());
		}catch(IllegalArgumentException ex){
		}
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

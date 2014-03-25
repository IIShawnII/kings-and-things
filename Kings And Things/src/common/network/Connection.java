package common.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetSocketAddress;

import common.event.AbstractEvent;
import common.event.AbstractNetwrokEvent;
import common.event.UpdatePackage;

/**
 * primary class for sending and receiving text from client or server
 */
public class Connection implements Closeable{
	
	private Socket socket = null;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private InetSocketAddress address;
	private volatile boolean isConnected = false;

	/**
	 * state of the current connection
	 * @return true if connectTo() has been successfully called
	 * 			and false if disconnect has been called
	 */
	public boolean isConnected(){
		return isConnected;
	}
	
	@Override
	public String toString(){
		return socket.toString();
	}
	
	/**
	 * connect to a specific IP and port; and create in out streams
	 * @param ip - destination IP address
	 * @param port - destination port
	 * @return true if all streams are created and connection established, otherwise false
	 * @throws IOException - any caught exception will be thrown again
	 */
	public boolean connectTo( String ip, int port) throws IOException{
		if( ip==null || port<=0){
			throw new IllegalArgumentException( "IP address cannot be null, port must be a positive none zero integer");
		}
		try {
			address = new InetSocketAddress( ip, port);
			socket = new Socket();
			socket.connect( address);
			connectTo( socket);
		} catch( IOException e){
			disconnect();
			throw e;
		}
		return isConnected;
	}
	
	/**
	 * connect to a specific socket and create in out streams
	 * @param socket destination socket containing valid address
	 * @return true if all streams are created and connection established, otherwise false
	 * @throws IOException - any caught exception will be thrown again
	 */
	public boolean connectTo( Socket socket) throws IOException{
		if( socket==null){
			throw new IllegalArgumentException( "Socket cannot be null");
		}
		this.socket = socket;
		try {
			output = new ObjectOutputStream( socket.getOutputStream());
            input = new ObjectInputStream( socket.getInputStream());
			isConnected = true;
		} catch( IOException e){
			disconnect();
			throw e;
		}
		return isConnected;
	}
	
	/**
	 * close all streams and socket
	 */
	public void disconnect() {
		if( socket!=null){
			if( input!=null){
				try {
					input.close();
				} catch ( IOException e) {
					e.printStackTrace();
				}
			}
			if( output!=null){
				try {
					output.close();
				} catch ( IOException e) {
					e.printStackTrace();
				}
			}
			if( socket!=null){
				try {
					socket.close();
				} catch ( IOException e) {
					e.printStackTrace();
				}
			}
			input = null;
			output = null;
			socket = null;
		}
		isConnected = false;
	}
	
	/**
	 * send an object to destination
	 * @param event - information to be sent as AbstractNetwrokEvent
	 * @return true if information has been sent, else false
	 */
	public void send( AbstractNetwrokEvent event) throws IOException{
		if( isConnected){
			output.reset();
			output.writeObject( event);
		}else{
			throw new IOException( "No connection is avalibale");
		}
	}
	
	/**
	 * send an object to destination
	 * @param event - information to be sent as UpdatePackage
	 * @return true if information has been sent, else false
	 */
	public void send( UpdatePackage event) throws IOException{
		if( isConnected){
			output.reset();
			output.writeObject( event);
		}else{
			throw new IOException( "No connection is avalibale");
		}
	}
	
	/**
	 * Receive response from destination in form of an Object
	 * @return a UpdatePackage if message is received, otherwise null
	 */
	public AbstractEvent recieve() throws IOException, ClassNotFoundException{
		if( isConnected){
			AbstractEvent event = null;
			event = (AbstractEvent) input.readObject();
			if( event==null){
				isConnected = false;
			}
			return event;
		}else{
			throw new IOException( "No connection is avalibale");
		}
	}

	@Override
	public void close(){
		disconnect();
	}
}
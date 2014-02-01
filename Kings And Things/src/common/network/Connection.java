package common.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.InetSocketAddress;

/**
 * primary class for sending and receiving text from client or server
 */
public class Connection implements Closeable{
	
	private Socket socket = null;
	private PrintWriter output;
	private BufferedReader input;
	private InetSocketAddress address;
	private boolean isConnected = false;
	
	/**
	 * create a connection with a specific socket, used primarily
	 * by server side after receiving connection from client, no
	 * need to call connectTo when using this constructor.
	 * setEventID must be called
	 * @param socket - established socket
	 */
	public Connection( Socket socket){
		this.socket = socket;
		connectTo( null, 0);
	}
	
	public Connection() {}

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
	 * connect to a specific address and create in out streams
	 * @param ip - destination IP address
	 * @param port - destination port
	 * @return true if all streams are created and connection established, otherwise false
	 */
	public boolean connectTo( String ip, int port){
		if( socket==null && ip==null){
			throw new IllegalArgumentException( "IP address cannot be null, port must be a positive none zero integer");
		}
		try {
			if( socket==null){
				address = new InetSocketAddress( ip, port);
				socket = new Socket();
				socket.connect( address);
			}
			output = new PrintWriter( socket.getOutputStream(), true);
            input = new BufferedReader( new InputStreamReader( socket.getInputStream()));
			isConnected = true;
		} catch( Exception e){
			disconnect();
			//e.printStackTrace();
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
				output.close();
			}
			if( socket!=null){
				try {
					socket.close();
				} catch ( IOException e) {
					e.printStackTrace();
				}
			}
			socket = null;
			isConnected = false;
		} else{
		}
	}
	
	/**
	 * send a text message to destination
	 * @param message - information to be sent
	 * @return true if information has been sent, else false
	 */
	public boolean send( String message){
		if( isConnected){
			output.println( message);
			return true;
		} else{
			return false;
		}
	}
	
	/**
	 * Receive response from destination in form of a text
	 * @return a string if message is received, otherwise null
	 */
	public String recieve(){
		if( isConnected){
			String message = null;
			try {
				message = input.readLine();
			} catch ( IOException e) {
				e.printStackTrace();
			}
			if( message!=null){
				return message;
			}else{
				isConnected = false;
			}
		}
		return null;
	}

	@Override
	public void close(){
		disconnect();
	}
}
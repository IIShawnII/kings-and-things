package common.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.InetSocketAddress;

import common.Constants.Level;
import common.event.EventMonitor;

/**
 * primary class for sending and receiving text from client or server
 */
public class Connection implements Closeable{
	
	private int EVENT_ID;
	private Socket socket = null;
	private PrintWriter output;
	private BufferedReader input;
	private InetSocketAddress address;
	private boolean isConnected = false;
	
	/**
	 * main constructor for creating a connection that is not connected.
	 * setEventID must be called
	 */
	public Connection(int eventID){
		EVENT_ID = eventID;
	}
	
	/**
	 * create a connection with a specific socket, used primarily
	 * by server side after receiving connection from client, no
	 * need to call connectTo when using this constructor.
	 * setEventID must be called
	 * @param socket - established socket
	 */
	public Connection( Socket socket, int eventID){
		EVENT_ID = eventID;
		this.socket = socket;
		connectTo( null, 0);
	}
	
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
			EventMonitor.fireEvent( EVENT_ID, "IP address cannot be null, port must be a positive none zero integer", Level.Error);
			throw new IllegalArgumentException( "IP address cannot be null, port must be a positive none zero integer");
		}
		try {
			if( socket==null){
				address = new InetSocketAddress( ip, port);
				socket = new Socket();
				socket.connect( address);
				EventMonitor.fireEvent( EVENT_ID, "Connected to " + address, Level.Notice);
			}
			output = new PrintWriter( socket.getOutputStream(), true);
			EventMonitor.fireEvent( EVENT_ID, "Output steam established", Level.Notice);
            input = new BufferedReader( new InputStreamReader( socket.getInputStream()));
            EventMonitor.fireEvent( EVENT_ID, "Input stream established", Level.Notice);
			isConnected = true;
		//} catch( ConnectException e){
			
		} catch( Exception e){
			EventMonitor.fireEvent( EVENT_ID, e.getMessage(), Level.Error);
			EventMonitor.fireEvent( EVENT_ID, "Disconnecting", Level.Warning);
			disconnect();
			e.printStackTrace();
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
					EventMonitor.fireEvent( EVENT_ID, "Input stream closed", Level.Warning);
				} catch ( IOException e) {
					EventMonitor.fireEvent( EVENT_ID, e.getMessage(), Level.Error);
					e.printStackTrace();
				}
			}
			if( output!=null){
				output.close();
				EventMonitor.fireEvent( EVENT_ID, "Output stream closed", Level.Warning);
			}
			if( socket!=null){
				try {
					socket.close();
					EventMonitor.fireEvent( EVENT_ID, "Socket closed", Level.Warning);
				} catch ( IOException e) {
					EventMonitor.fireEvent( EVENT_ID, e.getMessage(), Level.Error);
					e.printStackTrace();
				}
			}
			socket = null;
			isConnected = false;
		} else{
			EventMonitor.fireEvent( EVENT_ID, "Disconnected", Level.Warning);
		}
	}
	
	/**
	 * send a text message to destination
	 * @param message - information to be sent
	 * @return true if information has been sent, else false
	 */
	public boolean send( String message){
		if( isConnected){
			EventMonitor.fireEvent( EVENT_ID, "Sending: " + message, Level.Plain);
			output.println( message);
			return true;
		} else{
			EventMonitor.fireEvent( EVENT_ID, "No connection", Level.Warning);
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
				EventMonitor.fireEvent( EVENT_ID, e.getMessage(), Level.Error);
				e.printStackTrace();
			}
			if( message!=null){
				EventMonitor.fireEvent( EVENT_ID, "Recieved: " + message, Level.Plain);
				return message;
			}else{
				isConnected = false;
				EventMonitor.fireEvent( EVENT_ID, "Lost connection to" + socket, Level.Warning);
			}
		} else{
			EventMonitor.fireEvent( EVENT_ID, "No connection", Level.Warning);
		}
		return null;
	}

	@Override
	public void close(){
		disconnect();
	}
}
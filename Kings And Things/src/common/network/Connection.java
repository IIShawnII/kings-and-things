package common.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.InetSocketAddress;

import common.Constants.Level;
import common.event.EventMonitor;
import static common.Constants.CONSOLE;

/**
 * primary class for sending and receiving text from client or server
 */
public class Connection {
	
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private InetSocketAddress address;
	private boolean isConnected = false;
	
	/**
	 * connect to a specific address and create in out streams
	 * @param ip - distension IP address
	 * @param port - distension port
	 * @return true if all streams are created and connection established, otherwise false
	 */
	public boolean connectTo( String ip, int port){
		if( ip==null){
			EventMonitor.fireEvent( CONSOLE, "IP address cannot be null, port must be a positive none zero integer", Level.Error);
			return false;
		}
		address = new InetSocketAddress( ip, port);
		socket = new Socket();
		try {
			socket.connect( address);
			EventMonitor.fireEvent( CONSOLE, "Connected to " + address, Level.Notice);
			output = new PrintWriter( socket.getOutputStream(), true);
			EventMonitor.fireEvent( CONSOLE, "Output steam established", Level.Notice);
            input = new BufferedReader( new InputStreamReader( socket.getInputStream()));
            EventMonitor.fireEvent( CONSOLE, "Input stream established", Level.Notice);
			isConnected = true;
		} catch ( Exception e){
			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
			EventMonitor.fireEvent( CONSOLE, "Disconnecting", Level.Warning);
			disconnect();
			e.printStackTrace();
		}
		return isConnected;
	}
	
	/**
	 * close all streams and socket
	 */
	public void disconnect() {
		if( isConnected){
			if( input!=null){
				try {
					input.close();
					EventMonitor.fireEvent( CONSOLE, "Input stream closed", Level.Warning);
				} catch ( IOException e) {
					EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
					e.printStackTrace();
				}
			}
			if( output!=null){
				output.close();
				EventMonitor.fireEvent( CONSOLE, "Output stream closed", Level.Warning);
			}
			if( socket!=null){
				try {
					socket.close();
					EventMonitor.fireEvent( CONSOLE, "Socket closed", Level.Warning);
				} catch ( IOException e) {
					EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
					e.printStackTrace();
				}
			}
			isConnected = false;
		} else{
			EventMonitor.fireEvent( CONSOLE, "Disconnected", Level.Warning);
		}
	}
	
	/**
	 * send a text message to distension
	 * @param message - information to be sent
	 * @return true if information has been sent, else flase
	 */
	public boolean send( String message){
		if( isConnected){
			EventMonitor.fireEvent( CONSOLE, "Sending: " + message, Level.Plain);
			output.println( message);
			return true;
		} else{
			EventMonitor.fireEvent( CONSOLE, "No connection", Level.Warning);
			return false;
		}
	}
	
	/**
	 * Receive response from distension in form of a text
	 * @return a string if message is received, otherwise null
	 */
	public String recieve(){
		if( isConnected){
			String message = null;
			try {
				message = input.readLine();
			} catch ( IOException e) {
				EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
				e.printStackTrace();
			}
			if( message!=null){
				EventMonitor.fireEvent( CONSOLE, "Recieved: " + message, Level.Plain);
				return message;
			}else{
				isConnected = false;
				EventMonitor.fireEvent( CONSOLE, "Lost connection to" + socket, Level.Warning);
			}
		} else{
			EventMonitor.fireEvent( CONSOLE, "No connection", Level.Warning);
		}
		return null;
	}
}
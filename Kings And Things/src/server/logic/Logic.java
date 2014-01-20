package server.logic;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import common.event.EventMonitor;
import static common.Constants.Level;
import static common.Constants.CONSOLE;
import static common.Constants.SERVER_PORT;
import static common.Constants.SERVER_TIMEOUT;

public class Logic implements Runnable, EndOfGame {

	private boolean close = false;
	private ServerSocket serverSocket;
	
	public Logic() throws IOException{
		try {
			serverSocket = new ServerSocket( SERVER_PORT);
            serverSocket.setSoTimeout( SERVER_TIMEOUT*1000);
			EventMonitor.fireEvent( CONSOLE, "Listening on port " + SERVER_PORT, Level.Notice);
		} catch ( IOException e) {
			EventMonitor.fireEvent( CONSOLE, "Failed to open port " + SERVER_PORT, Level.Error);
			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void run() {
		while( !close){
            try (Socket clientSocket = serverSocket.accept();
            		PrintWriter output = new PrintWriter( clientSocket.getOutputStream(), true);
            		BufferedReader input = new BufferedReader( new InputStreamReader( clientSocket.getInputStream()));){
            	
	    		EventMonitor.fireEvent( CONSOLE, "Recieved connection from " + clientSocket, Level.Notice);
	    		String str;
	    		do{	
	    			str = input.readLine();
	    			if( str!=null){
		    			EventMonitor.fireEvent( CONSOLE, "Recieved: " + str, Level.Plain);
		    			
		    			str = new StringBuilder( str).reverse().toString();
		    			output.println( str);
		    			EventMonitor.fireEvent( CONSOLE, "Send: " + str, Level.Plain);
	    			}
            	}while( clientSocket.isConnected() && str!=null);
	    		EventMonitor.fireEvent( CONSOLE, "Lost connection from " + clientSocket, Level.Warning);
    			
            } catch( SocketTimeoutException ex){
                //try again for incoming connections
            } catch ( IOException e) {
    			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
				e.printStackTrace();
			}
        }
		try {
			serverSocket.close();
		} catch ( IOException e) {
			EventMonitor.fireEvent( CONSOLE, e.getMessage(), Level.Error);
			e.printStackTrace();
		}
	}

	@Override
	public void endGame() {
		close = true;
	}
}

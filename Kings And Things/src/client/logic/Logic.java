package client.logic;

import common.network.Connection;

public class Logic implements Runnable {

	private Connection connection;
	
	public Logic( Connection connection){
		this.connection = connection;
	}
	
	@Override
	public void run() {
		
	}
	
	
}

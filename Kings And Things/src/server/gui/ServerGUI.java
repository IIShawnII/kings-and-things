package server.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import server.logic.EndOfGame;
import common.Console;
import common.Logger;
import common.event.EventHandler;
import common.event.EventMonitor;
import static common.Constants.Level;
import static common.Constants.PLAYER;
import static common.Constants.CONSOLE;
import static common.Constants.PLAYER_INC;
import static common.Constants.MAX_PLAYERS;
import static common.Constants.CONSOLE_SIZE;

/**
 * server GUI to hold all and display all server related information
 */
@SuppressWarnings("serial")
public class ServerGUI extends JFrame implements Runnable, EventHandler{

	private EndOfGame end;
	private Console console;
	
	/**
	 * construct server GUI
	 * @param title - JFrame title
	 */
	public ServerGUI( String title){
		super( title);
	}

	/**
	 * start GUI
	 */
	@Override
	public void run() {
		EventMonitor.register( CONSOLE, this);
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		addWindowListener( new WindowListener());
		setContentPane( createGUI());
		pack();
		Dimension size = new Dimension( getWidth(), getHeight());
		setMinimumSize( size);
		setSize( size);
		setLocationRelativeTo( null);
		setVisible( true);
	}

	/**
	 * create all component of server GUI
	 * @return collection of created components in a JPanel
	 */
	private JPanel createGUI() {
		JPanel jpMain = new JPanel( new BorderLayout( 5, 5));
		
		JPanel jpPlayers = new JPanel( new GridLayout( 2, 2, 5, 5));
		PlayerStatus player;
		for( int ID=PLAYER, count=0; count<MAX_PLAYERS; ID+=PLAYER_INC, count++){
			player = new PlayerStatus( ID);
			player.initialize();
			jpPlayers.add( player);
		}
		jpMain.add( jpPlayers, BorderLayout.CENTER);
		
		console = new Console();
		console.setEditable( false);
		console.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( console);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jpMain.add( jsp, BorderLayout.SOUTH);
		
		return jpMain;
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosed(WindowEvent e){
			end.endGame();
			Logger.flush( "Server", console.getText());
		}
	}

	@Override
	public void handel( String message, Level level) {
		console.add( message, level);
	}

	public void setEnd( EndOfGame end) {
		this.end = end;
	}
}

package server.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.common.eventbus.Subscribe;

import server.event.EndServer;
import common.Console;
import common.Constants;
import common.event.ConsoleMessage;

/**
 * server GUI to hold all and display all server related information
 */
@SuppressWarnings("serial")
public class ServerGUI extends JFrame implements Runnable{

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
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weighty = 1;
		constraints.weightx = 1;
		
		JPanel jpPlayers = new JPanel( new GridLayout( 2, 2, 5, 5));
		PlayerStatus player;
		for( int ID=Constants.PLAYER_START_ID, count=0; count<Constants.MAX_PLAYERS; ID*=Constants.PLAYER_ID_MULTIPLIER, count++){
			player = new PlayerStatus( ID);
			player.initialize();
			jpPlayers.add( player);
		}
		constraints.gridy = 0;
		jpMain.add( jpPlayers, constraints);
		
		JPanel jpConsol = new JPanel( new BorderLayout());
		jpConsol.setBorder( BorderFactory.createEmptyBorder( 0, 5, 5, 5));
		console = new Console();
		console.setEditable( false);
		console.setPreferredSize( Constants.CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( console);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jpConsol.add( jsp, BorderLayout.CENTER);
		constraints.gridy = 1;
		jpMain.add( jpConsol, constraints);
		
		return jpMain;
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosed(WindowEvent e){
			new EndServer().postInternalEvent();
		}
	}
	
	@Subscribe
	public void setConsoleMessage( ConsoleMessage message){
		console.add( message.getMessage(), message.getLevel());
	}
}

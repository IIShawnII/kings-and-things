package client.gui;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import client.event.EndClient;
import common.Console;
import common.event.EventDispatch;
import common.game.LoadResources;
import static common.Constants.BOARD_SIZE;
import static common.Constants.MAX_PLAYERS;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.BYPASS_LOBBY;
import static common.Constants.BYPASS_MIN_PLAYER;
import static common.Constants.BYPASS_LOAD_IMAGES;

/**
 * client GUI to hold all and display all game related information
 */
@SuppressWarnings("serial")
public class ClientGUI extends JFrame implements Runnable{

	private Console console;
	
	/**
	 * construct Client GUI
	 * @param title - JFrame title
	 */
	public ClientGUI( String title){
		super( title);
	}

	/**
	 * start GUI
	 */
	@SuppressWarnings("unused")
	@Override
	public void run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		addWindowListener( new WindowListener());
        setLocationRelativeTo(null);
        setUndecorated(true);
        setVisible(true);
		LoadingDialog dialog = new LoadingDialog( new LoadResources( !BYPASS_LOAD_IMAGES), "Lobby", true, true, getGraphicsConfiguration());
		EventDispatch.registerForCommandEvents( dialog);
		int playerCount=BYPASS_MIN_PLAYER?MAX_PLAYERS:0;
		if( BYPASS_LOBBY || (playerCount=dialog.run())>0){
			EventDispatch.unregisterForCommandEvents( dialog);
        	dispose();
            setUndecorated(false);
			setContentPane( createGUI( playerCount));
			pack();
			Dimension size = new Dimension( getWidth(), getHeight());
			setMinimumSize( size);
	        setLocationRelativeTo(null);
			/*setMinimumSize( MIN_CLIENT_SIZE);
			setExtendedState( MAXIMIZED_BOTH);
			Rectangle bound = getBounds();
			bound.width -= bound.x*2;
			bound.height -= bound.y*2;
			bound.x = bound.y = 0;*/
			setVisible( true);
		}else{
			close();
		}
	}

	/**
	 * create all component of server GUI
	 * @return collection of created components in a JPanel
	 */
	private JComponent createGUI( int playerCount) {
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		Board board = new Board( null, true);
		EventDispatch.registerForCommandEvents( board);
		board.setPreferredSize( BOARD_SIZE);
		board.setSize( BOARD_SIZE);
		board.init(playerCount);
		constraints.gridx = 0;
		constraints.gridy = 0;
		jpMain.add( board, constraints);

		JScrollPane jsp = new JScrollPane( jpMain);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		return jsp;
	}
	
	//TODO Add console to a separate dialog
	@SuppressWarnings("unused")
	private JPanel createTempPanel(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);

		console = new Console();
		console.setEditable( false);
		console.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( console);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		jpMain.add( jsp, constraints);
		
		return jpMain;
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e){
			close();
		}
	}
	
	private void close(){
		new EndClient().postCommand();
		dispose();
	}
}

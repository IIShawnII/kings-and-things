package client.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import com.google.common.eventbus.Subscribe;

import common.game.PlayerInfo;
import common.game.LoadResources;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.Constants.UpdateInstruction;
import static common.Constants.GUI;
import static common.Constants.LOGIC;
import static common.Constants.MAX_PLAYERS;
import static common.Constants.BYPASS_LOBBY;
import static common.Constants.LOAD_RESOURCE;
import static common.Constants.BYPASS_MIN_PLAYER;
import static common.Constants.BYPASS_LOAD_IMAGES;

/**
 * client GUI to hold all and display all game related information
 */
@SuppressWarnings("serial")
public class ClientGUI extends JFrame implements Runnable{

	private MultiBoardManager boards;
	
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
	@Override
	public void run() {
		EventDispatch.registerForCommandEvents( this);
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		addWindowListener( new WindowListener());
        setLocationRelativeTo(null);
        setUndecorated(true);
        setVisible(true);
		LoadingDialog dialog = new LoadingDialog( new LoadResources( !BYPASS_LOAD_IMAGES), "Lobby", true, true, getGraphicsConfiguration());
		EventDispatch.registerForCommandEvents( dialog);
		int playerCount=0;
		if( BYPASS_LOBBY || (playerCount=dialog.run())>0){
			EventDispatch.unregisterForCommandEvents( dialog);
        	dispose();
            setUndecorated(false);
            setJMenuBar( createMenu());
			setContentPane( createGUI( BYPASS_MIN_PLAYER?MAX_PLAYERS:playerCount));
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
		constraints.gridx = 0;
		constraints.gridy = 0;
		
		boards = new MultiBoardManager( jpMain, constraints, playerCount);
		boards.creatBoards();
		boards.showBoard( 0);

		JScrollPane jsp = new JScrollPane( jpMain);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		return jsp;
	}
	
	private JMenuBar createMenu(){
		JMenuBar jmb = new JMenuBar();
		jmb.setLayout( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		
		JComboBox< Object> jcbPlayers = new JComboBox<>( new Object[]{ 1,2,3,4});
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		constraints.weightx = 1;
		jmb.add( jcbPlayers, constraints);
		
		JCheckBox jcbActive = new JCheckBox( "Active", true);
		constraints.gridx = 3;
		constraints.weightx = 1.3;
		jmb.add( jcbActive, constraints);
		
		return jmb;
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e){
			close();
		}
	}
	
	private void close(){
		new UpdatePackage( UpdateInstruction.End, "GUI.Close").postCommand( LOGIC|LOAD_RESOURCE);
		dispose();
	}
	
	@Subscribe
	public void receiveUpdate( UpdatePackage update){
		if( update.isPublic() || update.getID()!=GUI){
			return;
		}
		
	}
	
	public void PlayerChanged( PlayerInfo player){
		
	}
}

package client.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import client.gui.components.MultiBoardManager;
import common.game.PlayerInfo;
import common.game.LoadResources;
import common.event.UpdatePackage;
import common.event.AbstractUpdateReceiver;
import common.Constants.UpdateKey;
import common.Constants.UpdateInstruction;
import static common.Constants.GUI;
import static common.Constants.BOARD_SIZE;
import static common.Constants.BYPASS_LOBBY;

/**
 * client GUI to hold all and display all game related information
 */
@SuppressWarnings("serial")
public class ClientGUI extends JFrame implements Runnable, ActionListener{

	private PlayerInfo[] players;
	private MultiBoardManager boards;
	private JComboBox< PlayerInfo> jcbPlayers;
	
	/**
	 * construct Client GUI
	 * @param title - JFrame title
	 */
	public ClientGUI( String title){
		super( title);
		new UpdateReceiver();
	}

	/**
	 * start GUI
	 */
	@Override
	public void run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		addWindowListener( new WindowListener());
		LoadingDialog dialog = null;
		if( !BYPASS_LOBBY){
	        setLocationRelativeTo(null);
	        setUndecorated(true);
	        setVisible(true);
	        dialog = new LoadingDialog( new LoadResources( true), "Lobby", true, true, getGraphicsConfiguration());
			dialog.run();
        	dispose();
		}
		if(dialog==null || !dialog.isForceClosed()){
	        setUndecorated(false);
	        setJMenuBar( createMenu());
			setContentPane( createGUI());
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
	 */
	private JComponent createGUI() {
		JPanel jpMain = new JPanel( new GridBagLayout());
		jpMain.setPreferredSize( BOARD_SIZE);
		jpMain.setSize( BOARD_SIZE);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;

		boards = new MultiBoardManager( jpMain, constraints);
		boards.creatBoards( players);
		boards.show( -1);
		
		for(PlayerInfo player:players){
			jcbPlayers.addItem( player);
		}

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
		
		jcbPlayers = new JComboBox<>();
		jcbPlayers.addActionListener( this);
		constraints.gridx = 0;
		constraints.weightx = 1;
		jmb.add( jcbPlayers, constraints);
		
		JCheckBox jcbActive = new JCheckBox( "Active", true);
		constraints.gridx = 1;
		constraints.weightx = 1;
		jmb.add( jcbActive, constraints);
		
		return jmb;
	}
	
	private void close(){
		new UpdatePackage( UpdateInstruction.End, "GUI.Close", this).postInternalEvent();
		dispose();
	}

	@Override
	public void actionPerformed( ActionEvent e) {
		boards.show( ((PlayerInfo)jcbPlayers.getSelectedItem()).getID());
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e){
			close();
		}
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, GUI, ClientGUI.this);
		}

		@Override
		protected void handlePublic( UpdatePackage update) {
			changeBoad( update);
		}
	}
	
	private void changeBoad( UpdatePackage update){
		switch( update.peekFirstInstruction()){
			case UpdatePlayers:
				players = (PlayerInfo[])update.getData( UpdateKey.Players);
				break;
			default:
		}
	}
}

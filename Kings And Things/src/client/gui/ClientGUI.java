package client.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import client.gui.util.MultiBoardManager;
import common.game.PlayerInfo;
import common.game.LoadResources;
import common.event.UpdatePackage;
import common.event.AbstractUpdateReceiver;
import common.Constants;
import common.Constants.UpdateKey;
import common.Constants.UpdateInstruction;
import common.Logger;

/**
 * client GUI to hold all and display all game related information
 */
@SuppressWarnings("serial")
public class ClientGUI extends JFrame implements Runnable, ActionListener{

	private JCheckBox jcbActive;
	private PlayerInfo player;
	private PlayerInfo[] players;
	private JButton jbNext, jbPrev;
	private MultiBoardManager boards;
	private JComboBox< PlayerInfo> jcbPlayers;
	private boolean demo = false;
	
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
        setLocationRelativeTo(null);
        setUndecorated(true);
        setVisible(true);
        dialog = new LoadingDialog( new LoadResources( true), "Lobby", true, true, getGraphicsConfiguration());
		dialog.run();
		demo = dialog.isDemo();
    	dispose();
		if(dialog==null || !dialog.isForceClosed()){
	        setUndecorated(false);
	        if( demo){
	        	setJMenuBar( createMenu());
	        }
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
		jpMain.setPreferredSize( Constants.BOARD_SIZE);
		jpMain.setSize( Constants.BOARD_SIZE);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		
		if( demo){
			boards = new MultiBoardManager( jpMain, constraints, demo);
			boards.creatBoards( players);
			boards.show( -1);
			
			for(PlayerInfo player:players){
				jcbPlayers.addItem( player);
			}
		}else{
			Board board = new Board( demo, player);
			board.setPreferredSize( Constants.BOARD_SIZE);
			board.setSize( Constants.BOARD_SIZE);
			board.init( players.length);
			board.setActive( true);
			jpMain.add( board, constraints);
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
		constraints.weightx = 1;
		
		jcbPlayers = new JComboBox<>();
		jcbPlayers.addActionListener( this);
		constraints.gridx = 0;
		jmb.add( jcbPlayers, constraints);
		
		jcbActive = new JCheckBox( "Active", true);
		constraints.gridx = 1;
		jmb.add( jcbActive, constraints);
		
		jbPrev = new JButton("<<");
		jbPrev.addActionListener( this);
		constraints.gridx = 2;
		jmb.add( jbPrev, constraints);
		
		jbNext = new JButton(">>");
		jbNext.addActionListener( this);
		constraints.gridx = 3;
		jmb.add( jbNext, constraints);
		
		return jmb;
	}
	
	private void close(){
		new UpdatePackage( UpdateInstruction.End, "GUI.Close", this).postInternalEvent();
		dispose();
	}

	@Override
	public void actionPerformed( ActionEvent e) {
		Object source = e.getSource();
		PlayerInfo player = null;
		int index = -1;
		int MAX_INDEX = jcbPlayers.getItemCount();
		if( source==jbPrev){
			index = jcbPlayers.getSelectedIndex()-1;
			if( index<=-1){
				index = MAX_INDEX-1;
			}
		}else if( source==jbNext){
			index = jcbPlayers.getSelectedIndex()+1;
			if( index>=MAX_INDEX){
				index = 0;
			}
		}
		if( index!=-1){
			player = (PlayerInfo)jcbPlayers.getItemAt( index);
			jcbPlayers.setSelectedIndex( index);
		}else{
			player = (PlayerInfo)jcbPlayers.getSelectedItem();
		}
		if( player!=null){
			boards.show( player);
		}
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e){
			close();
		}
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, Constants.GUI, ClientGUI.this);
		}

		@Override
		protected void handlePublic( UpdatePackage update) {
			changeBoad( update);
		}

		@Override
		protected void handlePrivate( UpdatePackage update) {
			changeBoad( update);
		}
		
		@Override
		protected boolean verifyPrivate( UpdatePackage update){
			return update.isValidID( ID);
		}
	}
	
	private void changeBoad( UpdatePackage update){
		switch( update.peekFirstInstruction()){
			case UpdatePlayers:
				player = (PlayerInfo)update.getData( UpdateKey.Player);
				players = (PlayerInfo[])update.getData( UpdateKey.Players);
				if( demo && boards!=null && jcbActive.isSelected()){
					for( PlayerInfo player :players){
						if( player.isActive()){
							boards.show( player);
						}
					}
				}
				break;
			default:
				Logger.getStandardLogger().warn( "ClientGUI: NO handle for: " + update.peekFirstInstruction());
		}
	}
}

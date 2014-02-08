package client.gui;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.google.common.eventbus.Subscribe;

import client.event.ConnectionAction;
import client.event.ConnectionState;
import client.event.UpdatePlayerNames;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import common.Constants.NetwrokAction;
import common.PlayerInfo;
import static common.Constants.SERVER_IP;
import static common.Constants.SERVER_PORT;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.LOADING_SIZE;
import static common.Constants.PROGRESS_SIZE;
import static common.Constants.IP_COLUMN_COUNT;
import static common.Constants.PORT_COLUMN_COUNT;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog{

	private boolean progress;
	private InputControl control;
	private String title;
	private Runnable task;
	private JPanel jpProgress;
	private DefaultListModel< PlayerInfo> listModel;
	private JTextField jtfIP, jtfPort, jtfName;
	private JButton jbConnect, jbDisconnect, jbReady;
	private JProgressBar jpbHex, jpbCup, jpbBuilding;
	private JProgressBar jpbGold, jpbSpecial, jpbState;
	private boolean result = false, isConnected = false;
	
	public LoadingDialog( Runnable task, String title, boolean modal, boolean progress, GraphicsConfiguration gc) {
		super( (Frame)null, title, modal, gc);
		this.task = task;
		this.title = title;
		this.progress = progress;
		control = new InputControl();
	}

	public boolean run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		setContentPane( createGUI());
		pack();
		setMinimumSize( LOADING_SIZE);
		setLocationRelativeTo( null);
		Thread thread = new Thread( task, title);
		thread.setDaemon( true);
		thread.start();
		setVisible( true);
		return result;
	}
	
	private JPanel createGUI(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);

		JLabel label = new JLabel( "User Name:");
		constraints.gridy = 0;
		jpMain.add( label, constraints);
		
		jbReady = new JButton( "(Un)Ready");
		jbReady.setEnabled( false);
		jbReady.addActionListener( control);
		constraints.gridy = 1;
		jpMain.add( jbReady, constraints);
		
		JButton jbCancel = new JButton( "Cancel");
		jbCancel.setActionCommand( "Cancel");
		jbCancel.addActionListener( control);
		constraints.gridy = 2;
		jpMain.add( jbCancel, constraints);
		
		jbConnect = new JButton( "Connect");
		jbConnect.addActionListener( control);
		constraints.gridy = 3;
		jpMain.add( jbConnect, constraints);
		
		jbDisconnect = new JButton( "Disonnect");
		jbDisconnect.setEnabled( false);
		jbDisconnect.addActionListener( control);
		constraints.gridy = 4;
		jpMain.add( jbDisconnect, constraints);
		
		label = new JLabel( "IP:");
		constraints.gridy = 6;
		constraints.gridx = 1;
		jpMain.add( label, constraints);
		
		label = new JLabel( "Port:");
		constraints.gridx = 3;
		jpMain.add( label, constraints);
		
		jtfIP = new JTextField( SERVER_IP, IP_COLUMN_COUNT);
		constraints.gridx = 2;
		constraints.weightx = .6;
		jpMain.add( jtfIP, constraints);
		
		jtfPort = new JTextField( SERVER_PORT+"", PORT_COLUMN_COUNT);
		constraints.gridx = 4;
		constraints.weightx = .4;
		jpMain.add( jtfPort, constraints);
		
		jtfName = new JTextField();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 1;
		jpMain.add( jtfName, constraints);
		
		if( progress){
			jpProgress = createLoadingPanel();
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.gridx = 0;
			constraints.gridy = 7;
			constraints.weightx = 1;
			jpMain.add( jpProgress, constraints);
		}

		listModel = new DefaultListModel<>();
		JList<PlayerInfo> jlPlayers = new JList<>( listModel);
		jlPlayers.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( jlPlayers);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		constraints.gridheight = 5;
		constraints.weighty = 1;
		constraints.gridx = 1;
		constraints.gridy = 1;
		jpMain.add( jsp, constraints);
		
		return jpMain;
	}
	
	private JPanel createLoadingPanel(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		jpMain.setPreferredSize( PROGRESS_SIZE);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);
		
		JLabel label = new JLabel("HEX");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 0;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbHex = new JProgressBar( JProgressBar.HORIZONTAL, 0, 8);
		jpbHex.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbHex, constraints);
		
		label = new JLabel("Cup");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 1;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbCup = new JProgressBar( JProgressBar.HORIZONTAL, 0, 158);
		jpbCup.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbCup, constraints);
		
		label = new JLabel("Building");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 2;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbBuilding = new JProgressBar( JProgressBar.HORIZONTAL, 0, 6);
		jpbBuilding.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbBuilding, constraints);
		
		label = new JLabel("Gold");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 3;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbGold = new JProgressBar( JProgressBar.HORIZONTAL, 0, 6);
		jpbGold.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbGold, constraints);
		
		label = new JLabel("Special");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 4;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbSpecial = new JProgressBar( JProgressBar.HORIZONTAL, 0, 22);
		jpbSpecial.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbSpecial, constraints);
		
		label = new JLabel("State");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 5;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbState = new JProgressBar( JProgressBar.HORIZONTAL, 0, 4);
		jpbState.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbState, constraints);
		
		return jpMain;
	}
	
	public void removeProgress(){
		remove( jpProgress);
		jpProgress = null;
		Dimension size = getSize();
		size.height -= PROGRESS_SIZE.height;
		setMinimumSize( size);
		setPreferredSize( size);
		setSize( size);
		revalidate();
	}
	
	public void close(){
		setVisible( false);
		task = null;
		dispose();
	}
	
	private class InputControl implements ActionListener {
		
		@Override
		public void actionPerformed( ActionEvent e) {
			Object source = e.getSource();
			if( source==jbConnect){
				new ConnectionAction( jtfName.getText().trim(), jtfIP.getText().trim(), Integer.parseInt( jtfPort.getText().trim())).postCommand();
			}else if( source==jbDisconnect){
				new ConnectionAction( NetwrokAction.Disconnect).postCommand();
			}else if( isConnected && source==jbReady){
				new ConnectionAction( NetwrokAction.ReadyState).postCommand();
			}else if( e.getActionCommand().equals( "Cancel")){
				result = false;
				dispose();
			}
		}
	}
	
	@Subscribe
	public void updateJList( UpdatePlayerNames names){
		listModel.removeAllElements();
		for( PlayerInfo player: names.getPlayers()){
			listModel.addElement( player);
		}
	}

	@Subscribe
	public void ConnectionState( ConnectionState conncetion){
		switch( conncetion.getAction()){
			case Connect:
				isConnected = true;
				jtfName.setEnabled( false);
				jtfIP.setEnabled( false);
				jtfPort.setEnabled( false);
				break;
			case Disconnect:
				isConnected = false;
				if( conncetion.getMessage()!=null){
					JOptionPane.showMessageDialog( this, conncetion.getMessage(), "Connection", JOptionPane.ERROR_MESSAGE);
				}
				listModel.removeAllElements();
				break;
			case StartGame:
				result = true;
				dispose();
			case ReadyState:
			default:
				return;
		}
		jbReady.setEnabled( isConnected);
		jbDisconnect.setEnabled( isConnected);
		jbConnect.setEnabled( !isConnected);
	}
	
	@Subscribe
	public void loadProgress( LoadProgress load) {
		switch( load.getCategory()){
			case Building:
				jpbBuilding.setValue( jpbBuilding.getValue()+1);
				break;
			case Cup:
				jpbCup.setValue( jpbCup.getValue()+1);
				break;
			case Gold:
				jpbGold.setValue( jpbGold.getValue()+1);
				break;
			case Hex:
				jpbHex.setValue( jpbHex.getValue()+1);
				break;
			case Special:
				jpbSpecial.setValue( jpbSpecial.getValue()+1);
				break;
			case State:
				jpbState.setValue( jpbState.getValue()+1);
				break;
			case END:
				remove( jpProgress);
				Dimension size = getSize();
				size.height -= PROGRESS_SIZE.height+10;
				setMinimumSize( size);
				setSize( size);
				revalidate();
				repaint();
			default:
				break;
		}
	}
}
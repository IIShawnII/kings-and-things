package common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import common.Constants.Level;
import common.Constants.Category;
import common.event.EventHandler;
import common.event.EventMonitor;
import common.network.Connection;

import static common.Constants.UPDATE;
import static common.Constants.SERVER_IP;
import static common.Constants.SERVER_PORT;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.LOADING_SIZE;
import static common.Constants.PROGRESS_SIZE;
import static common.Constants.IP_COLUMN_COUNT;
import static common.Constants.PORT_COLUMN_COUNT;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog implements Runnable, EventHandler{

	private boolean progress;
	private InputControl control;
	private Connection connection;
	private String title;
	private Runnable task;
	private Console players;
	private JPanel jpProgress;
	private JTextField jtfIP, jtfPort;
	private JProgressBar jpbHex, jpbCup, jpbBuilding;
	private JProgressBar jpbGold, jpbSpecial, jpbState;
	
	public LoadingDialog( Runnable task, String title, boolean modal, boolean progress, GraphicsConfiguration gc) {
		super( (Frame)null, title, modal, gc);
		this.task = task;
		this.title = title;
		this.progress = progress;
		control = new InputControl();
		EventMonitor.register( UPDATE, this);
	}

	@Override
	public void run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		setContentPane( createGUI());
		pack();
		setMinimumSize( LOADING_SIZE);
		setLocationRelativeTo( null);
		Thread thread = new Thread( task, title);
		thread.setDaemon( true);
		thread.start();
		setVisible( true);
	}
	
	private JPanel createGUI(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);
		
		JButton jbStart = new JButton( "Start");
		jbStart.setActionCommand( "Start");
		jbStart.addActionListener( control);
		constraints.gridy = 0;
		jpMain.add( jbStart, constraints);
		
		JButton jbCancel = new JButton( "Cancel");
		jbCancel.setActionCommand( "Cancel");
		jbCancel.addActionListener( control);
		constraints.gridy = 1;
		jpMain.add( jbCancel, constraints);
		
		JButton jbConnect = new JButton( "Connect");
		jbConnect.setActionCommand( "Connection");
		jbConnect.addActionListener( control);
		constraints.gridy = 2;
		jpMain.add( jbConnect, constraints);
		
		JLabel label = new JLabel( "IP:");
		constraints.gridy = 4;
		constraints.gridx = 1;
		jpMain.add( label, constraints);
		
		jtfIP = new JTextField( SERVER_IP, IP_COLUMN_COUNT);
		constraints.gridx = 2;
		jpMain.add( jtfIP, constraints);
		
		label = new JLabel( "Port:");
		constraints.gridx = 3;
		jpMain.add( label, constraints);
		
		jtfPort = new JTextField( SERVER_PORT+"", PORT_COLUMN_COUNT);
		constraints.gridx = 4;
		jpMain.add( jtfPort, constraints);
		
		if( progress){
			jpProgress = createLoadingPanel();
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.gridx = 0;
			constraints.gridy = 6;
			constraints.weightx = 1;
			jpMain.add( jpProgress, constraints);
		}
		
		players = new Console();
		players.setEditable( false);
		players.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( players);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		constraints.weighty = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = 4;
		constraints.gridx = 1;
		constraints.gridy = 0;
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

	public void setConnection( Connection connection) {
		this.connection = connection;
	}
	
	private class InputControl implements ActionListener {

		@Override
		public void actionPerformed( ActionEvent e) {
			if( connection!=null && e.getActionCommand().equals( "Connection")){
				JButton source = (JButton)e.getSource();
				if ( connection.isConnected()){
					connection.disconnect();
					source.setText( "Connect   ");
				}else{
					if(	connection.connectTo( jtfIP.getText(), Integer.parseInt( jtfPort.getText()))){
						source.setText( "Disconnect");
					}
				}
			}else if( e.getActionCommand().equals( "Start")){
				
			}else if( e.getActionCommand().equals( "Cancel")){
				dispose();
			}
		}
	}

	@Override
	public void handle( Object obj, Level level) {
		if( level==Level.END){
			remove( jpProgress);
			EventMonitor.unRegister( UPDATE);
			Dimension size = getSize();
			size.height -= PROGRESS_SIZE.height+10;
			setMinimumSize( size);
			setSize( size);
			revalidate();
			repaint();
		}
		switch( (Category)obj){
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
			default:
				break;
		}
	}
}

package common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;

import common.network.Connection;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static common.Constants.SERVER_IP;
import static common.Constants.SERVER_PORT;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.LOADING_SIZE;
import static common.Constants.PROGRESS_SIZE;
import static common.Constants.IP_COLUMN_COUNT;
import static common.Constants.PORT_COLUMN_COUNT;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog implements Runnable{

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
	}

	@Override
	public void run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		setContentPane( createGUI());
		pack();
		setMinimumSize( LOADING_SIZE);
		setLocationRelativeTo( null);
		/*Thread thread = new Thread( task, title);
		thread.setDaemon( true);
		thread.start();*/
		setVisible( true);
	}
	
	private JPanel createGUI(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);
		
		JButton jbStart = new JButton( "Start");
		constraints.gridy = 0;
		jpMain.add( jbStart, constraints);
		
		JButton jbCancel = new JButton( "Cancel");
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
		JPanel jpMain = new JPanel( new GridLayout( 6,1,5,5));
		jpMain.setPreferredSize( PROGRESS_SIZE);
		
		jpbHex = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100);
		jpbHex.setStringPainted( true);
		jpbHex.setString( "Hex: 0%");
		jpMain.add( jpbHex);
		
		jpbCup = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100);
		jpbCup.setStringPainted( true);
		jpbCup.setString( "Cup: 0%");
		jpMain.add( jpbCup);
		
		jpbBuilding = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100);
		jpbBuilding.setStringPainted( true);
		jpbBuilding.setString( "Building: 0%");
		jpMain.add( jpbBuilding);
		
		jpbGold = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100);
		jpbGold.setStringPainted( true);
		jpbGold.setString( "Gold: 0%");
		jpMain.add( jpbGold);
		
		jpbSpecial = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100);
		jpbSpecial.setStringPainted( true);
		jpbSpecial.setString( "Special: 0%");
		jpMain.add( jpbSpecial);
		
		jpbState = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100);
		jpbState.setStringPainted( true);
		jpbState.setString( "State: 0%");
		jpMain.add( jpbState);
		
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
			}
		}
	}
}

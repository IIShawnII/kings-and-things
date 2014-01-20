package client.gui;

import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import common.Constants.Level;
import common.Logger;
import common.Console;
import common.event.EventHandler;
import common.event.EventMonitor;
import common.network.Connection;
import static common.Constants.CONSOLE;
import static common.Constants.SERVER_IP;
import static common.Constants.SERVER_PORT;
import static common.Constants.CONSOLE_SIZE;

/**
 * client GUI to hold all and display all game related information
 */
@SuppressWarnings("serial")
public class ClientGUI extends JFrame implements Runnable, EventHandler{

	private Console console;
	private InputControl control;
	private Connection connection;
	private JTextField jtfIP, jtfPort;
	
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
		connection = new Connection();
		control = new InputControl();
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
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);

		JLabel label = new JLabel( "Input:");
		constraints.weightx = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		jpMain.add( label, constraints);
		
		JTextField jtfInput = new JTextField();
		jtfInput.setActionCommand( "Input");
		jtfInput.addActionListener( control);
		constraints.weightx = 1;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		jpMain.add( jtfInput, constraints);

		console = new Console();
		console.setEditable( false);
		console.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( console);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 1;
		jpMain.add( jsp, constraints);
		
		label = new JLabel( "IP:");
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		jpMain.add( label, constraints);
		
		jtfIP = new JTextField( SERVER_IP);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		jpMain.add( jtfIP, constraints);
		
		label = new JLabel( "Port:");
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 3;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		jpMain.add( label, constraints);
		
		jtfPort = new JTextField( SERVER_PORT+"");
		constraints.weightx = .5;
		constraints.gridwidth = 1;
		constraints.gridx = 4;
		constraints.gridy = 2;
		jpMain.add( jtfPort, constraints);
		
		JButton jbConnect = new JButton( "Connect");
		jbConnect.setActionCommand( "Connect");
		jbConnect.addActionListener( control);
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 5;
		constraints.gridy = 2;
		jpMain.add( jbConnect, constraints);
		
		JButton jbClose = new JButton( "Disconnect");
		jbClose.setActionCommand( "Disconnect");
		jbClose.addActionListener( control);
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 6;
		constraints.gridy = 2;
		jpMain.add( jbClose, constraints);
		
		return jpMain;
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosed(WindowEvent e){
			connection.disconnect();
			Logger.flush( "Client", console.getText());
		}
	}
	
	private class InputControl implements ActionListener {

		@Override
		public void actionPerformed( ActionEvent e) {
			if( e.getActionCommand().equals( "Connect")){
				connection.connectTo( jtfIP.getText(), Integer.parseInt( jtfPort.getText()));
			}else if( e.getActionCommand().equals( "Input")){
				JTextField source = (JTextField)e.getSource();
				String input = source.getText();
				if( input.length()>0){
					connection.send(input);
					connection.recieve();
					source.setText( "");
				}
			}else{
				connection.disconnect();
			}
		}
	}

	@Override
	public void handel( String message, Level level) {
		console.add( message, level);
	}
}

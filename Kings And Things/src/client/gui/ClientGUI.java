package client.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import common.Logger;
import common.Console;
import common.Constants.Level;
import common.event.EventHandler;
import common.event.EventMonitor;
import common.network.Connection;
import static common.Constants.CONSOLE;
import static common.Constants.SERVER_IP;
import static common.Constants.BOARD_SIZE;
import static common.Constants.SERVER_PORT;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.IP_COLUMN_COUNT;
import static common.Constants.MIN_CLIENT_SIZE;
import static common.Constants.PORT_COLUMN_COUNT;

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
		control = new InputControl();
		connection = new Connection( CONSOLE);
		EventMonitor.register( CONSOLE, this);
	}

	/**
	 * start GUI
	 */
	@Override
	public void run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		addWindowListener( new WindowListener());
		//setContentPane( createGUI());
		pack();
		setMinimumSize( MIN_CLIENT_SIZE);
		setLocationRelativeTo( null);
		setExtendedState( MAXIMIZED_BOTH);
		setVisible( true);
		Rectangle bound = getBounds();
		System.out.println(bound);
		bound.width -= bound.x*2;
		bound.height -= bound.y*2;
		bound.x = bound.y = 0;
		System.out.println(bound);
		repaint();
	}

	/**
	 * create all component of server GUI
	 * @return collection of created components in a JPanel
	 */
	private JComponent createGUI() {
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		//constraints.insets = new Insets( 5, 5, 5, 5);

		Board board = new Board( null, true);
		board.setPreferredSize( BOARD_SIZE);
		//board.init();
		constraints.gridx = 0;
		constraints.gridy = 0;
		jpMain.add( board, constraints);

		/*constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		jpMain.add( createTempPanel(), constraints);*/
		

		JScrollPane jsp = new JScrollPane( jpMain);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		return jsp;
	}
	
	//TODO Add console to a seprate dialog
	@SuppressWarnings("unused")
	private JPanel createTempPanel(){
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
		
		label = new JLabel( "IP:");
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		jpMain.add( label, constraints);
		
		jtfIP = new JTextField( SERVER_IP, IP_COLUMN_COUNT);
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		jpMain.add( jtfIP, constraints);
		
		label = new JLabel( "Port:");
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		jpMain.add( label, constraints);
		
		jtfPort = new JTextField( SERVER_PORT+"", PORT_COLUMN_COUNT);
		constraints.weightx = .5;
		constraints.gridwidth = 1;
		constraints.gridx = 4;
		constraints.gridy = 1;
		jpMain.add( jtfPort, constraints);
		
		JButton jbConnect = new JButton( "Connect   ");
		jbConnect.setActionCommand( "Connection");
		jbConnect.addActionListener( control);
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 5;
		constraints.gridy = 1;
		jpMain.add( jbConnect, constraints);

		console = new Console();
		console.setEditable( false);
		console.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( console);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		jpMain.add( jsp, constraints);
		
		return jpMain;
	}
	
	private class WindowListener extends WindowAdapter{
		
		@Override
		public void windowClosed(WindowEvent e){
			connection.disconnect();
			if( console!=null){
				Logger.flush( "Client", console.getText());
			}
		}
	}
	
	private class InputControl implements ActionListener {

		@Override
		public void actionPerformed( ActionEvent e) {
			if( e.getActionCommand().equals( "Connection")){
				JButton source = (JButton)e.getSource();
				if (connection.isConnected()){
					connection.disconnect();
					source.setText( "Connect   ");
				}else{
					if(	connection.connectTo( jtfIP.getText(), Integer.parseInt( jtfPort.getText()))){
						source.setText( "Disconnect");
					}
				}
			}else if( e.getActionCommand().equals( "Input")){
				JTextField source = (JTextField)e.getSource();
				String input = source.getText();
				if( input.length()>0){
					connection.send(input);
					connection.recieve();
					source.setText( "");
				}
			}
		}
	}

	@Override
	public void handle( String message, Level level) {
		if( console!=null){
			console.add( message, level);
		}
	}
}

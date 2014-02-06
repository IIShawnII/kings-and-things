package client.gui;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import common.Console;
import common.LoadingDialog;
import common.LoadResources;
import common.Constants.Level;

import static common.Constants.BOARD_SIZE;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.MIN_CLIENT_SIZE;

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
	@Override
	public void run() {
		setDefaultCloseOperation( DISPOSE_ON_CLOSE);
		addWindowListener( new WindowListener());
		pack();
		setMinimumSize( MIN_CLIENT_SIZE);
		setLocationRelativeTo( null);
		setExtendedState( MAXIMIZED_BOTH);
		setVisible( true);
		Rectangle bound = getBounds();
		bound.width -= bound.x*2;
		bound.height -= bound.y*2;
		bound.x = bound.y = 0;
		LoadingDialog dialog = new LoadingDialog( new LoadResources(), "Loby", true, true, getGraphicsConfiguration());
		if( dialog.run()){
			setContentPane( createGUI());
			revalidate();
		}else{
			dispose();
		}
	}

	/**
	 * create all component of server GUI
	 * @return collection of created components in a JPanel
	 */
	private JComponent createGUI() {
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		Board board = new Board( null, true);
		board.setPreferredSize( BOARD_SIZE);
		//board.init();
		constraints.gridx = 0;
		constraints.gridy = 0;
		jpMain.add( board, constraints);

		JScrollPane jsp = new JScrollPane( jpMain);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		return jsp;
	}
	
	//TODO Add console to a separate dialog
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
		public void windowClosed(WindowEvent e){
			//TODO close client gui
		}
	}

	public void handle( String message, Level level) {
		if( console!=null){
			console.add( message, level);
		}
	}
}

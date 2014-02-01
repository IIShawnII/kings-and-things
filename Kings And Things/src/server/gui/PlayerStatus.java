package server.gui;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import common.Console;
import common.event.EventHandler;
import common.event.EventMonitor;
import static common.Constants.RED;
import static common.Constants.BLUE;
import static common.Constants.GREEN;
import static common.Constants.YELLOW;
import static common.Constants.DARK_RED;
import static common.Constants.DARK_BLUE;
import static common.Constants.DARK_GREEN;
import static common.Constants.DARK_YELLOW;
import static common.Constants.HOLD;
import static common.Constants.WAIT;
import static common.Constants.Level;
import static common.Constants.UPDATE;
import static common.Constants.PLAYER;
import static common.Constants.ANALYSE;
import static common.Constants.CONSOLE;
import static common.Constants.PLAYER_INC;
import static common.Constants.LABEL_SIZE;
import static common.Constants.CONSOLE_SIZE;
import static common.Constants.LABEL_FONT_SIZE;
import static common.Constants.PLAYER_FONT_SIZE;

/**
 * complete set of GUI components that represent the current state of a player and related server activities
 */
@SuppressWarnings("serial")
public class PlayerStatus extends JPanel implements EventHandler{

	private final int PLAYER_ID;

	private Font font;
	private Console console;
	
	/**
	 * @param PLAYER_ID - player id used in even monitor, must be a positive integer bigger or equal to common.Constants.PLAYER
	 */
	public PlayerStatus( final int PLAYER_ID) {
		super();
		if( PLAYER_ID<PLAYER){
			String message = "ERROR - PLAYER_ID Must be bigger than common.Constants.PLAYER";
			throw new IllegalArgumentException( message);
		}
		this.PLAYER_ID = PLAYER_ID;
	}
	
	/**
	 * create all components of PlayerStatus and register it with event monitor
	 */
	protected void initialize(){
		font = getFont();
		EventMonitor.register( PLAYER_ID+CONSOLE, this);
		setLayout( new BorderLayout( 5, 5));
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5));
		add( createStatusPanel(), BorderLayout.NORTH);
		add( createConsole(), BorderLayout.CENTER);
	}
	
	/**
	 * create a text area inside a scroll pane
	 * @return JPanel containing the console
	 */
	private JPanel createConsole(){
		JPanel jpMain = new JPanel( new BorderLayout());
		console = new Console();
		console.setEnabled( false);
		console.setEditable( false);
		console.setPreferredSize( CONSOLE_SIZE);
		JScrollPane jsp = new JScrollPane( console);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jpMain.add( jsp, BorderLayout.CENTER);
		return jpMain;
	}
	
	/**
	 * create JPanel with all Status labels of the server functions
	 * @return collection of StatusLabels in a JPanel
	 */
	private JPanel createStatusPanel(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.fill = GridBagConstraints.HORIZONTAL;

		Font player = font.deriveFont( Font.BOLD, font.getSize() + PLAYER_FONT_SIZE);
		Font label = font.deriveFont( Font.BOLD, font.getSize() + LABEL_FONT_SIZE);
		
		JLabel jlPlayer = new JLabel( "Player " + (((PLAYER_ID-PLAYER)/PLAYER_INC)+1));
		jlPlayer.setAlignmentX( LEFT_ALIGNMENT);
		jlPlayer.setFont( player);
		con.weightx = 1.0;
		jpMain.add( jlPlayer, con);
			
		StatusLabel wait = new StatusLabel();
		wait.setAlignmentX( RIGHT_ALIGNMENT);
		con.weightx = 0.0;
		wait.initialize( "Waiting for Players move", "W", YELLOW, DARK_YELLOW, label, PLAYER_ID + WAIT, LABEL_SIZE);
		jpMain.add( wait, con);
		
		StatusLabel analyse = new StatusLabel();
		analyse.setAlignmentX( RIGHT_ALIGNMENT);
		analyse.initialize( "Analysing Player Move", "A", BLUE, DARK_BLUE, label, PLAYER_ID + ANALYSE, LABEL_SIZE);
		jpMain.add( analyse, con);
		
		StatusLabel hold = new StatusLabel();
		hold.setAlignmentX( RIGHT_ALIGNMENT);
		hold.initialize( "Waiting for Players Turn", "H", RED, DARK_RED, label, PLAYER_ID + HOLD, LABEL_SIZE);
		jpMain.add( hold, con);
		
		StatusLabel update = new StatusLabel();
		update.setAlignmentX( RIGHT_ALIGNMENT);
		update.initialize( "Syncing Data", "U", GREEN, DARK_GREEN, label, PLAYER_ID + UPDATE, LABEL_SIZE);
		jpMain.add( update, con);
		
		return jpMain;
	}

	/**
	 * called by event monitor for updating the console
	 * @param message - new message to be added to display
	 */
	@Override
	public void handle( Object obj, Level level) {
		if( obj!=null){
			console.setEnabled( true);
			console.add( (String)obj, level);
		}else if (level==Level.END){
			console.setEnabled( false);
		}
	}
}

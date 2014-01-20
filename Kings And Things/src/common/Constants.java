package common;

import java.awt.Color;
import java.awt.Dimension;

public interface Constants {
	
	enum Level { Error, Warning, Notice, Plain, END}

	//Colors
	public static final Color RED = new Color( 255, 0, 0);
	public static final Color DARK_RED = new Color( 128, 0, 0);
	public static final Color GREEN = new Color( 0, 255, 0);
	public static final Color DARK_GREEN = new Color( 0, 100, 0);
	public static final Color BLUE = new Color( 0, 200, 255);
	public static final Color DARK_BLUE = new Color( 0, 75, 128);
	public static final Color YELLOW = new Color( 255, 255, 0);
	public static final Color DARK_YELLOW = new Color( 128, 128, 0);
	public static final Color COLOR_ERROR = RED;
	public static final Color COLOR_PLAIN = Color.BLACK;
	public static final Color COLOR_NOTICE = DARK_GREEN;
	public static final Color COLOR_WARNNING = DARK_YELLOW;
	
	//keys for events
	public static final int HOLD = 0;         //00000000
	public static final int WAIT = 1;         //00000001
	public static final int UPDATE = 2;       //00000010
	public static final int ANALYSE = 3;      //00000011
	public static final int CONSOLE = 4;      //00000100
	public static final int ENDGAME = 5;      //00000101
	public static final int PLAYER = 128;     //10000000
	public static final int PLAYER_INC = 16;  //00010000
	
	//Maximums
	public static final int MAX_PLAYERS = 4;
	
	//Minimums
	public static final int MIN_PLAYERS = 2;
	
	//Sizes
	public static final int LABEL_FONT_SIZE = 25;
	public static final int PLAYER_FONT_SIZE = 12;
	public static final Dimension LABEL_SIZE = new Dimension( 50,50);
	public static final Dimension CONSOLE_SIZE = new Dimension( 400,150);
	
	//Defaults
	public static final int SERVER_TIMEOUT = 10;
	public static final int SERVER_PORT = 12345;
	public static final String SERVER_IP = "127.0.0.1";
}

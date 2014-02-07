package common;

import java.util.HashMap;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Color;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Dimension;
import java.awt.Rectangle;


public final class Constants {
	
	public enum Level { Error, Warning, Notice, Plain, END, LOADING_DIALOG}
	public enum Category { Resources, Building, Cup, Gold, Hex, Special, State, Misc}
	public enum Ability { Charge, Fly, Range, Special, Magic, Armor, Neutralised}
	public enum Restriction { Gold, Magic, Treasure, Building, Event, Special, State, Battle, Sea,
			Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp, Yellow, Red, Green, Gray}
	public enum Biome { Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp, Sea}
	public enum Building {Castle, Citadel, City, Keep, Tower, Village}
	public enum BuildableBuilding {Tower, Keep, Castle, Citadel}
	
	//Note, phase progression is dependent on the order of the declarations in the following enum type
	public enum SetupPhase {PICK_FIRST_HEX, EXCHANGE_SEA_HEXES, PICK_SECOND_HEX, PICK_THIRD_HEX, PLACE_FREE_TOWER, PLACE_FREE_THINGS, EXCHANGE_THINGS, PLACE_EXCHANGED_THINGS, SETUP_FINISHED}
	
	//Regular turn phases
	public enum RegularPhase {RECRUITING_CHARACTERS, RECRUITING_THINGS, RANDOM_EVENTS, MOVEMENT, COMBAT, CONSTRUCTION, SPECIAL_POWERS}
	
	//Resources
	public static final Image IMAGE_BACKGROUND;
	public static final Image IMAGE_HEX_REVERSE; 
	public static final Image IMAGE_TILE_REVERSE;
	public static final HashMap< Integer, TileProperties> HEX = new HashMap<>();
	public static final HashMap< Integer, TileProperties> CUP = new HashMap<>();
	public static final HashMap< Integer, TileProperties> GOLD = new HashMap<>();
	public static final HashMap< Integer, TileProperties> SPECIAL = new HashMap<>();
	public static final HashMap< Integer, TileProperties> BUILDING = new HashMap<>();
	public static final HashMap< Restriction, TileProperties> STATE = new HashMap<>();
	
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
	public static final int PLAYER = 128;     //10000000
	public static final int PLAYER_INC = 16;  //00010000
	
	//Maximums
	public static final int MAX_HEXES = 48;
	public static final int MAX_PLAYERS = 4;
	public static final int MAX_HEXES_ON_BOARD = 37;
	public static final int MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX = 10;
	
	//Minimums
	public static final int MIN_PLAYERS = 2;
	public static final int MIN_HEXES_ON_BOARD = 19;
	public static final Dimension MIN_CLIENT_SIZE = new Dimension( 1300,720);
	
	//Sizes
	public static final int LOCK_SIZE = 28;
	public static final int HEX_HEIGHT = 70;
	public static final Polygon HEX_OUTLINE;
	public static final int HEX_SPACING = 16;
	public static final int LABEL_FONT_SIZE = 25;
	public static final int IP_COLUMN_COUNT = 12;
	public static final int PLAYER_FONT_SIZE = 12;
	public static final int PORT_COLUMN_COUNT = 7;
	public static final int BOARD_TOP_PADDING = 100;
	public static final int BOARD_WIDTH_SEGMENT = 8;
	public static final int BOARD_PLAYERS_STATE = 450;
	public static final int BOARD_HEIGHT_SEGMENT = 14;
	public static final double HEX_RATIO = 752.0/658.0;
	public static final int BOARD_BOTTOM_PADDING = 120;
	public static final Dimension LABEL_SIZE = new Dimension( 50,50);
	public static final Dimension TILE_SIZE = new Dimension( 430,440);
	public static final Dimension CONSOLE_SIZE = new Dimension( 100,80);
	public static final Dimension LOADING_SIZE = new Dimension( 350,425);
	public static final Dimension PROGRESS_SIZE = new Dimension( LOADING_SIZE.width,170);
	public static final Rectangle FACE_DOWN = new Rectangle( 0,0,TILE_SIZE.width,TILE_SIZE.height);
	public static final Dimension HEX_SIZE = new Dimension( (int)(HEX_HEIGHT*HEX_RATIO),HEX_HEIGHT);
	public static final Rectangle FACE_UP = new Rectangle( TILE_SIZE.width/2,0,TILE_SIZE.width,TILE_SIZE.height);
	public static final Dimension HEX_BOARD_SIZE = new Dimension( HEX_SIZE.height*BOARD_HEIGHT_SEGMENT/2 + HEX_SPACING, HEX_SIZE.height*BOARD_HEIGHT_SEGMENT/2 + HEX_SPACING);
	public static final Dimension BOARD_SIZE = new Dimension( HEX_BOARD_SIZE.width + BOARD_PLAYERS_STATE, HEX_BOARD_SIZE.height + BOARD_BOTTOM_PADDING);
	
	//Defaults
	public static final int SPIRAL_DELAY = 5;
	public static final int INFINITE_TILE = -1;
	public static final int SERVER_TIMEOUT = 5;
	public static final int SERVER_PORT = 12345;
	public static final boolean PLAYER_READY = true;
	public static final String SERVER_IP = "127.0.0.1";
	public static final String GAME_TITLE = "Kings & Things";
	public static final Path RESOURCE_PATH = Paths.get( "Resources/");
	public static final int HEX_MOVE_DISTANCE = (int) (HEX_HEIGHT*0.5);
	public static final int BOARD_LOAD_ROW[][] = { { 7, 5, 6, 8, 9, 8, 6},
													{4, 3, 4, 5, 7, 9, 10, 11, 10, 9, 7, 5},
													{3, 2, 1, 2, 3, 4, 6, 8, 10, 11, 12, 13, 12, 11, 10, 8, 6, 4}};
	public static final int BOARD_LOAD_COL[][] = { { 4, 4, 5, 5, 4, 3, 3},
													{3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 2, 2},
													{2, 3, 4, 5, 6, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1}};
	
	static{
		int w = (int) (HEX_SIZE.getWidth()/4)+1;
		int h = (int) (HEX_SIZE.getHeight()/2)+2;
		HEX_OUTLINE = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		IMAGE_BACKGROUND = loadImage( "Resources\\Misc\\-n Woodboard.jpg");
		IMAGE_HEX_REVERSE = loadImage( "Resources\\Misc\\-n Hex_Reverse.png");
		IMAGE_TILE_REVERSE = loadImage( "Resources\\Misc\\-n Tile_Reverse.png");
	}
	
	private static Image loadImage( String path){
		try {
			return ImageIO.read( new File( path));
		} catch ( IOException e) {
			return null;
		}
	}
	
	//to prevent instances
	private Constants(){}
}

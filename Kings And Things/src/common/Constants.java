package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

import com.google.common.collect.ImmutableBiMap;
import common.game.TileProperties;

public final class Constants {
	
	//Primary for bypassing rules for 4 player, so it can be tested for one players
	public static final boolean BYPASS_MIN_PLAYER = false;
	public static final boolean BYPASS_MOUSE_CLICK = false;
	public static final boolean BYPASS_LOAD_IMAGES = true;
	public static final boolean BYPASS_LOBBY = true;
	public static final boolean DRAW_LOCKS = false;
	public static final boolean LOAD_BUILDING = false;
	public static final boolean LOAD_SPECIAL = false;
	public static final boolean LOAD_GOLD = false;
	public static final boolean LOAD_STATE = true;
	public static final boolean LOAD_CUP = false;
	public static final boolean LOAD_HEX = true;

	public enum UpdateKey {Command, Message, PlayerCount, Players, Name, Port, IP}
	public enum UpdateInstruction {Connect, Disconnect, ReadyState, Start, UpdatePlayers, Category, End, Send}
	public enum UpdateContent {Setup, Regular, Combat, PlayerInfo, PlayerOrder}
	public enum BuildableBuilding {Tower, Keep, Castle, Citadel}
	public enum Building {Castle, Citadel, City, Keep, Tower, Village}
	public enum Level { Error, Warning, Notice, Plain, END, LOADING_DIALOG}
	public enum Ability { Charge, Fly, Range, Special, Magic, Armor, Neutralised}
	public enum Category { Resources, Building, Cup, Gold, Hex, Special, State, Misc, END, Creature, Event, Magic, Treasure, Buildable}
	public enum Biome { Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp, Sea}
	public enum Restriction { Gold, Magic, Treasure, Building, Event, Special, State, Battle, Sea,
			Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp, Yellow, Red, Green, Gray}
	public enum RandomEvent {Big_Juju, Dark_Plague, Defection, Good_Harvest, Mother_Lode, Teenie_Pox, Terrain_Disaster, Vandalism, Weather_Control, Willing_Workers}
	
	
	//Note, phase progression is dependent on the order of the declarations in the following enum type
	public enum SetupPhase {DETERMINE_PLAYER_ORDER, PICK_FIRST_HEX, EXCHANGE_SEA_HEXES, PICK_SECOND_HEX, PICK_THIRD_HEX, PLACE_FREE_TOWER, PLACE_FREE_THINGS, EXCHANGE_THINGS, PLACE_EXCHANGED_THINGS, SETUP_FINISHED}
	
	//Regular turn phases
	public enum RegularPhase {RECRUITING_CHARACTERS, RECRUITING_THINGS, RANDOM_EVENTS, MOVEMENT, COMBAT, CONSTRUCTION, SPECIAL_POWERS}
	
	//Combat phases
	public enum CombatPhase {DETERMINE_DEFENDERS, SELECT_TARGET_PLAYER, MAGIC_ATTACK, APPLY_MAGIC_HITS, RANGED_ATTACK, APPLY_RANGED_HITS, MELEE_ATTACK, APPLY_MELEE_HITS, RETREAT, DETERMINE_DAMAGE, PLACE_THINGS, NO_COMBAT}
	
	//Reasons for dice rolls
	public enum RollReason {DETERMINE_PLAYER_ORDER, EXPLORE_HEX, ATTACK_WITH_CREATURE, CALCULATE_DAMAGE_TO_TILE, ENTERTAINMENT, RECRUIT_SPECIAL_CHARACTER}
	
	//Resources
	public static final Image IMAGE_BACKGROUND;
	public static final Image IMAGE_HEX_REVERSE; 
	public static final Image IMAGE_TILE_REVERSE;
	public static final HashMap< Integer, Image> IMAGES = new HashMap<>();
	public static final HashMap< Integer, TileProperties> HEX = new HashMap<>();
	public static final HashMap< Integer, TileProperties> CUP = new HashMap<>();
	public static final HashMap< Integer, TileProperties> GOLD = new HashMap<>();
	public static final HashMap< Restriction, TileProperties> STATE = new HashMap<>();
	public static final HashMap< Integer, TileProperties> SPECIAL = new HashMap<>();
	public static final HashMap< Integer, TileProperties> BUILDING = new HashMap<>();
	
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
	public static final int LOBBY = 0;					//00000000
	public static final int LOGIC = 1;					//00000001
	public static final int PROGRESS = 2;				//00000010
	public static final int LOAD_RESOURCE = 4;			//00000100
	public static final int GUI = 8;					//00001000
	public static final int PLAYER_ID_MULTIPLIER = 2;	//00010010
	public static final int PLAYER_START_ID = 16;		//00010000
	
	//Maximums
	public static final int MAX_HEXES = 48;
	public static final int MAX_PLAYERS = 4;
	public static final int MAX_RACK_SIZE = 10;
	public static final int MAX_MOVE_SPEED = 4;
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
	public static final int TIILE_HEIGHT = 60;
	public static final Rectangle TILE_OUTLINE;
	public static final int LABEL_FONT_SIZE = 25;
	public static final int IP_COLUMN_COUNT = 12;
	public static final int PLAYER_FONT_SIZE = 12;
	public static final int PORT_COLUMN_COUNT = 7;
	public static final int TIILE_HEIGHT_BOARD = 35;
	public static final int BOARD_TOP_PADDING = 100;
	public static final int BOARD_WIDTH_SEGMENT = 8;
	public static final int PLAYERS_STATE_SIZE = 500;
	public static final int BOARD_RIGHT_PADDING = 40;
	public static final int BOARD_HEIGHT_SEGMENT = 14;
	public static final int PLAYERS_STATE_PADDING = 35;
	public static final int BOARD_BOTTOM_PADDING = 150;
	public static final double HEX_RATIO = 752.0/658.0;
	public static final double TILE_RATIO = 430.0/440.0;
	public static final double TILE_RATIO_REVERSE = 430.0/440.0;
	public static final Dimension LABEL_SIZE = new Dimension( 50,50);
	public static final Dimension CONSOLE_SIZE = new Dimension( 300,100);
	public static final Dimension LOADING_SIZE = new Dimension( 350,425);
	public static final Dimension PROGRESS_SIZE = new Dimension( LOADING_SIZE.width,170);
	public static final Dimension HEX_SIZE = new Dimension( (int)(HEX_HEIGHT*HEX_RATIO),HEX_HEIGHT);
	public static final Dimension TILE_SIZE = new Dimension( (int) (TIILE_HEIGHT*TILE_RATIO),TIILE_HEIGHT);
	public static final Dimension TILE_SIZE_BOARD = new Dimension( (int) (TIILE_HEIGHT_BOARD*TILE_RATIO),TIILE_HEIGHT_BOARD);
	public static final Dimension HEX_BOARD_SIZE = new Dimension( HEX_SIZE.height*BOARD_HEIGHT_SEGMENT/2 + HEX_SPACING, HEX_SIZE.height*BOARD_HEIGHT_SEGMENT/2 + HEX_SPACING);
	public static final Dimension BOARD_SIZE = new Dimension( HEX_BOARD_SIZE.width + BOARD_RIGHT_PADDING + PLAYERS_STATE_SIZE, HEX_BOARD_SIZE.height + BOARD_BOTTOM_PADDING);
	
	//Defaults
	public static final int ANIMATION_DELAY = 5;
	public static final int INFINITE_TILE = -1;
	public static final int SERVER_TIMEOUT = 5;
	public static final int SERVER_PORT = 12345;
	public static final boolean PLAYER_READY = true;
	public static final String SERVER_IP = "127.0.0.1";
	public static final String RESOURCE_PATH = "Resources/";
	public static final String GAME_TITLE = "Kings & Things";
	public static final int MOVE_DISTANCE = (int) (HEX_HEIGHT*0.5);
	public static final int BOARD_LOAD_ROW[][] = { { 7, 5, 6, 8, 9, 8, 6},
													{4, 3, 4, 5, 7, 9, 10, 11, 10, 9, 7, 5},
													{3, 2, 1, 2, 3, 4, 6, 8, 10, 11, 12, 13, 12, 11, 10, 8, 6, 4}};
	public static final int BOARD_LOAD_COL[][] = { { 4, 4, 5, 5, 4, 3, 3},
													{3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 2, 2},
													{2, 3, 4, 5, 6, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1}};
	//starting from top right and going clockwise for 4 player game
	public static final int BOARD_POSITIONS[][] = {{5,2}, {5,10}, {1,10}, {1,2}} ;
	
	public static final ImmutableBiMap<String,String> HERO_PAIRINGS = new ImmutableBiMap.Builder<String,String>().put("Assassin_Primus", "Plains_Lord")
																												.put("Swordmaster","Mountain_King")
																												.put("Lord_Of_Eagles","Master_Theif")
																												.put("Jungle_Lord","Arch_Cleric")
																												.put("Baron_Munchausen","Desert_Master")
																												.put("Elf_Lord","Ice_Lord")
																												.put("Arch_Mage","Ghaog_II")
																												.put("Dwarf_King","Warlord")
																												.put("Deerhunter","Grand_Duke")
																												.put("Marksman","Forest_King")
																												.put("Swamp_King","Sir_Lancealot").build();
			
	static{
		int w = (int) (HEX_SIZE.getWidth()/4)+1;
		int h = (int) (HEX_SIZE.getHeight()/2)+2;
		HEX_OUTLINE = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		TILE_OUTLINE = new Rectangle( 0, 0, TILE_SIZE.width, TILE_SIZE.height);
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
	
	public static HashSet<Point> getValidStartingHexes(int playerCount){
		HashSet<Point> startingHexes = new HashSet<Point>();
		if(playerCount==4){
			startingHexes.add(new Point(1,2));
			startingHexes.add(new Point(1,10));
			startingHexes.add(new Point(5,10));
			startingHexes.add(new Point(5,2));
		}else{
			startingHexes.add(new Point(0,2));
			startingHexes.add(new Point(0,6));
			startingHexes.add(new Point(2,0));
			startingHexes.add(new Point(2,8));
			startingHexes.add(new Point(4,2));
			startingHexes.add(new Point(4,6));
		}
		return startingHexes;
	}
}

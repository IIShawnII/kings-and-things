package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.google.common.collect.ImmutableBiMap;

import common.game.ITileProperties;
import common.game.TileProperties;
import common.game.TwoSidedTileProperties;

public final class Constants {
	
	//Primary for bypassing load of images for faster start
	public static final boolean DRAW_LOCKS = false;

	public enum BuildableBuilding {Tower, Keep, Castle, Citadel}
	public enum Building {Castle, Citadel, City, Keep, Tower, Village}
	public enum Level { Error, Warning, Notice, Plain, END, LOADING_DIALOG}
	public enum Ability { Charge, Fly, Range, Special, Magic, Armor, Neutralised}
	public enum Biome { Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp, Sea}
	public enum Category { Resources, Building, Cup, Gold, Hex, Special, State, Misc, END, Creature, Event, Magic, Treasure, Buildable}
	public enum RandomEvent {Big_Juju, Dark_Plague, Defection, Good_Harvest, Mother_Lode, Teenie_Pox, Terrain_Disaster, Vandalism, Weather_Control, Willing_Workers}
	public enum MagicEvent {Balloon, Bow, Dispel_Magic, Dust_Of_Defense, Fan, Firewall, Golem, Lucky_Charm, Elixir, Sword, Talisman}
	public enum UpdateKey {Category, Message, PlayerCount, Players, Name, Port, IP, Player, Hex, Phase, HexState, Roll, Tile, Flipped, Setup, Regular, Combat,
		Special, Rack, Instruction, ThingArray, Gold}
	public enum Restriction { Gold, Magic, Treasure, Building, Event, Special, State, Battle, Sea, Desert, Forest, Frozen_Waste, Jungle, Mountain, Plains, Swamp, Yellow, Red, Green, Gray, None}
	public enum UpdateInstruction {Connect, Disconnect, State, Start, UpdatePlayers, Category, End, Send, PlaceBoard, SetupPhase, RegularPhase, CombatPhase, PlayTreasure,
		NeedRoll, HexOwnership, DieValue, DoneRolling, TieRoll, FlipAll, SeaHexChanged, Skip, GameState, Special, Rejected, InitiateCombat, TargetPlayer, ThingChanged, 
		ApplyHit, Retreat, RemoveThingsFromHex, HexStatesChanged, BribeCreature, ShowExplorationResults, MoveThings, RackChanged, ConstructBuilding, CallBluff, ViewContents,
		GetHeroes, HandChanged, BribeHero, RecruitThings, ExchangeThings}
	
	public enum HexContentsTarget{RETREAT,MOVEMENT,VIEW,REMOVAL}
	
	//Regular turn phases
	public enum RegularPhase {RECRUITING_CHARACTERS, RECRUITING_THINGS, RANDOM_EVENTS, MOVEMENT, COMBAT, CONSTRUCTION, SPECIAL_POWERS}
	//Reasons for dice rolls
	public enum RollReason {DETERMINE_PLAYER_ORDER, EXPLORE_HEX, ATTACK_WITH_CREATURE, CALCULATE_DAMAGE_TO_TILE, ENTERTAINMENT, RECRUIT_SPECIAL_CHARACTER, DEFECTION_USER, DEFECTION_DEFENDER, TERRAIN_DISASTER}
	//Note, phase progression is dependent on the order of the declarations in the following enum type
	public enum SetupPhase {DETERMINE_PLAYER_ORDER, PICK_FIRST_HEX, EXCHANGE_SEA_HEXES, PICK_SECOND_HEX, PICK_THIRD_HEX, PLACE_FREE_TOWER, PLACE_FREE_THINGS, EXCHANGE_THINGS, PLACE_EXCHANGED_THINGS, SETUP_FINISHED}
	//Combat phases
	public enum CombatPhase {DETERMINE_DEFENDERS, BRIBE_CREATURES, SELECT_TARGET_PLAYER, MAGIC_ATTACK, APPLY_MAGIC_HITS, RANGED_ATTACK, APPLY_RANGED_HITS, MELEE_ATTACK, APPLY_MELEE_HITS, ATTACKER_ONE_RETREAT, ATTACKER_TWO_RETREAT, ATTACKER_THREE_RETREAT, DEFENDER_RETREAT, DETERMINE_DAMAGE, PLACE_THINGS, NO_COMBAT}
	//Control permissions for client GUI
	public enum Permissions { Roll, NoMove, MoveMarker, ExchangeThing, ExchangeHex, MoveFromCup, MoveTower, MoveFromRack, ResolveCombat, PlayTreasure, RecruitThings};
	
	//Resources
	public static final Image IMAGE_SKIP;
	public static final Image IMAGE_DICE[];
	public static final Image IMAGE_GREEN;
	public static final Image IMAGE_BACKGROUND;
	public static final Image IMAGE_HEX_REVERSE; 
	public static final Image IMAGE_TILE_REVERSE;
	public static final Image CROSSHAIR;
	public static final Image WHITE_FLAG;
	public static final Image FIGHT_ON;
	public static final Image RUN_AWAY;
	public static final Image PICK_ME_KITTEN;
	public static final HashMap< Integer, Image> IMAGES = new HashMap<>();
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
	public static final int PUBLIC = Integer.MIN_VALUE;
	public static final int BOARD = 0;					//00000000
	public static final int LOGIC = 1;					//00000001
	public static final int PROGRESS = 2;				//00000010
	public static final int LOAD_RESOURCE = 4;			//00000100
	public static final int GUI = 8;					//00001000
	public static final int PLAYER_ID_MULTIPLIER = 2;	//00000010
	public static final int PLAYER_START_ID = 16;		//00010000
	public static final int PLAYER_1_ID = 16;			//00010000
	public static final int PLAYER_2_ID = 32;			//00100000
	public static final int PLAYER_3_ID = 64;			//01000000
	public static final int PLAYER_4_ID = 128;			//10000000
	public static final int ALL_PLAYERS_ID = 240;		//11110000
	
	//Maximums
	public static final int MAX_ROLLS = 7;
	public static final int MAX_HEXES = 48;
	public static final int MAX_PLAYERS = 4;
	public static final int MAX_DICE_FACE = 6;
	public static final int MAX_RACK_SIZE = 10;
	public static final int MAX_MOVE_SPEED = 4;
	public static final int MAX_HEXES_ON_BOARD = 37;
	public static final int MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX = 10;
	
	//Minimums
	public static final int MIN_PLAYERS = 2;
	public static final int MIN_DICE_FACE = 1;
	public static final int MIN_HEXES_ON_BOARD = 19;
	public static final Dimension MIN_CLIENT_SIZE = new Dimension( 1300,720);
	
	//Sizes
	public static final int DICE_SIZE = 70;
	public static final int LOCK_SIZE = 28;
	public static final Polygon HEX_OUTLINE;
	public static final int HEX_HEIGHT = 70;
	public static final int HEX_SPACING = 16;
	public static final int TIILE_HEIGHT = 60;
	public static final Rectangle TILE_OUTLINE;
	public static final int LABEL_FONT_SIZE = 25;
	public static final int IP_COLUMN_COUNT = 12;
	public static final int PLAYER_FONT_SIZE = 12;
	public static final int PORT_COLUMN_COUNT = 7;
	public static final Polygon HEX_OUTLINE_IMAGE;
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
	public static final Random rand = new Random();
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
																												.put("Desert_Master","Baron_Munchausen")
																												.put("Elf_Lord","Ice_Lord")
																												.put("Arch_Mage","Ghaog_II")
																												.put("Dwarf_King","Warlord")
																												.put("Deerhunter","Grand_Duke")
																												.put("Marksman","Forest_King")
																												.put("Swamp_King","Sir_Lancealot").build();

	private static final String RESOURCES_DIRECTORY = "Resources\\";
	private static final String MISC_DIRECTORY = RESOURCES_DIRECTORY + "Misc\\";
	static{
		int w = (int) (HEX_SIZE.getWidth()/4)+1;
		int h = (int) (HEX_SIZE.getHeight()/2)+2;
		HEX_OUTLINE_IMAGE = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		w -= 1;
		h -= 1;
		HEX_OUTLINE = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		TILE_OUTLINE = new Rectangle( 0, 0, TILE_SIZE.width, TILE_SIZE.height);
		IMAGE_BACKGROUND = loadImage( MISC_DIRECTORY + "-n Woodboard.jpg");
		IMAGE_HEX_REVERSE = loadImage( MISC_DIRECTORY + "-n Hex_Reverse.png");
		IMAGE_TILE_REVERSE = loadImage( MISC_DIRECTORY + "-n Tile_Reverse.png");
		IMAGE_GREEN = loadImage( MISC_DIRECTORY + "-n Green_Surface.png");
		IMAGE_SKIP = loadImage( MISC_DIRECTORY + "-n Skip.png");
		CROSSHAIR = loadImage(MISC_DIRECTORY + "-n Crosshair.png");
		WHITE_FLAG = loadImage(MISC_DIRECTORY + "-n White_Flag.jpg");
		FIGHT_ON = loadImage(MISC_DIRECTORY + "-n Fight_On.jpg");
		RUN_AWAY = loadImage(MISC_DIRECTORY + "-n Run_Away.jpg");
		PICK_ME_KITTEN = loadImage(MISC_DIRECTORY + "Pick Me Kitten.jpeg");
		IMAGE_DICE = new Image[7];
		IMAGE_DICE[0] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 0.png");
		IMAGE_DICE[1] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 1.png");
		IMAGE_DICE[2] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 2.png");
		IMAGE_DICE[3] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 3.png");
		IMAGE_DICE[4] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 4.png");
		IMAGE_DICE[5] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 5.png");
		IMAGE_DICE[6] = loadImage( MISC_DIRECTORY + "Dice\\-n Dice -a 6.png");
	}
	
	public static Image getImageForBiome(Biome biome)
	{
		for(ITileProperties hex : HEX.values())
		{
			if(hex.getBiomeRestriction() == biome)
			{
				return IMAGES.get(hex.hashCode());
			}
		}
		
		throw new IllegalArgumentException("Unable to find hex for biome type: " + biome);
	}
	
	public static Image getImageForTile(ITileProperties tile)
	{
		if(tile.isBuilding())
		{
			for(ITileProperties b : Constants.BUILDING.values())
			{
				if(b.getName().equals(tile.getName()) && Arrays.equals(b.getAbilities(), tile.getAbilities()))
				{
					return Constants.IMAGES.get(b.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT);
				}
			}
		}
		else
		{
			if(tile.isSpecialCharacter())
			{
				TwoSidedTileProperties hero = (TwoSidedTileProperties) tile;
				return Constants.IMAGES.get(hero.getFaceUpHashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT);
			}
			if(tile.isFaceUp() || !tile.isCreature())
			{
				return Constants.IMAGES.get(tile.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT);
			}
			else
			{
				return IMAGE_TILE_REVERSE;
			}
		}
		
		return null;
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

	@SuppressWarnings("unchecked")
	public static <T extends ITileProperties, C extends Collection<T>, R extends Collection<T>> R deepCloneCollection(C tiles, R out)
	{
		for(T tp : tiles)
		{
			out.add((T)tp.clone());
		}
		return out;
	}
	

	
	/**
	 * get a specific marker according to the player ID,
	 * currently in order  of player 1 to 4, colors go as Red, Yellow, Green, Gray.
	 * ID PUBLIC is special for getting the battle tile.
	 * @param ID - player ID number
	 * @return ITileProperties corresponding to the ID
	 */
	public final static ITileProperties getPlayerMarker( final int ID){
		switch( ID){
			case PUBLIC: return STATE.get( Restriction.Battle);
			case PLAYER_1_ID: return STATE.get( Restriction.Red);
			case PLAYER_2_ID: return STATE.get( Restriction.Yellow);
			case PLAYER_3_ID: return STATE.get( Restriction.Green);
			case PLAYER_4_ID: return STATE.get( Restriction.Gray);
			default:
				throw new IllegalArgumentException("ERROR - invalid ID for marker");
		}
	}

	public static List< Integer> convertToDice( int total, final int count){
		if( count<=0 || total<=0){
			throw new IllegalArgumentException( "ERROR - arguments must be positive and non-zero");
		}
		if( total < MIN_DICE_FACE*count || total > MAX_DICE_FACE*count){
			throw new IllegalArgumentException( "Error - when count is " + count + ", total(" + total + ") must be between " + MIN_DICE_FACE*count + " and " + MAX_DICE_FACE*count);
		}
		TotalDiceTree tree = new TotalDiceTree();
		tree.generate( total, count, MIN_DICE_FACE, MAX_DICE_FACE);
		return tree.getRandomCombination();
	}
	
	public static int roll(){
		return random( MIN_DICE_FACE, MAX_DICE_FACE);
	}
	
	/**
	 * Generate a random integer between min(inclusive) and max(inclusive)
	 * @param min - smallest possible number (inclusive)
	 * @param max - largest possible number (inclusive)
	 */
	public static int random( int min, int max){
		return rand.nextInt((max+1)-min)+min;
	}
	
	/**
	 * Generate a random integer between 0(inclusive) and max(inclusive)
	 * @param max - largest possible number (inclusive)
	 */
	public static int random( int max){
		return rand.nextInt((max+1));
	}
	

	private static final String DIRECTION_DIRECTORY = MISC_DIRECTORY + "Directional Arrows\\";
	
	public static enum Direction{
		NORTH(DIRECTION_DIRECTORY + "N.png"),
		NORTH_EAST(DIRECTION_DIRECTORY + "NE.png"),
		EAST(DIRECTION_DIRECTORY + "E.png"),
		SOUTH_EAST(DIRECTION_DIRECTORY + "SE.png"),
		SOUTH(DIRECTION_DIRECTORY + "S.png"),
		SOUTH_WEST(DIRECTION_DIRECTORY + "SW.png"),
		WEST(DIRECTION_DIRECTORY + "W.png"),
		NORTH_WEST(DIRECTION_DIRECTORY + "NW.png");
		
		private final Image image;
		
		private Direction(String fileName)
		{
			image = loadImage(fileName);
		}
		
		public Image getImage()
		{
			return image;
		}
		
		public static Direction getFromAdjacentPoints(Point origin, Point destination)
		{
			if(origin.x < destination.x)
			{
				if(origin.y < destination.y)
				{
					return SOUTH_EAST;
				}
				else if(origin.y > destination.y)
				{
					return NORTH_EAST;
				}
				else
				{
					return EAST;
				}
			}
			else if(origin.x > destination.x)
			{
				if(origin.y < destination.y)
				{
					return SOUTH_WEST;
				}
				else if(origin.y > destination.y)
				{
					return NORTH_WEST;
				}
				else
				{
					return WEST;
				}
			}
			else
			{
				if(origin.y < destination.y)
				{
					return SOUTH;
				}
				else if(origin.y > destination.y)
				{
					return NORTH;
				}
				else
				{
					return null;
				}
			}
		}
	}
	
	public static String getTerrainLordNameForBiome(Biome b)
	{
		switch(b)
		{
			case Desert:
			{
				return "Desert_Master";
			}
			case Forest:
			{
				return "Forest_King";
			}
			case Frozen_Waste:
			{
				return "Ice_Lord";
			}
			case Jungle:
			{
				return "Jungle_Lord";
			}
			case Mountain:
			{
				return "Mountain_King";
			}
			case Plains:
			{
				return "Plains_Lord";
			}
			case Swamp:
			{
				return "Swamp_King";
			}
			default:
			{
				return "";
			}
		}
	}
}

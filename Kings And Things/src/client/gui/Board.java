package client.gui;

import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_RIGHT_PADDING;
import static common.Constants.BOARD_SIZE;
import static common.Constants.DICE_SIZE;
import static common.Constants.HEX_BOARD_SIZE;
import static common.Constants.HEX_OUTLINE_IMAGE;
import static common.Constants.HEX_SIZE;
import static common.Constants.PLAYERS_STATE_PADDING;
import static common.Constants.TILE_OUTLINE;
import static common.Constants.TILE_SIZE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.logic.Controller;
import client.gui.die.DiceRoller;
import client.gui.tiles.Hex;
import client.gui.tiles.Tile;
import client.gui.util.LockManager;
import client.gui.util.LockManager.Lock;
import client.gui.util.animation.CanvasParent;
import client.gui.util.animation.FlipAll;
import client.gui.util.animation.MoveAnimation;
import common.Constants;
import common.Constants.Ability;
import common.Constants.Category;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.PlayerInfo;
import common.game.TileProperties;

@SuppressWarnings("serial")
public class Board extends JPanel implements CanvasParent{
	
	private static final BufferedImage IMAGE;
	public static final int HEIGHT_SEGMENT = (int) ((HEX_BOARD_SIZE.getHeight())/Constants.BOARD_HEIGHT_SEGMENT);
	public static final int WIDTH_SEGMENT = (int) ((HEX_BOARD_SIZE.getWidth())/Constants.BOARD_WIDTH_SEGMENT);
	//used for placing bank outlines
	public static final int INITIAL_TILE_X_SHIFT = WIDTH_SEGMENT/2;
	public static final int TILE_X_SHIFT = (int) (WIDTH_SEGMENT*1.2);
	public static final int TILE_Y_SHIFT = 13;
	private static final int HEX_Y_SHIFT = 8-3;
	private static final int HEX_X_SHIFT = 8-2;
	public static final int PADDING = 10;
	public static final Font STATUS_INDICATOR_FONT = new Font("default", Font.BOLD, 28);
	
	/**
	 * create a static image with background and all outlines for faster drawing in Game 
	 */
	static{
		//create image for outlines on board
		IMAGE = new BufferedImage( BOARD_SIZE.width, BOARD_SIZE.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = IMAGE.createGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//draw background on the new image
		g2d.drawImage( Constants.IMAGE_BACKGROUND, 0, 0, BOARD_SIZE.width, BOARD_SIZE.height, null);
		int x=0, y=0;
		//create a thicker stroke
		g2d.setStroke( new BasicStroke( 5));
		g2d.setColor( Color.BLACK);
		//position the outline for hex
		HEX_OUTLINE_IMAGE.translate( HEX_X_SHIFT, HEX_Y_SHIFT);
		g2d.drawPolygon( HEX_OUTLINE_IMAGE);
		HEX_OUTLINE_IMAGE.translate( -HEX_X_SHIFT, -HEX_Y_SHIFT);
		//draw hex board
		for( int ring=0; ring<BOARD_LOAD_ROW.length; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length; count++){
				x = (WIDTH_SEGMENT*Constants.BOARD_LOAD_COL[ring][count]);
				y = (HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+Constants.BOARD_TOP_PADDING;
				HEX_OUTLINE_IMAGE.translate( ((int) (x-HEX_SIZE.getWidth()/2))-2, ((int) (y-HEX_SIZE.getHeight()/2)-3));
				g2d.drawPolygon( HEX_OUTLINE_IMAGE);
				HEX_OUTLINE_IMAGE.translate( -((int) (x-HEX_SIZE.getWidth()/2)-2), -((int) (y-HEX_SIZE.getHeight()/2)-3));
			}
		}
		//draw bank tiles
		TILE_OUTLINE.translate( INITIAL_TILE_X_SHIFT, TILE_Y_SHIFT);
		for( int i=0; i<4; i++){
			TILE_OUTLINE.translate( TILE_X_SHIFT, 0);
			g2d.draw( TILE_OUTLINE);
		}
		//draw rack tiles
		TILE_OUTLINE.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-TILE_OUTLINE.height-PADDING);
		for( int i=0; i<Constants.MAX_RACK_SIZE; i++){
			if(i==5){
				TILE_OUTLINE.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(2*PADDING));
			}
			TILE_OUTLINE.translate( -TILE_X_SHIFT, 0);
			g2d.draw( TILE_OUTLINE);
		}
		TILE_OUTLINE.setLocation( 0, 0);
		g2d.dispose();
	}
	private final boolean demo;
	private volatile boolean isActive = false;
	private volatile boolean phaseDone = false;

	private JButton jbSkip;
	private DiceRoller dice;
	private LockManager locks;
	private JTextField jtfStatus;
	private Controller controller;
	private MoveAnimation moveAnimation;
	private ITileProperties playerMarker;
	private PlayerInfo players[], currentPlayer;
	
	/**
	 * basic super constructor warper for JPanel
	 */
	public Board( boolean demo, PlayerInfo player){
		super( null, true);
		this.demo = demo;
		setCurrentPlayer( player);
	}

	public void setActive( boolean active) {
		this.isActive = active;
	}
	
	/**
	 * create LockManager and mouse listeners with specific player count
	 * @param playerCount - number of players to be playing on this board
	 */
	public void init( int playerCount){
		moveAnimation = new MoveAnimation( this);
		locks = new LockManager( playerCount);
		controller = new Controller( this, demo, locks, currentPlayer.getID());
		addMouseListener( controller);
		addMouseWheelListener( controller);
		addMouseMotionListener( controller);

		dice = new DiceRoller();
		dice.setBounds( HEX_BOARD_SIZE.width+DICE_SIZE, getHeight()-(DICE_SIZE*2)-10, DICE_SIZE, DICE_SIZE);
		dice.init();
		dice.addMouseListener( controller);
		add( dice);
		
		jtfStatus = new JTextField("Welcome to Kings & Things");
		jtfStatus.setBounds( 10, getHeight()-40, HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING, 35);
		jtfStatus.setEditable( false);
		jtfStatus.setOpaque( false);
		jtfStatus.setBorder( null);
		jtfStatus.setFont( STATUS_INDICATOR_FONT);
		add(jtfStatus);
		
		jbSkip = new JButton( new ImageIcon( Constants.IMAGE_SKIP));
		jbSkip.setContentAreaFilled(false);
		jbSkip.setBorderPainted(false);
		jbSkip.setOpaque( false);
		jbSkip.addActionListener( controller);
		jbSkip.setToolTipText( "Skip this phase");
		jbSkip.setBounds( HEX_BOARD_SIZE.width+DICE_SIZE, getHeight()-DICE_SIZE-10, DICE_SIZE, DICE_SIZE);
		add(jbSkip);
		Rectangle bound = new Rectangle( INITIAL_TILE_X_SHIFT, TILE_Y_SHIFT, TILE_SIZE.width, TILE_SIZE.height);
		bound.translate( TILE_X_SHIFT, 0);
		//Tower
		bound.translate( TILE_X_SHIFT, 0);
		//Marker
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Gold)), bound, true);
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Cup)), bound, true);
	}
	
	public void setPlayers( PlayerInfo[] players){
		this.players = players;
	}
	
	public void setCurrentPlayer( PlayerInfo player){
		currentPlayer = player;
		playerMarker = Constants.getPlayerMarker( currentPlayer.getID());
	}
	
	public boolean matchPlayer( final int ID){
		return currentPlayer.getID()==ID;
	}
	
	/**
	 * add a tile to the board
	 * @param tile - tile to be added, must not be null
	 * @param bound - bounds to be used in placing the tile, must nut be null
	 * @param lock - if true this tile is fake and cannot be animated, and uses a Permanent Lock
	 * @return fully created tile that was added to board
	 */
	public Tile addTile( Tile tile, Rectangle bound, boolean lock){
		tile.init();
		tile.setBounds( bound);
		tile.setLockArea( locks.getPermanentLock( tile));
		tile.setCanAnimate( !lock);
		add(tile,0);
		return tile;
	}
	
	/**
	 * paint the background with already drawn outlines.
	 * paint players information
	 * paint locks if Constants.DRAW_LOCKS is true
	 */
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage( IMAGE, 0, 0, getWidth(), getHeight(), null);
		g2d.setFont( STATUS_INDICATOR_FONT);
		if( players!=null && currentPlayer!=null){
			for( int i=0, y=PLAYERS_STATE_PADDING; i<players.length; i++, y+=PLAYERS_STATE_PADDING){
				if( players[i].getID()!=currentPlayer.getID()){
					g2d.drawString( (players[i].isActive()?"*":"")+players[i].getName(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING, y);
					g2d.drawString( "Gold: " + players[i].getGold(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING+165, y);
					g2d.drawString( "Rack: " + players[i].getCradsOnRack(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING+355, y);
				}else{
					y-=PLAYERS_STATE_PADDING;
				}
			}
			g2d.drawString( (currentPlayer.isActive()?"*":"")+currentPlayer.getName(), HEX_BOARD_SIZE.width+160, BOARD_SIZE.height-TILE_OUTLINE.height*2-PADDING*4);
			g2d.drawString( "Gold: " + currentPlayer.getGold(), HEX_BOARD_SIZE.width+360, BOARD_SIZE.height-TILE_OUTLINE.height*2-PADDING*4);
		}
		if( Constants.DRAW_LOCKS){
			locks.draw( g2d);
		}
	}


	/**
	 * add new hexes to bank lock to be send to board, max of 37
	 * this placement uses the predetermined order stored in 
	 * arrays Constants.BOARD_LOAD_ROW and Constants.BOARD_LOAD_COL
	 * @param hexes - list of hexStates to be used in placing Hexes, if null fakes will be created
	 * @return array of tiles in order they were created
	 */
	private Tile[] setupHexesForPlacement( HexState[] hexes) {
		int x, y, hexCount = hexes.length;
		Tile[] list = new Tile[hexCount];
		for(int ring=0, drawIndex=0; ring<BOARD_LOAD_ROW.length&&drawIndex<hexCount; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length&&drawIndex<hexCount; count++, drawIndex++){
				list[drawIndex] = addTile( new Hex( hexes[drawIndex]), new Rectangle( 8,8,HEX_SIZE.width, HEX_SIZE.height), false);
				x = (WIDTH_SEGMENT*Constants.BOARD_LOAD_COL[ring][count]);
				y = (HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+Constants.BOARD_TOP_PADDING;
				list[drawIndex].setDestination( x, y);
			}
		}
		return list;
	}

	/**
	 * add new tiles to cup lock to be send to player rack, max of 10
	 * @param prop - list of tiles to be placed, if null fakes will be created
	 * @return array of tiles in order they were created
	 */
	private Tile[] setupTilesForRack( ITileProperties[] prop) {
		Tile[] list = new Tile[ prop.length];
		Lock lock = locks.getPermanentLock( Category.Cup);
		Point center = lock.getCenter();
		//create bound for starting position of tile
		Rectangle start = new Rectangle( center.x-TILE_SIZE.width/2, center.y-TILE_SIZE.height/2, TILE_SIZE.width, TILE_SIZE.height);
		addTile( new Tile( new TileProperties( Category.Cup)), start, true);
		//create bound for destination location, this bound starts from outside of board
		Rectangle bound = new Rectangle( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-TILE_SIZE.height-PADDING, TILE_SIZE.width, TILE_SIZE.height);
		for( int count=0; count<prop.length; count++){
			list[count] = addTile( new Tile( prop==null || count>=prop.length?new TileProperties(Category.Cup):prop[count]), start, false);
			if( count==5){
				// since rack is two rows of five, at half all bounds must be shifted up, this bound starts from outside of board
				bound.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(PADDING*2));
			}
			bound.translate( -TILE_X_SHIFT, 0);
			//set final destination for tile to be animated later
			list[count].setDestination( bound.x+TILE_SIZE.width/2, bound.y+TILE_SIZE.height/2);
			list[count].flip();
		}
		return list;
	}
	
	private void noneAnimatedPlacement( Tile[] tiles){
		Point end;
		Dimension size;
		for( Tile tile : tiles){
			if( tile==null){
				continue;
			}
			size = tile.getSize();
			end = tile.getDestination();
			tile.setLocation( end.x-size.width/2, end.y-size.height/2);
			tile.setLockArea( locks.getLock( tile));
			placeTileOnHex( tile);
		}
		revalidate();
		repaint();
	}
	
	private void noneAnimtedFlipAll() {
		for( Component comp : getComponents()){
			if( comp instanceof Hex){
				((Tile)comp).flip();
			}
		}
	}
	
	/**
	 * animate placement of hex tiles
	 * @param hexes - list of HexState to populate the hexes on board
	 */
	public void animateHexPlacement( HexState[] hexes){
		addTile( new Hex( new HexState()), new Rectangle(8,8,HEX_SIZE.width,HEX_SIZE.height), true);
		if( !isActive){
			noneAnimatedPlacement( setupHexesForPlacement( hexes));
			phaseDone = true;
		}else{
			moveAnimation.start( setupHexesForPlacement( hexes));
		}
	}
	
	/**
	 * animate placement of rack tiles
	 */
	public void animateRackPlacement( ITileProperties[] rack){
		if( !isActive){
			noneAnimatedPlacement( setupTilesForRack( rack));
			phaseDone = true;
		}else{
			moveAnimation.start( setupTilesForRack( rack));
		}
	}
	
	/**
	 * Flip all hexes
	 */
	public void FlipAllHexes(){
		if( !isActive){
			noneAnimtedFlipAll();
			phaseDone = true;
		}else{
			new FlipAll( getComponents(), this).start();
		}
	}
	
	private boolean firstMarker = true;
	public void placeMarkers(){
		Point point = locks.getPermanentLock( Category.State).getCenter();
		Rectangle bound = new Rectangle( point.x-TILE_SIZE.width/2, point.y-TILE_SIZE.height/2,TILE_SIZE.width,TILE_SIZE.height);
		if( firstMarker){
			addTile( new Tile( playerMarker), bound, false).flip();
			addTile( new Tile( playerMarker), bound, false).flip();
			addTile( new Tile( playerMarker), bound, false).flip();
			addTile( new Tile( playerMarker), bound, false).flip();
			firstMarker = false;
		}
		addTile( new Tile( playerMarker), bound, false).flip();
	}
	
	private boolean firstTower = true;
	private ITileProperties tower = null;
	public void placeTower(){
		Point point = locks.getPermanentLock( Category.Buildable).getCenter();
		Rectangle bound = new Rectangle( point.x-TILE_SIZE.width/2, point.y-TILE_SIZE.height/2,TILE_SIZE.width,TILE_SIZE.height);
		if( firstTower){
			for( TileProperties tile : Constants.BUILDING.values()){
				if( tile.getName().equals("Tower") && !tile.hasAbility( Ability.Neutralised)){
					tower = tile;
				}
			}
			addTile( new Tile( tower), bound, false).flip();
			addTile( new Tile( tower), bound, false).flip();
			addTile( new Tile( tower), bound, false).flip();
			addTile( new Tile( tower), bound, false).flip();
			firstTower = false;
		}
		addTile( new Tile( tower), bound, false).flip();
	}

	/**
	 * place any tile on hex, however only battle and markers will be drawn
	 * current Tile will be removed and added as TileProperties to the Hex
	 * @param tile - thing to be placed
	 */
	@Override
	public HexState placeTileOnHex( Tile tile) {
		if( tile.isTile() && tile.hasLock() && tile.getLock().canHold( tile) ){
			return tile.getLock().getHex().getState().addThingToHexGUI( tile.getProperties());
		}
		return null;
	}
	
	public void setStatusMessage( String message){
		jtfStatus.setText(message);
	}

	@Override
	public void phaseDone() {
		phaseDone = true;
	}

	@Override
	public void phaseStarted() {
		phaseDone = false;
	}

	@Override
	public void repaintCanvas() {
		repaint();
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public Lock getLock(Tile tile) {
		return locks.getLock(tile);
	}
	
	public MoveAnimation getAnimator(){
		return moveAnimation;
	}
	
	public boolean isPhaseDone(){
		return phaseDone;
	}
	
	public DiceRoller getDice(){
		return dice;
	}

	public PlayerInfo[] getPlayers() {
		return players;
	}
}

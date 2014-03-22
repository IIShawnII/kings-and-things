package client.gui;

import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import client.gui.tiles.Hex;
import client.gui.tiles.Tile;
import client.gui.LockManager.Lock;
import common.Constants.Category;
import common.Constants.UpdateKey;
import common.Constants.SetupPhase;
import common.Constants.Restriction;
import common.Constants.RegularPhase;
import common.event.UpdatePackage;
import common.event.AbstractUpdateReceiver;
import common.event.network.HexOwnershipChanged;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.TileProperties;
import common.game.ITileProperties;
import static common.Constants.STATE;
import static common.Constants.BOARD;
import static common.Constants.HEX_SIZE;
import static common.Constants.TILE_SIZE;
import static common.Constants.DRAW_LOCKS;
import static common.Constants.BOARD_SIZE;
import static common.Constants.HEX_OUTLINE;
import static common.Constants.TILE_OUTLINE;
import static common.Constants.MOVE_DISTANCE;
import static common.Constants.MAX_RACK_SIZE;
import static common.Constants.HEX_BOARD_SIZE;
import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_LOAD_COL;
import static common.Constants.ANIMATION_DELAY;
import static common.Constants.BOARD_POSITIONS;
import static common.Constants.IMAGE_BACKGROUND;
import static common.Constants.BOARD_TOP_PADDING;
import static common.Constants.BYPASS_MOUSE_CLICK;
import static common.Constants.MAX_HEXES_ON_BOARD;
import static common.Constants.BOARD_WIDTH_SEGMENT;
import static common.Constants.BOARD_RIGHT_PADDING;
import static common.Constants.BOARD_HEIGHT_SEGMENT;
import static common.Constants.PLAYERS_STATE_PADDING;

@SuppressWarnings("serial")
public class Board extends JPanel{
	
	private static final BufferedImage IMAGE;
	static final int HEIGHT_SEGMENT = (int) ((HEX_BOARD_SIZE.getHeight())/BOARD_HEIGHT_SEGMENT);
	static final int WIDTH_SEGMENT = (int) ((HEX_BOARD_SIZE.getWidth())/BOARD_WIDTH_SEGMENT);
	//used for placing bank outlines
	static final int INITIAL_TILE_X_SHIFT = WIDTH_SEGMENT/2;
	static final int TILE_X_SHIFT = (int) (WIDTH_SEGMENT*1.2);
	static final int TILE_Y_SHIFT = 13;
	private static final int HEX_Y_SHIFT = 8-3;
	private static final int HEX_X_SHIFT = 8-2;
	static final int PADDING = 10;
	
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
		g2d.drawImage( IMAGE_BACKGROUND, 0, 0, BOARD_SIZE.width, BOARD_SIZE.height, null);
		int x=0, y=0;
		//create a thicker stroke
		g2d.setStroke( new BasicStroke( 5));
		g2d.setColor( Color.BLACK);
		//position the outline for hex
		HEX_OUTLINE.translate( HEX_X_SHIFT, HEX_Y_SHIFT);
		g2d.drawPolygon( HEX_OUTLINE);
		HEX_OUTLINE.translate( -HEX_X_SHIFT, -HEX_Y_SHIFT);
		//draw hex board
		for( int ring=0; ring<BOARD_LOAD_ROW.length; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length; count++){
				x = (WIDTH_SEGMENT*BOARD_LOAD_COL[ring][count]);
				y = (HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
				HEX_OUTLINE.translate( ((int) (x-HEX_SIZE.getWidth()/2))-2, ((int) (y-HEX_SIZE.getHeight()/2)-3));
				g2d.drawPolygon( HEX_OUTLINE);
				HEX_OUTLINE.translate( -((int) (x-HEX_SIZE.getWidth()/2)-2), -((int) (y-HEX_SIZE.getHeight()/2)-3));
			}
		}
		//draw bank tiles
		TILE_OUTLINE.translate( INITIAL_TILE_X_SHIFT, TILE_Y_SHIFT);
		for( int i=0; i<5; i++){
			TILE_OUTLINE.translate( TILE_X_SHIFT, 0);
			g2d.draw( TILE_OUTLINE);
		}
		//draw rack tiles
		TILE_OUTLINE.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-TILE_OUTLINE.height-PADDING);
		for( int i=0; i<MAX_RACK_SIZE; i++){
			if(i==5){
				TILE_OUTLINE.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(2*PADDING));
			}
			TILE_OUTLINE.translate( -TILE_X_SHIFT, 0);
			g2d.draw( TILE_OUTLINE);
		}
		TILE_OUTLINE.setLocation( 0, 0);
		g2d.dispose();
	}
	
	private volatile boolean phaseDone = false;
	private volatile boolean isActive = false;

	private LockManager locks;
	private JTextField jtfStatus;
	private MouseInput mouseInput;
	private ITileProperties playerMarker;
	private PlayerInfo players[], currentPlayer;
	private Font font = new Font("default", Font.BOLD, 30);
	
	/**
	 * basic super constructor warper for JPanel
	 * @param layout
	 */
	public Board( LayoutManager layout){
		super( layout, true);
	}

	public void setActive( boolean active) {
		this.isActive = active;
	}
	
	/**
	 * create LockManager and mouse listeners with specific player count
	 * @param playerCount - number of players to be playing on this board
	 */
	protected void init( int playerCount){
		mouseInput = new MouseInput();
		addMouseListener( mouseInput);
		addMouseWheelListener( mouseInput);
		addMouseMotionListener( mouseInput);
		locks = new LockManager( playerCount);
		jtfStatus = new JTextField("Welcome to Kings & Things");
		jtfStatus.setBounds( 10, getHeight()-40, HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING, 35);
		jtfStatus.setEditable( false);
		jtfStatus.setOpaque( false);
		jtfStatus.setBorder( null);
		jtfStatus.setFont( font);
		add(jtfStatus);
		/*Rectangle bound = new Rectangle( INITIAL_TILE_X_SHIFT, TILE_Y_SHIFT, TILE_SIZE.width, TILE_SIZE.height);
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Buildable)), bound, true);
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Gold)), bound, true);
		bound.translate( TILE_X_SHIFT, 0);
		
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Special)), bound, true);
		bound.translate( TILE_X_SHIFT, 0);
		addTile( new Tile( new TileProperties( Category.Cup)), bound, true);*/
		new UpdateReceiver();
	}
	
	public void setCurrentPlayer( PlayerInfo player){
		currentPlayer = player;
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
		if( lock){
			tile.setLockArea( locks.getPermanentLock( tile));
			tile.setCanAnimate( false);
		}else{
			tile.setCanAnimate( true);
		}
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
		g2d.setFont( font);
		if( players!=null && currentPlayer!=null){
			for( int i=0, y=PLAYERS_STATE_PADDING; i<players.length; i++, y+=PLAYERS_STATE_PADDING){
				if( players[i].getID()!=currentPlayer.getID()){
					g2d.drawString( players[i].getName(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING, y);
					g2d.drawString( "Gold: " + players[i].getGold(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING+165, y);
					g2d.drawString( "Rack: " + players[i].getCradsOnRack(), HEX_BOARD_SIZE.width+BOARD_RIGHT_PADDING+355, y);
				}else{
					y-=PLAYERS_STATE_PADDING;
				}
			}
			g2d.drawString( currentPlayer.getName(), HEX_BOARD_SIZE.width+160, BOARD_SIZE.height-TILE_OUTLINE.height*2-PADDING*4);
			g2d.drawString( "Gold: " + currentPlayer.getGold(), HEX_BOARD_SIZE.width+360, BOARD_SIZE.height-TILE_OUTLINE.height*2-PADDING*4);
		}
		if( DRAW_LOCKS){
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
		Tile tile = null;
		int x, y, hexCount = hexes==null?MAX_HEXES_ON_BOARD:hexes.length;
		Tile[] list = new Tile[hexCount];
		for(int ring=0, drawIndex=0; ring<BOARD_LOAD_ROW.length&&drawIndex<hexCount; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length&&drawIndex<hexCount; count++, drawIndex++){
				tile = addTile( new Hex( hexes==null?new HexState():hexes[drawIndex]), new Rectangle( 8,8,HEX_SIZE.width, HEX_SIZE.height), false);
				x = (WIDTH_SEGMENT*BOARD_LOAD_COL[ring][count]);
				y = (HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
				tile.setDestination( x, y);
				list[drawIndex] = tile;
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
		Tile tile = null;
		Tile[] list = new Tile[MAX_RACK_SIZE];
		Lock lock = locks.getPermanentLock( Category.Cup);
		Point center = lock.getCenter();
		//create bound for starting position of tile
		Rectangle start = new Rectangle( center.x-TILE_SIZE.width/2, center.y-TILE_SIZE.height/2, TILE_SIZE.width, TILE_SIZE.height);
		addTile( new Tile( new TileProperties( Category.Cup)), start, true);
		//create bound for destination location, this bound starts from outside of board
		Rectangle bound = new Rectangle( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-TILE_SIZE.height-PADDING, TILE_SIZE.width, TILE_SIZE.height);
		for( int count=0; count<MAX_RACK_SIZE; count++){
			tile = addTile( new Tile( prop==null?new TileProperties(Category.Cup):prop[count]), start, false);
			if( count==5){
				// since rack is two rows of five, at half all bounds must be shifted up, this bound starts from outside of board
				bound.setLocation( BOARD_SIZE.width-PADDING, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(PADDING*2));
			}
			bound.translate( -TILE_X_SHIFT, 0);
			//set final destination for tile to be animated later
			tile.setDestination( bound.x+TILE_SIZE.width/2, bound.y+TILE_SIZE.height/2);
			list[count] = tile;
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
			placeTileOnHex( tile, null, true);
		}
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
	private void animateHexPlacement( HexState[] hexes){
		addTile( new Hex( new HexState()), new Rectangle(8,8,HEX_SIZE.width,HEX_SIZE.height), true);
		if( !isActive){
			noneAnimatedPlacement( setupHexesForPlacement( hexes));
			phaseDone = true;
		}else{
			MoveAnimation animation = new MoveAnimation( setupHexesForPlacement( hexes));
	        animation.start();
		}
	}
	
	/**
	 * animate placement of rack tiles
	 */
	private void animateRackPlacement(){
		if( !isActive){
			noneAnimatedPlacement( setupTilesForRack( null));
			phaseDone = true;
		}else{
			MoveAnimation animation = new MoveAnimation( setupTilesForRack( null));
			animation.start();
		}
	}
	
	/**
	 * Flip all hexes
	 */
	private void FlipAllHexes(){
		if( !isActive){
			noneAnimtedFlipAll();
			phaseDone = true;
		}else{
			FlipAll flip = new FlipAll( getComponents());
			flip.start();
		}
	}

	/**
	 * place markers on the board, if no order is provided a demo setup will be placed
	 * @param order - list of players id in order 
	 */
	private void placeMarkers(int[] order){
		if( order!=null){
			for( int i=0; i<order.length; i++){
				if( currentPlayer.getID()==order[i]){
					playerMarker = getPlayerMarker( i);
					break;
				}
			}
		}else{
			playerMarker = getPlayerMarker( -1);
			order = new int[4];
		}
		Point point = locks.getPermanentLock( Category.State).getCenter();
		Rectangle bound = new Rectangle( point.x-TILE_SIZE.width/2, point.y-TILE_SIZE.height/2,TILE_SIZE.width,TILE_SIZE.height);
		Tile tile = addTile( new Tile( playerMarker), bound, true);
		tile.flip();
		Tile[] tiles = new Tile[order.length];
		for( int i=0; i<BOARD_POSITIONS.length && i<tiles.length; i++){
			tiles[i] = addTile( new Tile( getPlayerMarker( i)), bound, false);
			tiles[i].flip();
			tiles[i].setDestination( locks.convertToCenterCoordinate( BOARD_POSITIONS[i][0], BOARD_POSITIONS[i][1]));
			new HexOwnershipChanged( tiles[i].getLock().getHex().getState());
		}
		MoveAnimation animation = new MoveAnimation( tiles);
		animation.start();
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, BOARD, Board.this);
		}

		@Override
		protected void handlePrivate( UpdatePackage update) {
			updateBoard( update);
		}

		@Override
		protected boolean verifyPrivate( UpdatePackage update) {
			return update.isValidID(ID|currentPlayer.getID());
		}
	}
	
	/**
	 * update the board with new information, such as
	 * hex placement, flip all, player order and rack info.
	 * @param update - event wrapper containing update information
	 */
	public void updateBoard( UpdatePackage update){
		switch( update.peekFirstInstruction()){
			case UpdatePlayers:
				currentPlayer = (PlayerInfo) update.getData( UpdateKey.CurrentPlayer);
				players = (PlayerInfo[]) update.getData( UpdateKey.Players);
				repaint();
				phaseDone = true;
				break;
			case PlaceBoard:
				animateHexPlacement( (HexState[]) update.getData( UpdateKey.Hex));
				break;
			default:
		}
		
		/*if( update.flipAll()){
			FlipAllHexes();
		}else if( update.isPlayerOder()){
			placeMarkers( update.getPlayerOrder());
		}else if( update.isRack()){
			animateRackPlacement();
		}else if( update.isSetupPhase()){
			manageSetupPhase( update.getSetup());
		}else if( update.isRegularPhase()){
			manageRegularPhase( update.getRegular());
		}*/
		while( !phaseDone){
			try {
				Thread.sleep( 100);
			} catch ( InterruptedException e) {}
		}
	}
	
	private void manageRegularPhase( RegularPhase phase) {
		switch( phase){
			case COMBAT:
				break;
			case CONSTRUCTION:
				break;
			case MOVEMENT:
				break;
			case RANDOM_EVENTS:
				break;
			case RECRUITING_CHARACTERS:
				break;
			case RECRUITING_THINGS:
				break;
			case SPECIAL_POWERS:
				break;
			default:
				break;
		}
	}

	private void manageSetupPhase( SetupPhase phase){
		switch( phase){
			case DETERMINE_PLAYER_ORDER:
				break;
			case EXCHANGE_SEA_HEXES:
				break;
			case EXCHANGE_THINGS:
				break;
			case PICK_FIRST_HEX:
				jtfStatus.setText( "Pick your first Hex"); mouseInput.ignore = false;
				//TODO must be implemented when dice roll is implemented
				break;
			case PICK_SECOND_HEX:
				jtfStatus.setText( "Pick your second Hex"); mouseInput.ignore = false;
				break;
			case PICK_THIRD_HEX:
				jtfStatus.setText( "Pick your third Hex"); mouseInput.ignore = false;
				break;
			case PLACE_EXCHANGED_THINGS:
				break;
			case PLACE_FREE_THINGS:
				break;
			case PLACE_FREE_TOWER:
				break;
			case SETUP_FINISHED:
				break;
			default:
				break;
		}
		
	}
	
	/**
	 * get a specific marker according to the player order,
	 * currently in order 1 to 4, colors go as Yellow, Gray, Green and Red
	 * order -1 is special for getting the battle tile.
	 * @param order - player order number
	 * @return ITileProperties corresponding to the order
	 */
	private ITileProperties getPlayerMarker( int order){
		switch( order){
			case -1: return STATE.get( Restriction.Battle);
			case 0: return STATE.get( Restriction.Yellow);
			case 1: return STATE.get( Restriction.Gray);
			case 2: return STATE.get( Restriction.Green);
			case 3: return STATE.get( Restriction.Red);
			default:
				throw new IllegalArgumentException("ERROR - invalid player name for marker");
		}
	}

	/**
	 * place any tile on hex, however only battle and markers will be drawn
	 * current Tile will be removed and added as TileProperties to the Hex
	 * @param tile - thing to be placed
	 */
	private boolean placeTileOnHex( Tile tile, HexState state, boolean ignoreMarker) {
		if( tile.isTile() && tile.hasLock() && tile.getLock().canHold( tile) && (ignoreMarker || (tile.getLock().getHex().getState().hasMarker()))){
			remove(tile);
			revalidate();
			return tile.getLock().getHex().getState().addThingToHex( tile.getProperties());
		}
		return false;
	}
	
	/**
	 * input class for mouse, used for like assignment and current testing phases suck as placement
	 */
	private class MouseInput extends MouseAdapter{

		private Rectangle bound, boardBound;
		private Lock newLock;
		private int clickCount = 0;
		private Tile currentTile;
		private boolean moveStack = false;
		private Point lastPoint;
		private HexState movingState;
		private boolean ignore = false;
		
		/**
		 * checks to see if movement is still inside the board,
		 * check to see if a new lock can be placed,	`
		 * check to see if old lock can be released/
		 */
		@Override
	    public void mouseDragged(MouseEvent e){
			if( BYPASS_MOUSE_CLICK || ignore){
				return;
			}
			if( (moveStack || phaseDone) && currentTile!=null){
				boardBound = getBounds();
				bound = currentTile.getBounds();
				lastPoint = bound.getLocation();
				//TODO adjust click to prevent centering all the time
				bound.x = e.getX()-(bound.width/2);
				if( !boardBound.contains( bound)){
					bound.x = lastPoint.x;
				}
				bound.y = e.getY()-(bound.height/2);
				if( !boardBound.contains( bound)){
					bound.y= lastPoint.y;
				}
				if( currentTile.hasLock()){
					if( locks.canLeaveLock( currentTile, e.getPoint())){
						currentTile.removeLock();
						currentTile.setBounds( bound);
					}
				}else{
					newLock = locks.getLock( currentTile, bound.x+(bound.width/2), bound.y+(bound.height/2));
					if( newLock!=null){
						currentTile.setLockArea( newLock);
						Point center = newLock.getCenter();
						bound.setLocation( center.x-(bound.width/2), center.y-(bound.height/2));
					}
					currentTile.setBounds( bound);
				}
			}
		}
		
		@Override
		public void mouseReleased( MouseEvent e){
			if( BYPASS_MOUSE_CLICK || ignore){
				return;
			}
			if( newLock!=null&&currentTile!=null&& newLock.canHold( currentTile)){
				if( placeTileOnHex( currentTile, movingState, true)){
					
				}
			}
			moveStack = false;
			currentTile = null; 
			lastPoint = null;
			newLock = null;
			bound = null;
			repaint();
		}

		/**
		 * record initial mouse press for later drag, locking and move
		 */
		@Override
		public void mousePressed( MouseEvent e){
			if( BYPASS_MOUSE_CLICK || ignore){
				return;
			}
			Component deepestComponent = SwingUtilities.getDeepestComponentAt( Board.this, e.getX(), e.getY());
			if( phaseDone && deepestComponent!=null){
				if( deepestComponent instanceof Tile){
					currentTile = (Tile) deepestComponent;
					remove( currentTile);
					add( currentTile, 0);
					revalidate();
					if( !currentTile.isTile() && currentTile.hasLock()){
						newLock = currentTile.getLock();
						movingState = newLock.getHex().getState();
						if( movingState.hasMarker()){
							if( movingState.hasThings()){
								lastPoint = newLock.getCenter();
								Rectangle bound = new Rectangle( TILE_SIZE);
								bound.setLocation( lastPoint.x-(TILE_SIZE.width/2), lastPoint.y-(TILE_SIZE.height/2));
								currentTile = addTile( new Tile( playerMarker), bound, false);
								currentTile.setLockArea( newLock);
								currentTile.flip();
								revalidate();
								moveStack = true;
							} else {
								currentTile = null;
							}
						}
					}
				}
			}
		}

		/**
		 * for testing purposes
		 */
		@Override
		public void mouseClicked( MouseEvent e){
			if( BYPASS_MOUSE_CLICK || ignore){
				return;
			}
			if( phaseDone && SwingUtilities.isRightMouseButton( e)){
				Component deepestComponent = SwingUtilities.getDeepestComponentAt( Board.this, e.getX(), e.getY());
				if( deepestComponent!=null){
					((Tile)deepestComponent).flip();
				}
			}else if( phaseDone && SwingUtilities.isMiddleMouseButton( e)){
				FlipAllHexes();
            }else if( SwingUtilities.isLeftMouseButton( e)){
            	switch( clickCount){
            		case 0: 
            			animateHexPlacement( null);
        	            clickCount++;
        	            break;
            		case 1:
            			animateRackPlacement();
        	            clickCount++;
        	            break;
            		case 2:
            			placeMarkers( null);
        	            clickCount++;
        				break;
            	}
		    }
		}
	}
	
	/**
	 * animation task to work with timer, used for animating 
	 * tile movement from starting position to its destination
	 */
	private class MoveAnimation implements ActionListener{
		
		private Tile tile;
		private Point end;
		private Timer timer;
		private int slope, intercept, xTemp=-1, yTemp;
		private Tile[] list;
		private int index = -1;
		private Dimension size;
		
		private void setTile( Tile tile){
			this.tile = tile;
			this.end = tile.getDestination();
			xTemp = tile.getX();
			yTemp = tile.getY();
			slope = (end.y-yTemp)/(end.x-xTemp);
			intercept = yTemp-slope*xTemp;
			size = tile.getSize();
		}
		
		public MoveAnimation( Tile[] tiles ){
			list = tiles;
			tile = null;
			index = 0;
		}
		
		public void start(){
			phaseDone = false;
			timer = new Timer( ANIMATION_DELAY, this);
            timer.setInitialDelay( 0);
            timer.start();
		}

		@Override
		public void actionPerformed( ActionEvent e) {
			//animation is done
			if( !isActive || xTemp==-1){
				//list is done
				if( !isActive || index==-1 || index>=list.length){
					timer.stop();
					phaseDone = true;
					return;
				}
				//get next index in list
				if( list[index]!=null && list[index].canAnimate()){
					setTile((tile = list[index]));
					index++;
				}else{
					index++;
					return;
				}
			}
			yTemp = (int)(slope*xTemp+intercept);
			tile.setLocation( xTemp, yTemp);
			xTemp+=MOVE_DISTANCE;
			//hex has passed its final location
			if( xTemp>=end.x-size.width/2){
				xTemp=-1;
				tile.setLocation( end.x-size.width/2, end.y-size.height/2);
				tile.setLockArea( locks.getLock( tile));
				placeTileOnHex( tile, null, true);
			}
			repaint();
		}
	}
	
	/**
	 * Task for Timer to flip all hex tiles
	 */
	private class FlipAll implements ActionListener{

		private Timer timer;
		private Component[] list;
		private int index = 0;
		
		public FlipAll( Component[] components ){
			list = components;
			index = 0;
		}
		
		public void start(){
			phaseDone = false;
			timer = new Timer( ANIMATION_DELAY, this);
            timer.setInitialDelay( 0);
            timer.start();
		}

		@Override
		public void actionPerformed( ActionEvent e) {
			if(index>=list.length){
				timer.stop();
				phaseDone = true;
			}else{
				if( list[index] instanceof Hex){
					((Tile) list[index]).flip();
					repaint();
				}
				index++;
			}
		}
	}
}

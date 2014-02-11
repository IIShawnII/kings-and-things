package client.gui;

import javax.swing.Timer;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
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
import client.event.BoardUpdate;
import client.event.UpdatePlayer;
import common.Constants.Category;
import common.Constants.Restriction;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.TileProperties;
import static common.Constants.STATE;
import static common.Constants.HEX_SIZE;
import static common.Constants.TILE_SIZE;
import static common.Constants.DRAW_LOCKS;
import static common.Constants.BOARD_SIZE;
import static common.Constants.HEX_OUTLINE;
import static common.Constants.TILE_OUTLINE;
import static common.Constants.MAX_RACK_SIZE;
import static common.Constants.HEX_BOARD_SIZE;
import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_LOAD_COL;
import static common.Constants.ANIMATION_DELAY;
import static common.Constants.IMAGE_BACKGROUND;
import static common.Constants.BOARD_TOP_PADDING;
import static common.Constants.HEX_MOVE_DISTANCE;
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
	static final int INITIAL_TILE_X_SHIFT = WIDTH_SEGMENT/2;
	static final int TILE_X_SHIFT = (int) (WIDTH_SEGMENT*1.2);
	static final int TILE_Y_SHIFT = 13;
	private static final int HEX_Y_SHIFT = 8-3;
	private static final int HEX_X_SHIFT = 8-2;
	static final int PADDING = 10;
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
	
	private boolean interactWithHexes = false, hexBoradComplete = false;

	private LockManager locks;
	private MouseInput mouseInput;
	private TileProperties playerMarker;
	private PlayerInfo players[], currentPlayer;
	private Font font = new Font("default", Font.BOLD, 30);
	
	public Board( LayoutManager layout, boolean isDoubleBuffered){
		super( layout, isDoubleBuffered);
	}
	
	protected void init( int playerCount){
		players = new PlayerInfo[]{ new PlayerInfo( "Player1", 1, true), new PlayerInfo( "Player2", 2, true), new PlayerInfo( "Player3", 3, true), new PlayerInfo( "Player4", 4, true)};
		currentPlayer = players[0];
		mouseInput = new MouseInput();
		addMouseListener( mouseInput);
		addMouseMotionListener( mouseInput);
		addMouseWheelListener( mouseInput);
		locks = new LockManager( playerCount);
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
	}
	
	public Tile addTile( Tile tile, Rectangle bound, boolean lock){
		tile.init();
		tile.setBounds( bound);
		tile.addMouseListener( mouseInput);
		tile.addMouseMotionListener( mouseInput);
		if( lock){
			tile.setLockArea( locks.getPermanentLock( tile));
			tile.setCanAnimate( false);
		}else{
			tile.setCanAnimate( true);
		}
		add(tile,0);
		return tile;
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage( IMAGE, 0, 0, getWidth(), getHeight(), null);
		g2d.setFont( font);
		if( players!=null){
			for( int i=0, y=PLAYERS_STATE_PADDING; i<players.length; i++, y+=PLAYERS_STATE_PADDING){
				if( players[i]!=currentPlayer){
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

	private Tile[] setupTilesForRack( TileProperties[] prop) {
		Tile tile = null;
		Tile[] list = new Tile[MAX_RACK_SIZE];
		Lock lock = locks.getPermanentLock( Category.Cup);
		Point center = lock.getCenter();
		Rectangle bound = new Rectangle( BOARD_SIZE.width+2, BOARD_SIZE.height-TILE_SIZE.height-PADDING/2, TILE_SIZE.width, TILE_SIZE.height);
		Rectangle start = new Rectangle( center.x-TILE_SIZE.width/2, center.y-TILE_SIZE.height/2, TILE_SIZE.width, TILE_SIZE.height);
		for( int count=0; count<MAX_RACK_SIZE; count++){
			tile = addTile( new Tile( prop==null?new TileProperties(Category.Cup):prop[count]), start, false);
			if( count==5){
				bound.setLocation( BOARD_SIZE.width+2, (int) (BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(PADDING*1.5)));
			}
			bound.translate( -TILE_X_SHIFT, 0);
			tile.setDestination( bound.x+TILE_SIZE.width/2, bound.y+TILE_SIZE.height/2);
			list[count] = tile;
		}
		return list;
	}
	
	@Subscribe
	public void updatePlayers( UpdatePlayer update){
		currentPlayer = update.getCurrent();
		players = update.getPlayers();
		repaint();
	}
	
	@Subscribe
	public void updateBoard( BoardUpdate update){
		if( update.hasHexes()){
			addTile( new Hex( new HexState()), new Rectangle(8,8,HEX_SIZE.width,HEX_SIZE.height), true);
			MoveAnimation animation = new MoveAnimation( setupHexesForPlacement( update.getHexes()));
            animation.start();
            interactWithHexes = true;
		}else if( update.flipAll()){
			while( !hexBoradComplete){
				try {
					Thread.sleep( 50);
				} catch ( InterruptedException e) {}
			}
			FlipAll flip = new FlipAll( getComponents());
			flip.start();
		}else if( update.isPlayerOder()){
			int[] order = update.getPlayerOrder();
			for( int i=0; i<order.length; i++){
				if( currentPlayer.getID()==order[i]){
					playerMarker = getPlayerMarker( i);
				}
			}
			Point point = locks.getPermanentLock( Category.State).getCenter();
			Tile tile = addTile( new Tile( playerMarker), new Rectangle( point.x-TILE_SIZE.width/2, point.y-TILE_SIZE.height/2,TILE_SIZE.width,TILE_SIZE.height), true);
			tile.flip();
		}else if( update.isRack()){
			MoveAnimation animation = new MoveAnimation( setupTilesForRack( null));
            animation.start();
		}
	}
	
	private TileProperties getPlayerMarker( int order){
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
	
	private class MouseInput extends MouseAdapter{

		private Rectangle bound, boardBound;
		private Lock newLock;
		private int xDiff, yDiff;
		private int xPressed, yPressed;
		private boolean ignore = false;
		
		@Override
	    public void mouseDragged(MouseEvent e){
			if(	!ignore && e.getSource() instanceof Tile && interactWithHexes){
				Tile tile = (Tile)e.getSource();
				boardBound = getBounds();
				bound = tile.getBounds();
				xDiff = e.getX() - xPressed;
				yDiff = e.getY() - yPressed;
				bound.translate( xDiff, 0);
				if( !boardBound.contains( bound)){
					bound.translate( -xDiff, 0);
				}
				bound.translate( 0, yDiff);
				if( !boardBound.contains( bound)){
					bound.translate( 0, -yDiff);
				}
				if(tile.hasLock()){
					if( locks.canLeaveLock( tile, xDiff, yDiff)){
						tile.removeLock();
						tile.setBounds( bound);
					}
				}else{
					newLock = locks.canLockToAny( tile);
					if( newLock!=null){
						tile.setLockArea( newLock);
						Point center = newLock.getCenter();
						bound.setLocation( center.x-(bound.width/2), center.y-(bound.height/2));
					}
					tile.setBounds( bound);
				}
			}
		}

		@Override
		public void mousePressed( MouseEvent e){
			Object source = e.getSource();
			if( interactWithHexes && source instanceof Tile){
				xPressed = e.getX();
				yPressed = e.getY();
				Tile tile = (Tile)source;
				if( tile.isInside( xPressed, yPressed)){
					remove( tile);
					add( tile, 0);
					revalidate();
					repaint( tile.getBounds());
					ignore = false;
				}else{
					ignore = true;
				}
			}
		}

		@Override
		public void mouseClicked( MouseEvent e){
			Object source = e.getSource();
			if( interactWithHexes && source instanceof Tile && e.getButton()==MouseEvent.BUTTON3){
				if( ((Tile)source).isInside( xPressed, yPressed)){
					((Tile)source).flip();
				}
			}else if( interactWithHexes && !(source instanceof Tile) && e.getButton()==MouseEvent.BUTTON2){
				FlipAll flip = new FlipAll( getComponents());
				flip.start();
            }else if( !(source instanceof Tile) && e.getButton()==MouseEvent.BUTTON1){
				MoveAnimation animation = new MoveAnimation( setupHexesForPlacement( null));
	            animation.start();
	            
	            animation = new MoveAnimation( setupTilesForRack( null));
	            animation.start();
	            interactWithHexes = true;
		    }
		}
	}
	
	private class MoveAnimation implements ActionListener{
		
		private Tile tile;
		private Point end;
		private Timer timer;
		private Rectangle start;
		private int slope, intercept, xTemp=-1, yTemp;
		private Tile[] list;
		private int index = -1;
		
		public MoveAnimation( Tile tile ){
			setTile( tile);
		}
		
		private void setTile( Tile tile){
			this.tile = tile;
			this.end = tile.getDestination();
			xTemp = tile.getX();
			yTemp = tile.getY();
			slope = (end.y-yTemp)/(end.x-xTemp);
			intercept = yTemp-slope*xTemp;
		}
		
		public MoveAnimation( Tile[] tiles ){
			list = tiles;
			tile = null;
			index = 0;
		}
		
		public void start(){
			timer = new Timer( ANIMATION_DELAY, this);
            timer.setInitialDelay( 0);
            timer.start();
		}

		@Override
		public void actionPerformed( ActionEvent e) {
			if( xTemp==-1){
				if( index>=list.length){
					timer.stop();
					hexBoradComplete = true;
					return;
				}
				if( list[index]!=null && list[index].canAnimate()){
					setTile((tile = list[index]));
					index++;
				}else{
					index++;
					return;
				}
			}
			start = tile.getBounds();
			yTemp = (int)(slope*xTemp+intercept);
			tile.setLocation( xTemp, yTemp);
			xTemp+=HEX_MOVE_DISTANCE;
			//hex has passed its final location
			if( xTemp>=end.x-HEX_SIZE.width/2){
				xTemp=-1;
				tile.setLocation( end.x-HEX_SIZE.width/2, end.y-HEX_SIZE.height/2);
				tile.setLockArea( locks.getLock( tile));
				if( index==-1 || index>=list.length){
					timer.stop();
				}
			}
			start.add( tile.getBounds());
			repaint( start);
		}
	}
	
	private class FlipAll implements ActionListener{

		private Timer timer;
		private Component[] list;
		private int index = 0;
		
		public FlipAll( Component[] components ){
			list = components;
			index = 0;
		}
		
		public void start(){
			timer = new Timer( ANIMATION_DELAY, this);
            timer.setInitialDelay( 0);
            timer.start();
		}

		@Override
		public void actionPerformed( ActionEvent e) {
			if( index>=list.length){
				timer.stop();
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

package client.gui.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import client.gui.Board;
import client.gui.tiles.Hex;
import client.gui.tiles.Tile;
import common.Constants.Category;
import static common.Constants.HEX_SIZE;
import static common.Constants.LOCK_SIZE;
import static common.Constants.BOARD_SIZE;
import static common.Constants.MAX_RACK_SIZE;
import static common.Constants.TILE_OUTLINE;
import static common.Constants.TILE_SIZE;
import static common.Constants.BOARD_LOAD_COL;
import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_TOP_PADDING;

public class LockManager {
	
	private Lock[][] rackLocks;
	private Lock[][] hexBoardLocks;
	private Lock hexLock, fortLock, goldLock;
	private Lock markerLock, cupLock;/*specialLock,*/ 
	
	public LockManager( int playerCount){
		hexLock = new Lock( 8+(HEX_SIZE.width/2)-LOCK_SIZE/2, 8+(HEX_SIZE.height/2)-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE, true, true);
		Rectangle bound = new Rectangle( Board.INITIAL_TILE_X_SHIFT, Board.TILE_Y_SHIFT, TILE_SIZE.width, TILE_SIZE.height);
		bound.translate( Board.TILE_X_SHIFT, 0);
		fortLock = new Lock( bound.x+TILE_SIZE.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		bound.translate( Board.TILE_X_SHIFT, 0);
		markerLock = new Lock( bound.x+TILE_SIZE.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		bound.translate( Board.TILE_X_SHIFT, 0);
		goldLock = new Lock( bound.x+TILE_SIZE.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		bound.translate( Board.TILE_X_SHIFT, 0);
		//specialLock = new Lock( bound.x+TILE_SIZE.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		//bound.translate( Board.TILE_X_SHIFT, 0);
		cupLock = new Lock( bound.x+TILE_SIZE.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		rackLocks = new Lock[1][MAX_RACK_SIZE];
		hexBoardLocks = new Lock[7][13];
		bound.setLocation( BOARD_SIZE.width-Board.PADDING+2, BOARD_SIZE.height-TILE_OUTLINE.height-Board.PADDING+2);
		for( int i=0; i<rackLocks[0].length;i++){
			if(i==5){
				bound.setLocation( BOARD_SIZE.width-Board.PADDING+2, BOARD_SIZE.height-(2*TILE_OUTLINE.height)-(2*Board.PADDING)+2);
			}
			bound.translate( -Board.TILE_X_SHIFT, 0);
			rackLocks[0][i] = new Lock( bound.x+LOCK_SIZE/2, bound.y+LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		}
		int x=0, y=0;
		for(int ring=0; ring<BOARD_LOAD_ROW.length; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length; count++){
				x = (Board.WIDTH_SEGMENT*BOARD_LOAD_COL[ring][count]);
				y = (Board.HEIGHT_SEGMENT*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
				hexBoardLocks[BOARD_LOAD_COL[ring][count]-1][BOARD_LOAD_ROW[ring][count]-1] = new Lock( x-LOCK_SIZE/2, y-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE, false, true);
			}
		}
	}

	public Lock getPermanentLock( Category category) {
		switch( category){
			case Cup:
			case Treasure:
			case Magic:
			case Creature:
			case Building:
			case Event:
				return cupLock;
			case Buildable:
				return fortLock;
			case Gold:
				return goldLock;
			case Hex:
				return hexLock;
			/*case Special:
				return specialLock;*/
			case State:
				return markerLock;
			default:
				throw new IllegalArgumentException( "ERROR - no lock for "+category);
		}
	}
	
	public Lock getPermanentLock( Tile tile) {
		Lock lock = getPermanentLock( tile.getProperties().getCategory());
		lock.setInUse( true);
		return lock;
	}

	private Lock getLock( Point point, boolean isTile, Category category) {
		Lock lock = getPermanentLock( category);
		if( !lock.inUse && lock.contains( point)){
			lock.setInUse( true);
			return lock;
		}
		lock = null;
		if( isTile){
			lock = lookThroughLocks( rackLocks, point, isTile);
		}
		if( lock==null){
			lock = lookThroughLocks( hexBoardLocks, point, isTile);
		}
		return lock;
	}
	
	public Lock getLock( Tile tile){
		return getLock( tile.getCenter(), tile.isTile(), tile.getProperties().getCategory());
	}
	
	public Lock getLock( Tile tile, int x, int y){
		return getLock( new Point(x,y), tile.isTile(), tile.getProperties().getCategory());
	}
	
	public Lock getLockForHex( Point point){
		return hexBoardLocks[ point.x][point.y];
	}
	
	private Lock lookThroughLocks( Lock[][] locks, Point point, boolean isTile){
		for( int i=0; i<locks.length;i++){
			for( int j=0; j<locks[i].length;j++){
				if( locks[i][j]!=null&&locks[i][j].canHold( isTile, true) && canLock( locks[i][j], point)){
					locks[i][j].setInUse( true);
					return locks[i][j];
				}
			}
		}
		return null;
	}
	
	public boolean canLeaveLock( Tile tile, Point point){
		return tile.hasLock() && !tile.getLock().contains( point);
	}
	
	public boolean canLockToPermanent( Tile tile){
		return canLock( getPermanentLock( tile), tile.getCenter());
	}
	
	private boolean canLock( Lock lock, Point point){
		return lock!=null&&point!=null&&lock.contains( point);
	}
	
	public Point convertToRowAndCol( int x, int y){
		int row = x/Board.WIDTH_SEGMENT;
		int col = (y-BOARD_TOP_PADDING)/Board.HEIGHT_SEGMENT;
		return new Point( row-1, col-1);
	}
	
	public Point convertToCenterCoordinate( int row, int col){
		Point point = hexBoardLocks[row][col].lock.getLocation();
		point.x += LOCK_SIZE/2;
		point.y += LOCK_SIZE/2;
		return point;
	}

	public void draw( Graphics2D g2d) {
		g2d.setColor( Color.RED);
		g2d.fill( hexLock.lock);
		g2d.fill( fortLock.lock);
		//g2d.fill( specialLock.lock);
		g2d.fill( goldLock.lock);
		g2d.fill( cupLock.lock);
		g2d.fill( markerLock.lock);
		g2d.setColor( Color.BLUE);
		for( int i=0; i<rackLocks.length;i++){
			for( int j=0; j<rackLocks[i].length;j++){
				g2d.fill(rackLocks[i][j].lock);
			}
		}
		g2d.setColor( Color.GREEN);
		for( int i=0; i<hexBoardLocks.length;i++){
			for( int j=0; j<hexBoardLocks[i].length;j++){
				if( hexBoardLocks[i][j] !=null){
					g2d.fill(hexBoardLocks[i][j].lock);
				}
			}
		}
	}

	public class Lock {

		private final boolean isHex;
		
		private final Rectangle lock;
		private final Point center;
		private boolean inUse, permanent;
		private Hex hex;
		
		private Lock( int x, int y, int width, int height, boolean permanent, final boolean isHex){
			this.isHex= isHex;
			inUse = false;
			this.lock = new Rectangle( x, y, width, height);
			center = new Point( x+width/2, y+height/2);
			this.permanent = permanent;
		}
		
		public void setHex( Hex hex){
			this.hex = hex;
		}
		
		public Hex getHex(){
			return hex;
		}
		
		private Lock( int x, int y, int width, int height, boolean permanent) {
			this( x, y, width, height, permanent, false);
		}
		
		private Lock( int x, int y, int width, int height) {
			this( x, y, width, height, false, false);
		}

		public boolean isForHex(){
			return isHex;
		}
		
		public boolean isInUse(){
			return inUse;
		}
		
		private boolean canHold( boolean isTile, boolean checkPermanent){
			if( this.isHex && (!checkPermanent || !permanent) && isTile){
				return true;
			}else{
				return !inUse;
			}
		}
		
		public boolean canTempHold( Tile tile){
			return canHold( tile.isTile(), false);
		}
		
		public boolean canHold( Tile tile){
			return canHold( tile.isTile(), true);
		}
		
		public void setInUse( boolean use){
			this.inUse = use;
		}
		
		public boolean contains( Point point){
			return lock.contains( point);
		}
		
		public boolean canLeave( int x, int y){
			return !lock.contains( x,y);
		}
		
		public Point getCenter(){
			return center;
		}
		
		public int getCenterX(){
			return center.x;
		}
		
		public int getCenterY(){
			return center.y;
		}
		
		@Override
		public String toString(){
			return "Center: " + center + ", InUse: " + inUse + ", Hex: " + isHex;
		}
	}
}

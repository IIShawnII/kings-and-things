package client.gui;

import java.awt.Point;
import java.awt.Rectangle;

import client.gui.tiles.Tile;
import common.game.TileProperties;
import static common.Constants.HEX_SIZE;
import static common.Constants.LOCK_SIZE;
import static common.Constants.BOARD_SIZE;
import static common.Constants.MAX_RACK_SIZE;
import static common.Constants.TILE_SIZE_BANK;
import static common.Constants.BOARD_LOAD_COL;
import static common.Constants.BOARD_LOAD_ROW;
import static common.Constants.BOARD_TOP_PADDING;
import static common.Constants.PLAYERS_STATE_SIZE;

public class LockManager {
	
	private Rectangle[][] rackLocks;
	private Rectangle[][] hexBoardLocks;
	private Rectangle hexLock, fortLock, goldLock;
	private Rectangle markerLock, specialLock, cupLock;
	
	public LockManager( int playerCount){
		hexLock = new Rectangle( 8+HEX_SIZE.width/2-LOCK_SIZE/2, 8+HEX_SIZE.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		Rectangle bound = new Rectangle( Board.initialTileXShift, Board.tileYShift, TILE_SIZE_BANK.width, TILE_SIZE_BANK.height);
		fortLock = new Rectangle( bound.x+TILE_SIZE_BANK.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE_BANK.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		goldLock = new Rectangle( bound.x+TILE_SIZE_BANK.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE_BANK.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		markerLock = new Rectangle( bound.x+TILE_SIZE_BANK.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE_BANK.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		specialLock = new Rectangle( bound.x+TILE_SIZE_BANK.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE_BANK.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		cupLock = new Rectangle( bound.x+TILE_SIZE_BANK.width/2-LOCK_SIZE/2, bound.y+TILE_SIZE_BANK.height/2-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
		rackLocks = new Rectangle[playerCount][MAX_RACK_SIZE];
		hexBoardLocks = new Rectangle[7][13];
		for( int j=0; j<playerCount;j++){
			for( int i=0; i<MAX_RACK_SIZE;i++){
				rackLocks[j][i] = new Rectangle( Board.LOCK_X,Board.LOCK_Y,Board.lockBorderWidth,Board.lockBorderheight);
				if( i!=4){
					rackLocks[j][i].translate( Board.lockBorderWidth, 0);
				}else{
					rackLocks[j][i].setLocation( Board.LOCK_X, Board.Y_SHIFT);
				}
			}
		}
		int x=0, y=0;
		for(int ring=0; ring<BOARD_LOAD_ROW.length; ring++){
			for( int count=0; count<BOARD_LOAD_ROW[ring].length; count++){
				x = (Board.widthSegment*BOARD_LOAD_COL[ring][count]);
				y = (Board.heightSegment*BOARD_LOAD_ROW[ring][count])+BOARD_TOP_PADDING;
				hexBoardLocks[BOARD_LOAD_COL[ring][count]-1][BOARD_LOAD_ROW[ring][count]-1] = new Rectangle( x-LOCK_SIZE/2, y-LOCK_SIZE/2, LOCK_SIZE, LOCK_SIZE);
			}
		}
		System.out.println();
	}

	private Rectangle getPermanentLock( Tile tile) {
		TileProperties prop = tile.getProperties();
		switch( prop.getCategory()){
			case Cup:
			case Treasure:
			case Magic:
			case Creature:
			case Building:
			case Event:
				return new Rectangle(cupLock);
			case Buildable:
				return new Rectangle(fortLock);
			case Gold:
				return new Rectangle(goldLock);
			case Hex:
				return new Rectangle(hexLock);
			case Special:
				return new Rectangle(specialLock);
			case State:
				return new Rectangle(markerLock);
			default:
				throw new IllegalArgumentException( "ERROR - no lock for "+tile);
		}
	}
	
	public Rectangle getLock( Tile tile){
		Rectangle rect = tile.getBounds(), lock = null;
		if( rect.x>(BOARD_SIZE.width-PLAYERS_STATE_SIZE.width)){
			lock = lookThroughLocks( rackLocks, rect);
		}else if( rect.y>(BOARD_TOP_PADDING/2)){
			lock = lookThroughLocks( hexBoardLocks, rect);
		}else{
			lock = getPermanentLock( tile);
		}
		return lock;
	}
	
	private Rectangle lookThroughLocks( Rectangle[][] locks, Rectangle bound){
		Rectangle lock;
		for( int i=0; i<hexBoardLocks.length;i++){
			for( int j=0; j<hexBoardLocks[i].length;j++){
				/*if( canLock( hexBoardLocks[i][j], bound)){
					lock = new Rectangle( hexBoardLocks[i][j]);
					hexBoardLocks[i][j] = null;
					return lock;
				}*/
			}
		}
		return null;
	}
	
	public boolean canLeaveLock( Tile tile, int x, int y){
		return tile.hasLock() && tile.getLock().contains( tile.getCeneter( x, y));
	}
	
	public boolean canLockToPermanent( Tile tile){
		return canLock( getPermanentLock( tile), tile.getCeneter(0,0));
	}
	
	private boolean canLock( Rectangle lock, Point point){
		return lock!=null&&point!=null&&lock.contains( point);
	}
	
	public Point convertToRowAndCol( int x, int y){
		int row = x/Board.widthSegment;
		int col = y/Board.heightSegment;
		return new Point( row-1, col-1);
	}
	
	public Point convertToCenterCoordinate( int row, int col){
		Point point = hexBoardLocks[row][col].getLocation();
		point.x += LOCK_SIZE/2;
		point.y += LOCK_SIZE/2;
		return point;
	}

	public Rectangle canLockToAny( Tile tile) {
		return getLock( tile);
	}
}

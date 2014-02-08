package client.gui;

import static common.Constants.HEX_SIZE;
import static common.Constants.LOCK_SIZE;
import static common.Constants.MAX_RACK_SIZE;
import static common.Constants.TILE_SIZE_BANK;

import java.awt.Rectangle;
import java.util.ArrayList;

import client.gui.tiles.Tile;

public class LockManager {
	
	private Rectangle[][] rackLocks;
	private ArrayList< Rectangle> hexBoardLocks;
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
	}

	public Rectangle getLock( Tile tile) {
		
		return null;
	}
}

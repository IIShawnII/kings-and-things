package client.gui.tiles;

import static common.Constants.HEX_OUTLINE;
import static common.Constants.IMAGE_HEX_REVERSE;

import java.awt.Graphics;
import java.awt.Point;

import client.gui.LockManager.Lock;
import common.game.HexState;
import static common.Constants.TILE_SIZE_BOARD;

@SuppressWarnings("serial")
public class Hex extends Tile{

	private HexState state = null;
	
	public Hex( HexState state){
		super( state.getHex());
		this.state = state;
	}
	
	@Override
	public void init(){
		drawTile = IMAGE_HEX_REVERSE;
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		if(state!=null){
			state.paint( g, getCenter( TILE_SIZE_BOARD));
		}
	}
	
	@Override
	public boolean contains( Point point){
		return HEX_OUTLINE.contains( point);
	}
	
	@Override
	public void setLockArea( Lock lock){
		super.setLockArea( lock);
		lock.setHex( this);
	}
	
	public HexState getState(){
		return state;
	}
	
	public void setState( HexState state){
		this.state = state;
	}
	
	@Override
	public boolean isTile(){
		return false;
	}
}
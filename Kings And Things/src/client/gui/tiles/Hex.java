package client.gui.tiles;

import static common.Constants.IMAGES;
import static common.Constants.HEX_OUTLINE;
import static common.Constants.IMAGE_HEX_REVERSE;
import static common.Constants.BYPASS_LOAD_IMAGES;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import client.gui.LockManager.Lock;
import common.game.HexState;
import common.game.TileProperties;
import static common.Constants.TILE_SIZE_BOARD;

@SuppressWarnings("serial")
public class Hex extends Tile{

	@SuppressWarnings("unused")
	private HexState state = null;
	private TileProperties marker, battle;
	private ArrayList< TileProperties> tiles;
	
	public Hex( HexState state){
		super( state.getHex());
		this.state = state;
		tiles = new ArrayList<>();
	}
	
	@Override
	public void init(){
		drawTile = IMAGE_HEX_REVERSE;
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		if( BYPASS_LOAD_IMAGES){
			return;
		}
		Point center = getCenter( TILE_SIZE_BOARD);
		if( marker!=null && battle!=null){
			g.drawImage( IMAGES.get( marker.hashCode()), center.x+5, center.y+5, null);
			g.drawImage( IMAGES.get( battle.hashCode()), center.x-5, center.y-5, null);
		}
		if( marker!=null){
			g.drawImage( IMAGES.get( marker.hashCode()), center.x, center.y, TILE_SIZE_BOARD.width, TILE_SIZE_BOARD.height, null);
		}
		if( battle!=null){
			g.drawImage( IMAGES.get( battle.hashCode()), center.x, center.y, null);
		}
	}
	
	@Override
	public boolean isInside( int x, int y){
		return HEX_OUTLINE.contains( x, y);
	}
	
	@Override
	public void setLockArea( Lock lock){
		super.setLockArea( lock);
		lock.setHex( this);
	}
	
	public void placeTile( TileProperties prop){
		if( prop.hasRestriction()){
			switch( prop.getRestriction( 0)){
				case Battle:
					battle = prop; return;
				case Yellow:
				case Gray:
				case Green:
				case Red:
					marker = prop; return;
				default:
					break;
			}
		}
		tiles.add( prop);
	}
	
	public boolean removeTile( TileProperties prop){
		return tiles.remove( prop);
	}
	
	public void removeMarker(){
		this.marker = null;
	}
	
	public void removeBattle(){
		this.battle = null;
	}
}
package client.gui.tiles;

import static common.Constants.IMAGES;
import static common.Constants.IMAGE_TILE_REVERSE;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JComponent;

import client.gui.util.LockManager.Lock;
import common.game.ITileProperties;

@SuppressWarnings("serial")
public class Tile extends JComponent{
	
	private boolean hasLock = false;
	protected Image drawTile = null;
	protected Lock lockArea = null;
	private ITileProperties prop = null;
	private Point destination;
	private boolean canAnimate = true;
	
	public Tile( ITileProperties prop){
		super();
		this.prop = prop;
	}
	
	public boolean canAnimate(){
		return canAnimate;
	}
	
	public void setCanAnimate( boolean canAnimate){
		this.canAnimate = canAnimate;
	}
	
	public boolean isFake(){
		return prop.isFake();
	}
	
	public ITileProperties getProperties(){
		return prop;
	}
	
	public void init(){
		drawTile = IMAGE_TILE_REVERSE;
	}
	
	public void setDestination( int x, int y){
		destination = new Point( x,y);
	}
	
	public void setDestination( Point point){
		destination = point;
	}
	
	public Point getDestination(){
		return destination;
	}
	
	public void setLockArea( Lock lock){
		if( lock!=null){
			lockArea = lock;
			hasLock = true;
		}else{
			hasLock = false;
		}
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		g.drawImage( drawTile, 0, 0, getWidth(), getHeight(), null);
	}

	public void flip() {
		if( prop!=null && !prop.isFake()){
			drawTile = IMAGES.get( prop.hashCode());
		}
	}
	
	public Lock getLock(){
		return lockArea;
	}
	
	public Point getCenter(){
		return new Point( getCenterX(), getCenterY());
	}
	
	public int getCenterX(){
		return getX()+getWidth()/2;
	}
	
	public int getCenterY(){
		return getY()+getHeight()/2;		
	}
	
	public boolean hasLock(){
		return hasLock;
	}
	
	public void removeLock(){
		hasLock = false;
		lockArea.setInUse( false);
		lockArea = null;
	}
	
	protected Point getTileCenter( Dimension offset) {
		return new Point( (getWidth()/2) - (offset.width/2), (getHeight()/2) - (offset.height/2));
	}

	public boolean isTile() {
		return true;
	}
}

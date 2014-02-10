package client.gui.tiles;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import common.game.TileProperties;
import static common.Constants.IMAGES;
import static common.Constants.IMAGE_TILE_REVERSE;

@SuppressWarnings("serial")
public class Tile extends JComponent{
	
	private boolean hasLock = false;
	protected Image drawTile = null;
	private Rectangle lockArea = null;
	private TileProperties prop = null;
	
	public Tile( TileProperties prop){
		super();
		this.prop = prop;
	}
	
	public TileProperties getProperties(){
		return prop;
	}
	
	public void init(){
		drawTile = IMAGE_TILE_REVERSE;
	}
	
	public void setLockArea( Rectangle lock){
		lockArea = lock;
		hasLock = true;
	}
	
	public void setLockArea( int x, int y, int width, int height){
		setLockArea( new Rectangle( x, y, width, height));
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawImage( drawTile, 0, 0, getWidth(), getHeight(), null);
		g2d.dispose();
	}

	public void flip() {
		if( prop!=null && !prop.isFake()){
			drawTile = IMAGES.get( prop.hashCode());
		}
		repaint();
	}
	
	public boolean isInside( int x, int y){
		return true;
	}
	
	public Rectangle getLock(){
		return new Rectangle( lockArea);
	}
	
	public Point getCeneter( int xOffset, int yOffset){
		return new Point( getCenterX()+xOffset, getCenterY()+yOffset);
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
	
	public Rectangle removeLock(){
		hasLock = false;
		return new Rectangle( lockArea);
	}
}

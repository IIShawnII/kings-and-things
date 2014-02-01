package client.gui.tiles;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import static common.Constants.IMAGE_TILE_REVERSE;

@SuppressWarnings("serial")
public class Tile extends JComponent{
	
	private Rectangle lockArea = null;
	private boolean hasLock = false;
	protected Image drawTile = null;
	
	public Tile(){
		super();
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
		repaint();
	}
	
	public boolean isInside( int x, int y){
		return true;
	}
	
	public boolean canLock( int x, int y){
		return hasLock && lockArea.contains( getCenterX()+x, getCenterY()+y);
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

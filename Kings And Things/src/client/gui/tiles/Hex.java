package client.gui.tiles;

import java.util.Random;

import java.io.File;
import java.io.IOException;

import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Graphics2D;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import static common.Constants.HEX_IMAGES;

@SuppressWarnings("serial")
public class Hex extends JComponent{
	
	static{
		File[] images = new File( "Resources\\Hex\\").listFiles();
		for( int i=0; i<HEX_IMAGES.length; i++){
			try {
				HEX_IMAGES[i] = ImageIO.read( images[i]);
			} catch ( IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int drawIndex = 0;
	private Polygon bound; 
	private Rectangle lockArea = null;
	private boolean hasLock = false;
	
	public Hex(){
		super();
	}
	
	public void init(){
		int w = getWidth()/4;
		int h = getHeight()/2;
		bound = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
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
		g2d.drawImage( HEX_IMAGES[ drawIndex], 0, 0, getWidth(), getHeight(), null);
		g2d.dispose();
	}

	public void flip() {
		drawIndex = new Random().nextInt( HEX_IMAGES.length-1)+1;
		repaint();
	}
	
	@Override
	public boolean contains( int x, int y){
		return bound.contains( x, y);
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

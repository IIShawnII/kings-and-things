package client.gui.tiles;

import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class Hex extends Tile{
	
	public static final BufferedImage[] HEX_IMAGES = new BufferedImage[9];
	static{
		File[] images = new File( "Resources\\Hex\\").listFiles();
		for( int i=0; i<HEX_IMAGES.length; i++){
			try {
				HEX_IMAGES[i] = ImageIO.read( images[i]);
			} catch ( IOException e) {
				e.printStackTrace();
				System.err.println("Hex");
			}
		}
	}
	
	private int drawIndex = 0;
	private Polygon bound;
	
	public Hex(){
		super();
	}
	
	@Override
	public void init(){
		int w = getWidth()/4;
		int h = getHeight()/2;
		bound = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		drawTile = HEX_IMAGES[ drawIndex];
	}

	@Override
	public void flip() {
		drawIndex = new Random().nextInt( HEX_IMAGES.length-1)+1;
		drawTile = HEX_IMAGES[ drawIndex];
		repaint();
	}
	
	@Override
	public boolean isInside( int x, int y){
		return bound.contains( x, y);
	}
}

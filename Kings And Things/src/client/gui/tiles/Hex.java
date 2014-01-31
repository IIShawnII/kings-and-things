package client.gui.tiles;

import java.awt.Polygon;

import static common.Constants.HEX_REVERSE;

@SuppressWarnings("serial")
public class Hex extends Tile{
	
	private Polygon bound;
	
	public Hex(){
		super();
	}
	
	@Override
	public void init(){
		int w = getWidth()/4;
		int h = getHeight()/2;
		bound = new Polygon( new int[]{w,0,w,w*3,w*4,w*3}, new int[]{h*2,h,0,0,h,h*2}, 6);
		drawTile = HEX_REVERSE;
	}

	@Override
	public void flip() {
		//TODO add flip
		repaint();
	}
	
	@Override
	public boolean isInside( int x, int y){
		return bound.contains( x, y);
	}
}

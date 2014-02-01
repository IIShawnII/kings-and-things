package client.gui.tiles;

import static common.Constants.HEX_OUTLINE;
import static common.Constants.IMAGE_HEX_REVERSE;

@SuppressWarnings("serial")
public class Hex extends Tile{
	
	public Hex(){
		super();
	}
	
	@Override
	public void init(){
		drawTile = IMAGE_HEX_REVERSE;
	}

	@Override
	public void flip() {
		//TODO add flip
		repaint();
	}
	
	@Override
	public boolean isInside( int x, int y){
		return HEX_OUTLINE.contains( x, y);
	}
}

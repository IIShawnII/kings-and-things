package client.gui.tiles;

import static common.Constants.HEX_OUTLINE;
import static common.Constants.IMAGE_HEX_REVERSE;
import common.game.HexState;

@SuppressWarnings("serial")
public class Hex extends Tile{
	
	public Hex( HexState state){
		super( state);
	}
	
	@Override
	public void init(){
		drawTile = IMAGE_HEX_REVERSE;
	}
	
	@Override
	public boolean isInside( int x, int y){
		return HEX_OUTLINE.contains( x, y);
	}
}

package client.gui.util.undo;

import java.awt.Point;

import common.game.HexState;
import client.gui.tiles.Tile;
import client.gui.util.animation.MoveAnimation;

public class UndoTileMovement implements Undo{

	private Tile tile;
	private HexState state;
	private Point location;
	
	public UndoTileMovement( Tile tile, Point location){
		this.tile = tile;
		this.location = location;
	}

	@Override
	public void undo( MoveAnimation animation) {
		if( animation==null){
			throw new NullPointerException();
		}
		tile.setDestination( location);
		animation.start( tile);
	}
}
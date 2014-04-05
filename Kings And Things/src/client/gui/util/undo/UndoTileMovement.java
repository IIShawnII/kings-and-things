package client.gui.util.undo;

import java.awt.Point;

import common.game.HexState;
import client.gui.tiles.Tile;
import client.gui.util.animation.MoveAnimation;

public class UndoTileMovement implements Undo{

	private Tile tile;
	private HexState state;
	private Point location;
	
	private UndoTileMovement( Tile tile, HexState hex, Point location){
		this.tile = tile;
		this.state = hex;
		this.location = location;
	}

	/**
	 * used when initial mouse press happens
	 */
	public UndoTileMovement( Tile tile, Point location){
		this(tile, null, location);
	}

	/**
	 * used when placing marker on hex
	 */
	public UndoTileMovement( Tile tile, HexState hex) {
		this(tile, hex, null);
	}
	
	/**
	 * used when exchanging hex tiles
	 */
	public UndoTileMovement( Tile tile) {
		this(tile, null, null);
	}

	@Override
	public void undo( MoveAnimation animation, Parent parent) {
		if( animation==null){
			throw new NullPointerException();
		}
		if( state==null && location==null){
			if( tile.getParent()==null){
				parent.addTile(tile);
			}
		}else if( location==null){
			state.removeMarker();
			if( tile.getParent()==null){
				parent.addTile(tile);
			}
		}else if( state==null){
			tile.setDestination( location);
			animation.start( tile);
		}
	}

	@Override
	public boolean undoLast() {
		return location==null || ( state==null && location==null);
	}
}
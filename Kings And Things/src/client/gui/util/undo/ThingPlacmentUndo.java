package client.gui.util.undo;

import java.awt.Point;

import common.game.HexState;
import client.gui.tiles.Tile;
import client.gui.util.animation.MoveAnimation;

public class ThingPlacmentUndo implements Undo{

	private ThingPlacmentOperation start, end;
	
	public ThingPlacmentUndo(){
		start = null;
		end = null;
	}
	
	@Override
	public void addStart( OperationInterface operation){
		if( start!=null){
			throw new IllegalStateException( "Operation already started");
		}
		if(!(operation instanceof ThingPlacmentOperation)){
			throw new IllegalArgumentException( "Operation must be created using the factory method in repected Undo class");
		}
		start = (ThingPlacmentOperation)operation;
	}
	
	@Override
	public void addEnd( OperationInterface operation){
		if( end!=null){
			throw new IllegalStateException( "Operation already ended");
		}
		if(!(operation instanceof ThingPlacmentOperation)){
			throw new IllegalArgumentException( "Operation must be created using the factory method in repected Undo class");
		}
		end = (ThingPlacmentOperation)operation;
	}
	
	public static OperationInterface createOperation( Tile tile, HexState hex, Point location){
		return new ThingPlacmentOperation(tile, hex, location);
	}

	@Override
	public void undo( MoveAnimation animation) {
		if( start==null || end==null){
			throw new IllegalStateException( "NO operation created to be undo");
		}
		if( end.isPlaced()){
			end.getHexState().removeMarker();
		}
		end.getTile().setDestination( start.getLocation());
		animation.start( end.getTile());
	}

	@Override
	public boolean isComplete() {
		return start!=null && end!=null;
	}
	
	private static class ThingPlacmentOperation implements OperationInterface{
		
		private Point location;
		private HexState hex;
		private Tile tile;
		
		private ThingPlacmentOperation( Tile tile, HexState hex, Point location){
			this.tile = tile;
			this.hex = hex;
			this.location = location;
		}
		
		private boolean isPlaced(){
			return hex!=null;
		}
		
		private HexState getHexState(){
			return hex;
		}
		
		private Point getLocation(){
			return location;
		}
		
		private Tile getTile(){
			return tile;
		}
	}
}
package client.gui.util.undo;

import java.util.ArrayDeque;
import java.util.Deque;

import client.gui.util.animation.MoveAnimation;

/**
 * UndoManager follows FILO policy.
 */
public class UndoManager {
	
	private Deque< Undo> operations;
	
	public UndoManager(){
		operations = new ArrayDeque<>();
	}
	
	public void addUndo( Undo undo){
		if( undo==null){
			throw new NullPointerException();
		}
		if( !undo.isComplete()){
			throw new IllegalArgumentException( "undo state must be complete before adding");
		}
		operations.addFirst( undo);
	}
	
	public void undo( MoveAnimation animation){
		operations.getLast().undo( animation);
	}
}

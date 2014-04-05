package client.gui.util.undo;

import java.util.ArrayDeque;
import java.util.Deque;

import client.gui.util.animation.MoveAnimation;

/**
 * UndoManager follows FILO policy.
 */
public class UndoManager {
	
	private Parent parent;
	private Deque< Undo> operations;
	
	public UndoManager(){
		operations = new ArrayDeque<>();
	}
	
	public void addUndo( Undo undo){
		if( undo==null){
			throw new NullPointerException();
		}
		operations.addFirst( undo);
	}
	
	public void undo( MoveAnimation animation){
		Undo undo = operations.getLast();
		undo.undo(animation, parent);
		if( undo.undoLast()){
			operations.getLast().undo( animation, parent);
		}
	}
}

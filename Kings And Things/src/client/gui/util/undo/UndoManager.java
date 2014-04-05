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
	
	public UndoManager( Parent parent){
		operations = new ArrayDeque<>();
		this.parent = parent;
	}
	
	public void addUndo( Undo undo){
		if( undo==null){
			throw new NullPointerException();
		}
		operations.addFirst( undo);
	}
	
	public void undo( MoveAnimation animation){
		if(operations.size()<=0){
			return;
		}
		Undo undo = operations.removeFirst();
		undo.undo(animation, parent);
		if( undo.undoLast()){
			operations.removeFirst().undo( animation, parent);
		}
	}
}

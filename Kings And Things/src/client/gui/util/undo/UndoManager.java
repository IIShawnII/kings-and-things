package client.gui.util.undo;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * UndoManager follows FILO policy.
 */
public class UndoManager {
	
	private Undo<OperationInterface> current;
	private Deque< Undo<OperationInterface>> operations;
	
	public UndoManager(){
		current = null;
		operations = new ArrayDeque<>();
	}
	
	public void addStart( OperationInterface operation){
		if( current!=null){
			throw new IllegalStateException( "cannot start recording new operation till last one is done");
		}
		current = new Undo<OperationInterface>();
		current.add( operation);
	}
	
	public void addEnd( OperationInterface operation){
		if( current==null){
			throw new IllegalStateException( "cannot end recording, no active operation is avaliable");
		}
		current.add( operation);
		operations.addFirst( current);
		current = null;
	}
	
	public Undo<OperationInterface> getLastUndo(){
		return operations.getLast();
	}
	
	/**
	 * this method will only remove last Start operation.
	 * it will not remove a complete Operations. if operation
	 * is finished, nothing will happen and method returns.
	 */
	public void removeLastOperation(){
		if( current!=null){
			current.clear();
			current = null;
		}
	}
}

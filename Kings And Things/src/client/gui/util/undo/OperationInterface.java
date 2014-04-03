package client.gui.util.undo;

public interface OperationInterface {

	/**
	 * is used to clean Operation of all its members, before removal;
	 * called by UndoManager when removing a unfinished undo Operation.
	 */
	public void clear();
}

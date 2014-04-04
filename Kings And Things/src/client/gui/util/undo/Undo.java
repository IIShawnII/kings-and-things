package client.gui.util.undo;

import client.gui.util.animation.MoveAnimation;

public interface Undo{
	
	public void undo( MoveAnimation animation);
	
	public void addStart( OperationInterface operation);
	
	public void addEnd( OperationInterface operation);

	public boolean isComplete();
}
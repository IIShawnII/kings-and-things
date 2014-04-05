package client.gui.util.undo;

import client.gui.util.animation.MoveAnimation;

public interface Undo{
	
	public boolean undoLast();
	
	public void undo( MoveAnimation animation, Parent parent);
}
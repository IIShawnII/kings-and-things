package client.gui.util.animation;

import common.game.HexState;
import client.gui.tiles.Tile;
import client.gui.util.LockManager.Lock;

public interface CanvasParent {

	public void phaseDone();
	
	public void phaseStarted();

	public boolean isActive();
	
	public void repaintCanvas();
	
	public Lock getLock( Tile tile);
	
	public HexState placeTileOnHex( Tile tile);
}

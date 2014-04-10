package client.logic;

import java.awt.Point;
import java.util.Collection;
import java.util.List;

import client.gui.util.LockManager.Lock;
import common.Constants.Permissions;
import common.Constants.RollReason;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.PlayerInfo;

public interface Control {
	
	public void undo();

	public void resetPhase();
	
	public void placeTowers();
	
	public boolean isRolling();
	
	public void waitForPhase();
	
	public void placeMarkers();
	
	public void flipAllHexes();
	
	public void requestRepaint();
	
	public void setDiceCount( int count);
	
	public RollReason getLastRollReason();
	
	public Lock getLockForHex( Point point);
	
	public void placeHexes( HexState[] hexes);
	
	public ITileProperties getLastRollTarget();
	
	public void setStatusMessage( String message);
	
	public void setPlayers( PlayerInfo[] players);
	
	public void setDiceResult( List<Integer> list);
	
	public void placeNewHexOnBOard( HexState state);

	public void setCurrentPlayer( PlayerInfo player);
	
	public ITileProperties getLastCombatResolvedHex();
	
	public void setPermission( Permissions permission);
	
	public void animateHexPlacement( HexState[] tiles);
	
	public void animateRackPlacement( ITileProperties[] tiles);
	
	public void animateHandPlacement(Collection<ITileProperties> tiles);
	
	public void showErrorMessage( String title, String message);
	
	public void prepareForRollDice( int count, RollReason reason, String message, ITileProperties target);
}

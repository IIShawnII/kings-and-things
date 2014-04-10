package client.logic;

import static common.Constants.HEX_SIZE;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import client.gui.Board;
import client.gui.components.ISelectionListener;
import client.gui.components.RemoveThingsFromHexPanel;
import client.gui.components.SelectThingsForMovementPanel;
import client.gui.tiles.Hex;
import client.gui.tiles.Tile;
import client.gui.util.LockManager;
import client.gui.util.LockManager.Lock;
import client.gui.util.undo.Parent;
import client.gui.util.undo.UndoManager;
import client.gui.util.undo.UndoTileMovement;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.HexContentsTarget;
import common.Constants.Permissions;
import common.Constants.RollReason;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.ViewHexContentsResponse;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.PlayerInfo;
import common.game.Roll;

/**
 * input class for mouse, used for like assignment and current testing phases suck as placement
 */
public class Controller extends MouseAdapter implements ActionListener, Parent, Control{

	private final int PLAYER_ID;
	private boolean demo;
	private Board board;
	private Lock newLock;
	private Point lastPoint;
	private Tile currentTile;
	private LockManager locks;
	private UndoManager undoManger;
	private Permissions permission;
	@SuppressWarnings("unused")
	private UpdateReceiver receiver;
	private RollReason lastRollReason;
	private Rectangle bound, boardBound;
	private ITileProperties lastRollTarget;
	private ITileProperties lastCombatResolvedHex;
	private final HashSet<ITileProperties> lastMovementSelection;
	private final LinkedHashSet<ITileProperties> hexMovementSelection;
	
	public Controller(Board board, boolean demo, LockManager locks, final int ID){
		this.board = board;
		this.locks = locks;
		this.PLAYER_ID = ID;
		this.demo = demo;
		lastMovementSelection = new HashSet<>();
		hexMovementSelection = new LinkedHashSet<>();
		this.receiver = new UpdateReceiver( this, ID);
		this.undoManger = new UndoManager( this);
		permission = Permissions.NoMove;
	}
	
	public void undo(){
		undoManger.undo( board.getAnimator());
	}
	
	@Override
	public void setPermission( Permissions permission){
		this.permission = permission;
	}
	
	/**
	 * checks to see if movement is still inside the board,
	 * check to see if a new lock can be placed,	`
	 * check to see if old lock can be released/
	 */
	@Override
    public void mouseDragged(MouseEvent e){
		if( !canMove()){
			return;
		}
		e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, board);
		if( board.isPhaseDone() && currentTile!=null){
			boardBound = board.getBounds();
			bound = currentTile.getBounds();
			lastPoint = bound.getLocation();
			bound.x = e.getX()-(bound.width/2);
			if( !boardBound.contains( bound)){
				bound.x = lastPoint.x;
			}
			bound.y = e.getY()-(bound.height/2);
			if( !boardBound.contains( bound)){
				bound.y= lastPoint.y;
			}
			if( currentTile.hasLock()){
				if( locks.canLeaveLock( currentTile, e.getPoint())){
					currentTile.removeLock();
					currentTile.setBounds( bound);
				}
			}else{
				switch( permission){
					case MoveFromCup:
					case MoveFromRack:
					case MoveMarker:
					case MoveTower:
					case PlayTreasure:
						newLock = locks.getLock( currentTile, bound.x+(bound.width/2), bound.y+(bound.height/2));
						break;
					case ExchangeHex:
					case ExchangeThing:
						newLock = locks.getDropLock( currentTile);
						break;
					default:
						return;
				}
				if( newLock!=null){
					currentTile.setLockArea( newLock);
					Point center = newLock.getCenter();
					bound.setLocation( center.x-(bound.width/2), center.y-(bound.height/2));
				}
				currentTile.setBounds( bound);
			}
		}
	}
	
	@Override
	public void mouseReleased( MouseEvent e){
		if( !canMove()){
			return;
		}
		e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, board);
		//TODO this condition might need to be updated for future phases
		if(currentTile!=null){
			if( newLock==null){
				undo();
			}else{
				if(currentTile.getProperties().isTreasure() && !currentTile.getProperties().isSpecialIncomeCounter())
				{
					//TODO need to update undo manager
					removeCurrentTile();
					new UpdatePackage(UpdateInstruction.PlayTreasure,UpdateKey.Tile, currentTile.getProperties(), "Controller.Input").postNetworkEvent(PLAYER_ID);
				}
				else
				{
					switch( permission){
						case MoveMarker:
							if( newLock.canHold( currentTile)){
								HexState hex = board.placeTileOnHex( currentTile);
								if( hex!=null){
									undoManger.addUndo( new UndoTileMovement(currentTile, hex));
									removeCurrentTile();
									new UpdatePackage( UpdateInstruction.HexOwnership, UpdateKey.HexState, hex, "Controll.Input").postNetworkEvent( PLAYER_ID);
								}
							}
							break;
						case ExchangeThing:
							break;
						case ExchangeHex:
							if( newLock.canTempHold( currentTile)){
								undoManger.addUndo( new UndoTileMovement(currentTile));
								removeCurrentTile();
								new UpdatePackage( UpdateInstruction.SeaHexChanged, UpdateKey.HexState, ((Hex)currentTile).getState(), "Controll.Input").postNetworkEvent( PLAYER_ID);
							}
							break;
						case MoveFromCup:
							break;
						case MoveFromRack:
							if( newLock.canHold( currentTile)){
								//TODO need to update undo manager
								removeCurrentTile();
								UpdatePackage update = new UpdatePackage( "Controll.input", null);
								update.addInstruction( UpdateInstruction.PlaceBoard);
								update.putData( UpdateKey.Tile, currentTile.getProperties());
								update.putData( UpdateKey.Hex, newLock.getHex().getState().getHex());
								update.postNetworkEvent( PLAYER_ID);
							}
							break;
						case MoveTower:
							if( newLock.canHold( currentTile)){
								//TODO need to update undo manager
								removeCurrentTile();
								UpdatePackage update = new UpdatePackage( "Controll.input", null);
								update.addInstruction( UpdateInstruction.ConstructBuilding);
								update.putData( UpdateKey.Tile, currentTile.getProperties().getBuildable());
								update.putData( UpdateKey.Hex, newLock.getHex().getState().getHex());
								update.postNetworkEvent( PLAYER_ID);
							}
							break;
						default:
							return;
					}
				}
			}
		}
		prepareForNextMouseRelease();
	}

	/**
	 * record initial mouse press for later drag, locking and move
	 */
	@Override
	public void mousePressed( MouseEvent e){
		if( !canMove()){
			return;
		}
		e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, board);
		Component deepestComponent = SwingUtilities.getDeepestComponentAt( board, e.getX(), e.getY());
		if( board.isPhaseDone() && deepestComponent!=null && deepestComponent instanceof Tile){
			currentTile = (Tile) deepestComponent;
			if( !checkTilePermission( currentTile)){
				return;
			}
			//move the component to the top, prevents overlapping
			board.remove( currentTile);
			board.add( currentTile, 0);
			board.revalidate();
			board.repaint();
			switch( permission){
				case MoveMarker:break;//nothing to do
				case ExchangeHex:break;//nothing to do
				case ExchangeThing:
					break;
				case MoveFromCup:
					break;
				case MoveFromRack:
					break;
				case MoveTower:
					break;
				default:
					return;
			}
			if( currentTile!=null){
				undoManger.addUndo( new UndoTileMovement( currentTile, currentTile.getCenter()));
			}
			//code for moving stack, need update
			/*newLock = currentTile.getLock();
			movingState = newLock.getHex().getState();
			if( movingState.hasMarker()){
				if( movingState.hasThings()){
					lastPoint = newLock.getCenter();
					Rectangle bound = new Rectangle( TILE_SIZE);
					bound.setLocation( lastPoint.x-(TILE_SIZE.width/2), lastPoint.y-(TILE_SIZE.height/2));
					currentTile = addTile( new Tile( playerMarker), bound, false);
					currentTile.setLockArea( newLock);
					currentTile.flip();
					revalidate();
					moveStack = true;
				} else {
					currentTile = null;
				}
			}*/
		}
		
	}
	
	@Override
	public void mouseExited(MouseEvent e){
		if( e.getSource()==board.getDice()){
			board.getDice().shrink();
		}
	}

	@Override
	public void mouseClicked( MouseEvent e){
		switch( permission){
			case Roll:
				tryToRoll( e);
				break;
			case ResolveCombat:
				showContextMenu(e,true,false);
				break;
			case MoveTower:
				showContextMenu(e,false,true);
				break;
			case NoMove:
			default:
				showContextMenu(e,false,false);
				return;
		}
	}

	@Override
	public void actionPerformed( ActionEvent e) {
		new UpdatePackage( UpdateInstruction.Skip, "Board.Input").postNetworkEvent( PLAYER_ID);
	}
	
	private void removeCurrentTile(){
		board.remove( currentTile);
		board.revalidate();
		board.repaint();
	}
	
	private void prepareForNextMouseRelease(){
		currentTile = null; 
		lastPoint = null;
		newLock = null;
		bound = null;
		board.repaint();
	}
	
	private boolean checkTilePermission( Tile tile){
		switch( permission){
			case ExchangeHex:
			case ResolveCombat:
				return !tile.isTile();
			case ExchangeThing:
			case MoveFromCup:
			case MoveFromRack:
			case MoveMarker:
			case MoveTower:
				return tile.isTile();
			case PlayTreasure:
				return tile.getProperties().isTreasure() && !tile.getProperties().isSpecialIncomeCounter();
			default:
				throw new IllegalStateException(" Encountered none tile permission: " + permission);
		}
	}
	
	private void tryToRoll( MouseEvent e){
		if( SwingUtilities.isLeftMouseButton(e)){
			if( e.getSource()==board.getDice()){
				if( board.getDice().canRoll()){
					int rollValue = 0;
					if(demo){
						try{
							rollValue = Integer.parseInt(JOptionPane.showInputDialog(board, "Select desired roll value", "RollValue", JOptionPane.PLAIN_MESSAGE));
						}catch(NumberFormatException ex){
							rollValue = 0;
						}
						if( rollValue<board.getDice().getDiceCount()*Constants.MIN_DICE_FACE || rollValue>board.getDice().getDiceCount()*Constants.MAX_DICE_FACE){
							board.setStatusMessage( "value must be between " + (board.getDice().getDiceCount()*Constants.MIN_DICE_FACE) + " and " + (board.getDice().getDiceCount()*Constants.MAX_DICE_FACE));
							return;
						}
					}
					Roll roll = new Roll( board.getDice().getDiceCount(), lastRollTarget, lastRollReason, PLAYER_ID, rollValue);
					new UpdatePackage( UpdateInstruction.NeedRoll, UpdateKey.Roll, roll,"Board "+PLAYER_ID).postNetworkEvent( PLAYER_ID);
					board.getDice().roll();
					new Thread( new Runnable() {
						@Override
						public void run() {
							while( board.getDice().isRolling()){
								try {
									Thread.sleep( 10);
								} catch ( InterruptedException e) {}
							}
							board.setStatusMessage( "Done Rolling: " + board.getDice().getResults());
							if(lastRollReason != RollReason.EXPLORE_HEX)
							{
								new UpdatePackage( UpdateInstruction.DoneRolling, "Board.Input").postNetworkEvent( PLAYER_ID);
							}
						}
					}, "Dice Wait").start();
				}else{
					board.getDice().expand();
				}
			}
		}
	}
	
	private void showContextMenu(MouseEvent e, boolean resolveCombatPermission, boolean upgradeBuildingPermission)
	{
		if(SwingUtilities.isRightMouseButton(e))
		{
			e = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, board);
			Component deepestComponent = SwingUtilities.getDeepestComponentAt( board, e.getX(), e.getY());
			if(deepestComponent instanceof Hex)
			{
				final Hex source = (Hex) deepestComponent;
				e = SwingUtilities.convertMouseEvent(board, e, source);
				final ITileProperties hex = source.getState().getHex();
				JPopupMenu clickMenu = new JPopupMenu("Select Action");
				
				JMenuItem initiateCombat = new JMenuItem("Resolve Combat");
				initiateCombat.setEnabled(resolveCombatPermission);
				initiateCombat.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						lastCombatResolvedHex = hex;
						new UpdatePackage(UpdateInstruction.InitiateCombat, UpdateKey.Hex,hex,"Board " + PLAYER_ID).postNetworkEvent(PLAYER_ID);
					}});
				clickMenu.add(initiateCombat);
				
				JMenuItem removeThings = new JMenuItem("Remove Things");
				removeThings.setEnabled(!isSomeoneElsesHex(source.getState()));
				removeThings.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						SwingWorker<Void,Void> removalDialogBuilder = new SwingWorker<Void,Void>(){
							private volatile ViewHexContentsResponse response = null;
							
							@Override
							protected Void doInBackground() throws Exception
							{
								UpdatePackage msg = new UpdatePackage(UpdateInstruction.ViewContents, UpdateKey.Hex,hex,"Board " + PLAYER_ID);
								msg.putData(UpdateKey.Category, HexContentsTarget.REMOVAL);
								msg.postNetworkEvent(PLAYER_ID);

								while(response==null)
								{
									Thread.sleep(100);
								}
								return null;
							}
							
							@Override
							protected void done()
							{
								JFrame removalDialog = new JFrame("Remove things");
								JScrollPane scrollPane = new JScrollPane();
								scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
								scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
								
								RemoveThingsFromHexPanel panel = new RemoveThingsFromHexPanel(PLAYER_ID, removalDialog, hex, false);
								panel.init(response.getContents(), 0);
								scrollPane.setViewportView(panel);
								removalDialog.setContentPane(scrollPane);
								removalDialog.pack();
								removalDialog.setLocationRelativeTo(null);
								removalDialog.setVisible(true);
								EventDispatch.unregisterFromInternalEvents(this);
							}
							
							@Subscribe
							public void recieveHexContents(ViewHexContentsResponse response)
							{
								if(response.getTarget() == HexContentsTarget.REMOVAL)
								{
									this.response = response;
								}
							}};
						EventDispatch.registerOnInternalEvents(removalDialogBuilder);
						removalDialogBuilder.execute();
					}});
				clickMenu.add(removeThings);

				JMenuItem viewContents = new JMenuItem("See Contents");
				viewContents.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						UpdatePackage msg = new UpdatePackage(UpdateInstruction.ViewContents, UpdateKey.Hex,hex,"Board " + PLAYER_ID);
						msg.putData(UpdateKey.Category, HexContentsTarget.VIEW);
						msg.postNetworkEvent(PLAYER_ID);
					}});
				clickMenu.add(viewContents);

				JMenuItem upgradeBuilding = new JMenuItem("Upgrade Building");
				boolean canUpgrade = upgradeBuildingPermission && !isSomeoneElsesHex(source.getState()) && 
						source.getState().hasBuilding() && source.getState().getBuilding().isBuildableBuilding() && !source.getState().getBuilding().getName().equals(Building.Citadel.name());
				upgradeBuilding.setEnabled(canUpgrade);
				upgradeBuilding.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						BuildableBuilding toConstruct = source.getState().getBuilding().getNextBuilding();
						UpdatePackage msg = new UpdatePackage(UpdateInstruction.ConstructBuilding, UpdateKey.Hex, source.getState().getHex(), "Board " + PLAYER_ID);
						msg.putData(UpdateKey.Tile, toConstruct);
						msg.postNetworkEvent(PLAYER_ID);
					}});
				clickMenu.add(upgradeBuilding);
				
				//put move stuff in own section
				clickMenu.add(new JSeparator());
				
				JMenuItem moveThings = new JMenuItem("Start Movement");
				moveThings.setEnabled(!isSomeoneElsesHex(source.getState()));
				moveThings.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						SwingWorker<Void,Void> movementDialogBuilder = new SwingWorker<Void,Void>(){
							private volatile ViewHexContentsResponse response = null;
							
							@Override
							protected Void doInBackground() throws Exception
							{
								UpdatePackage msg = new UpdatePackage(UpdateInstruction.ViewContents, UpdateKey.Hex,hex,"Board " + PLAYER_ID);
								msg.putData(UpdateKey.Category, HexContentsTarget.MOVEMENT);
								msg.postNetworkEvent(PLAYER_ID);

								while(response==null)
								{
									Thread.sleep(100);
								}
								return null;
							}
							
							@Override
							protected void done()
							{
								hexMovementSelection.clear();
								hexMovementSelection.add(source.getState().getHex());
								JFrame movementSelector = new JFrame("Movement");
								movementSelector.setContentPane(new SelectThingsForMovementPanel(movementSelector,response.getContents(),new ISelectionListener<ITileProperties>(){
									@Override
									public void selectionChanged(Collection<ITileProperties> newSelection)
									{
										lastMovementSelection.clear();
										lastMovementSelection.addAll(newSelection);
										board.setStatusMessage( "Select Hexes To Move Through");
									}}));
								movementSelector.pack();
								movementSelector.setLocationRelativeTo(null);
								movementSelector.setVisible(true);
								EventDispatch.unregisterFromInternalEvents(this);
							}
							
							@Subscribe
							public void recieveHexContents(ViewHexContentsResponse response)
							{
								if(response.getTarget() == HexContentsTarget.MOVEMENT)
								{
									this.response = response;
								}
							}};
						EventDispatch.registerOnInternalEvents(movementDialogBuilder);
						movementDialogBuilder.execute();
					}});
				clickMenu.add(moveThings);
				
				JMenuItem addMoveHex = new JMenuItem("Add to Movement Path");
				addMoveHex.setEnabled(lastMovementSelection.size()>0);
				addMoveHex.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						hexMovementSelection.add(source.getState().getHex());
					}});
				clickMenu.add(addMoveHex);

				JMenuItem finishMoveHex = new JMenuItem("Finish Movement Here");
				finishMoveHex.setEnabled(lastMovementSelection.size()>0);
				finishMoveHex.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						hexMovementSelection.add(source.getState().getHex());
						UpdatePackage msg = new UpdatePackage(UpdateInstruction.MoveThings, UpdateKey.Hex, new ArrayList<>(hexMovementSelection), "Board " + PLAYER_ID);
						msg.putData(UpdateKey.ThingArray, new ArrayList<>(lastMovementSelection));
						msg.postNetworkEvent(PLAYER_ID);
						hexMovementSelection.clear();
						lastMovementSelection.clear();
					}});
				clickMenu.add(finishMoveHex);
				
				clickMenu.show(source, e.getX(), e.getY());
			}
		}
	}
	
	private boolean isSomeoneElsesHex(HexState hs)
	{
		for( PlayerInfo pi : board.getPlayers())
		{
			if(hs.hasMarkerForPlayer(pi.getID()) && pi.getID() != PLAYER_ID)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean canMove(){
		return permission!=Permissions.NoMove && permission!=Permissions.Roll;
	}

	@Override
	public void addTile(Tile tile) {
		board.add(tile);
	}
	
	@Override
	public void prepareForRollDice( int count, RollReason reason, String message, ITileProperties target){
		board.getDice().setDiceCount( count);
		setStatusMessage( message);
		lastRollReason = reason;
		lastRollTarget = target;
	}


	/**
	 * used primarily for animation wait time.
	 * thread sleeps till animation is over.
	 */
	@Override
	public synchronized void waitForPhase() {
		while( !board.isPhaseDone()){
			try{
				Thread.sleep( 50);
			}catch( InterruptedException ex){}
		}
	}

	@Override
	public void placeHexes(HexState[] hexes) {
		board.animateHexPlacement( hexes);
	}

	@Override
	public void setStatusMessage(String message) {
		board.setStatusMessage(message);
	}

	@Override
	public void setPlayers(PlayerInfo[] players) {
		board.setPlayers(players);
		board.repaint();
	}

	@Override
	public void setCurrentPlayer(PlayerInfo player) {
		board.setCurrentPlayer( player);
		board.repaint();
	}

	@Override
	public void showErrorMessage(String title, String message) {
		JOptionPane.showMessageDialog( board, message, title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void placeTowers() {
		board.placeTower();
	}

	@Override
	public boolean isRolling() {
		return board.getDice().isRolling();
	}

	@Override
	public void placeMarkers() {
		board.placeMarkers();
	}

	@Override
	public void flipAllHexes() {
		board.FlipAllHexes();
	}

	@Override
	public void requestRepaint() {
		board.repaint();
	}

	@Override
	public void setDiceCount(int count) {
		board.getDice().setDiceCount(count);
	}

	@Override
	public RollReason getLastRollReason() {
		return lastRollReason;
	}

	@Override
	public Lock getLockForHex(Point point) {
		return locks.getLockForHex(point);
	}

	@Override
	public ITileProperties getLastRollTarget() {
		return lastRollTarget;
	}

	@Override
	public void setDiceResult(List<Integer> list) {
		board.getDice().setResult(list);
	}

	@Override
	public ITileProperties getLastCombatResolvedHex() {
		return lastCombatResolvedHex;
	}

	@Override
	public void animateHexPlacement(HexState[] tiles) {
		board.animateHexPlacement(tiles);
	}

	@Override
	public void animateRackPlacement(ITileProperties[] tiles) {
		board.animateRackPlacement(tiles);
	}

	@Override
	public void placeNewHexOnBOard(HexState state) {
		Lock end = locks.getLockForHex( state.getLocation());
		if( !state.getHex().isFaceUp()){
			state.getHex().flip();
		}
		if(end.isInUse()){
			end.getHex().setState( state);
		}else{
			Lock start = locks.getPermanentLock( state.getHex().getCategory());
			Tile hex = board.addTile( new Hex( state), new Rectangle( start.getCenter(),HEX_SIZE), false);
			hex.setDestination(end.getCenter());
			hex.flip();
			if( board.isActive()){
				board.getAnimator().start(hex);
			}else{
				//TODO this code more than likely will never be reached, not tested
				hex.setLocation( end.getCenterOffSet( HEX_SIZE));
				hex.setLockArea( end);
			}
		}
		
	}

	@Override
	public void resetPhase() {
		board.phaseDone();
	}
}
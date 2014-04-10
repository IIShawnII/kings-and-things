package client.logic;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import client.gui.components.CombatPanel;
import client.gui.components.HexContentsPanel;
import client.gui.components.RemoveThingsFromHexPanel;
import client.gui.components.combat.ExplorationResultsPanel;
import client.gui.util.LockManager.Lock;

import common.Constants;
import common.Constants.CombatPhase;
import common.Constants.HexContentsTarget;
import common.Constants.Permissions;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.Logger;
import common.event.AbstractUpdateReceiver;
import common.event.UpdatePackage;
import common.event.network.ExplorationResults;
import common.event.network.HexNeedsThingsRemoved;
import common.event.network.InitiateCombat;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.PlayerInfo;
import common.game.Roll;

public class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

	private final int PLAYER_ID;
	private Control controller;
	
	protected UpdateReceiver( Control controller, final int ID) {
		super( INTERNAL, Constants.BOARD, controller);
		this.controller = controller;
		this.PLAYER_ID = ID;
	}

	@Override
	protected void handlePrivate( UpdatePackage update) {
		updateBoard( update);
	}

	@Override
	protected boolean verifyPrivate( UpdatePackage update) {
		return update.isValidID(ID|PLAYER_ID);
	}
	
	/**
	 * update the board with new information, such as
	 * hex placement, flip all, player order and rack info.
	 * @param update - event wrapper containing update information
	 */
	public void updateBoard( UpdatePackage update){
		HexState hex = null;
		controller.resetPhase();
		for( UpdateInstruction instruction : update.getInstructions()){
			switch( instruction){
				case Start:
					break;//nothing to do
				case UpdatePlayers:
					controller.setCurrentPlayer( (PlayerInfo)update.getData( UpdateKey.Player));
					controller.setPlayers( (PlayerInfo[]) update.getData( UpdateKey.Players));
					break;
				case Rejected:
					manageRejection( (UpdateInstruction)update.getData( UpdateKey.Instruction), (String)update.getData( UpdateKey.Message));
					break;
				case PlaceBoard:
					controller.placeHexes( (HexState[]) update.getData( UpdateKey.Hex));
					break;
				case SetupPhase:
					manageSetupPhase( (SetupPhase)update.getData( UpdateKey.Phase));
					break;
				case RegularPhase:
					manageRegularPhase( (RegularPhase)update.getData( UpdateKey.Phase));
					break;
				case CombatPhase:
					manageCombatPhase((CombatPhase)update.getData(UpdateKey.Phase));
					break;
				case DieValue:
					Roll roll = (Roll)update.getData( UpdateKey.Roll);
					controller.setDiceResult( roll.getBaseRolls());
					break;
				case HexOwnership:
					hex = (HexState)update.getData( UpdateKey.HexState);
					if( controller.getPlayerCount()<Constants.MAX_PLAYERS){
						Point point = hex.getLocation();
						int x = point.x+1;
						int y = point.y+2;
						hex.setLocation( x, y);
					}
					controller.getLockForHex( hex.getLocation()).getHex().setState( hex);
					break;
				case HexStatesChanged:
					for(HexState hs : (HexState[])update.getData(UpdateKey.HexState))
					{
						controller.getLockForHex(hs.getLocation()).getHex().setState(hs);
					}
					break;
				case FlipAll:
					controller.flipAllHexes();
					break;
				case SeaHexChanged:
					controller.placeNewHexOnBOard( (HexState)update.getData( UpdateKey.HexState));
					break;
				case GameState:
					controller.animateHexPlacement( (HexState[]) update.getData( UpdateKey.Hex));
					controller.waitForPhase();
					if( (boolean) update.getData( UpdateKey.Flipped)){
						controller.flipAllHexes();
					}
					controller.waitForPhase();
					controller.animateRackPlacement( (ITileProperties[]) update.getData( UpdateKey.Rack));
					controller.waitForPhase();

					controller.setPlayers( (PlayerInfo[]) update.getData( UpdateKey.Players));
					controller.setCurrentPlayer( (PlayerInfo)update.getData( UpdateKey.Player));
					
					SetupPhase currSetupPhase = (SetupPhase) update.getData(UpdateKey.Setup);
					if(currSetupPhase.ordinal() > SetupPhase.DETERMINE_PLAYER_ORDER.ordinal())
					{
						controller.placeMarkers();
					}
					if(currSetupPhase.ordinal() >= SetupPhase.PLACE_FREE_TOWER.ordinal())
					{
						controller.placeTowers();
					}
					if(currSetupPhase != SetupPhase.SETUP_FINISHED)
					{
						manageSetupPhase(currSetupPhase);
					}
					else
					{
						manageRegularPhase((RegularPhase) update.getData(UpdateKey.Regular));
					}
					controller.requestRepaint();
					break;
				case InitiateCombat:
					final InitiateCombat combat = (InitiateCombat) update.getData(UpdateKey.Combat);
					try {
						SwingUtilities.invokeAndWait(new Runnable(){
							@Override
							public void run() {
								HashSet<HexState> possibleRetreatHexes = new HashSet<>();
								for(Point p : combat.getCombatHexState().getAdjacentLocations())
								{
									try
									{
										Lock l = controller.getLockForHex(p);
										if(l != null)
										{
											if(l.getHex().getState().hasMarkerForPlayer( PLAYER_ID))
											{
												possibleRetreatHexes.add(l.getHex().getState());
											}
										}
									}
									catch(IndexOutOfBoundsException e)
									{
									}
								}
								Player player = null;
								HashSet<Player> otherPlayers = new HashSet<>();
								for(Player p : combat.getInvolvedPlayers())
								{
									if(p.getID() == PLAYER_ID)
									{
										player = p;
									}
									else
									{
										otherPlayers.add(p);
									}
								}
								
								JFrame combatDialog = new JFrame("Combat!");
								CombatPanel panel = new CombatPanel(combat.getCombatHexState(), possibleRetreatHexes, player, otherPlayers,
										combat.getCurrentCombatPhase(), combat.getDefendingPlayer(), combat.getPlayerOrder(), combatDialog);
								panel.init();
								combatDialog.setContentPane(panel);
								combatDialog.pack();
								combatDialog.setLocationRelativeTo(null);
								combatDialog.setVisible(true);
							}});
					} catch (Throwable t) {
						Logger.getErrorLogger().error("Problem processing combat initiation command: ", t);
					}
					break;
				case RemoveThingsFromHex:
					final HexNeedsThingsRemoved evt = (HexNeedsThingsRemoved) update.getData(UpdateKey.HexState);
					if(evt.isFirstNotificationForThisHex())
					{
						try
						{
							SwingUtilities.invokeAndWait(new Runnable()
							{
								@Override
								public void run() {
									JFrame removalDialog = new JFrame("Remove things");
									JScrollPane scrollPane = new JScrollPane();
									scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
									scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
									
									RemoveThingsFromHexPanel panel = new RemoveThingsFromHexPanel(evt.getPlayerRemovingThings().getID(), removalDialog, evt.getHex().getHex(), true);
									panel.init(evt.getHex().getThingsInHexOwnedByPlayer(evt.getPlayerRemovingThings()), evt.getNumToRemove());
									scrollPane.setViewportView(panel);
									removalDialog.setContentPane(scrollPane);
									removalDialog.pack();
									removalDialog.setLocationRelativeTo(null);
									removalDialog.setVisible(true);
								}});
						} catch (Throwable t) {
							Logger.getErrorLogger().error("Problem processing remove things from hex command: ", t);
						}
					}
					break;
				case ShowExplorationResults:
					final ExplorationResults results = (ExplorationResults)update.getData(UpdateKey.Combat);

					while( controller.isRolling())
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
						}
					}
					try
					{
						SwingUtilities.invokeAndWait(new Runnable()
						{
							@Override
							public void run() {
								JFrame resultsDialog = new JFrame("Exploration");
								JScrollPane scrollPane = new JScrollPane();
								scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
								scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
									
								ExplorationResultsPanel panel = new ExplorationResultsPanel(results.getExplorer().getID(),results.getHex(),resultsDialog);
								panel.init(results.results());
								scrollPane.setViewportView(panel);
								resultsDialog.setContentPane(scrollPane);
								resultsDialog.pack();
								resultsDialog.setLocationRelativeTo(null);
								resultsDialog.setVisible(true);
							}});
					} catch (Throwable t) {
						Logger.getErrorLogger().error("Problem processing exploration results command: ", t);
					}
					break;
				case ViewContents:
					if((HexContentsTarget)update.getData(UpdateKey.Category) == HexContentsTarget.VIEW)
					{
						@SuppressWarnings("unchecked")
						final Collection<ITileProperties> thingsInHex = (Collection<ITileProperties>) update.getData(UpdateKey.Hex);
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run()
							{
								JFrame frame = new JFrame("Hex Contents");
								frame.setContentPane(new HexContentsPanel(thingsInHex));
								frame.pack();
								frame.setLocationRelativeTo(null);
								frame.setVisible(true);
							}});
					}
					break;
				case RackChanged:
					controller.animateRackPlacement((ITileProperties[])update.getData(UpdateKey.Rack));
					break;
				case HandChanged:
					@SuppressWarnings("unchecked")
					final Set<ITileProperties> hand = (Set<ITileProperties>) update.getData(UpdateKey.ThingArray);
					controller.animateHandPlacement(hand);
					break;
				default:
					throw new IllegalStateException( "ERROR - No handle for " + update.peekFirstInstruction());
			}
			controller.waitForPhase();
		}
	}
	
	private void manageRejection( UpdateInstruction data, String message) {
		//TODO Handle More Rejections
		switch( data){
			case Skip:
				//controller.setPermission( Permissions.NoMove);
				controller.setStatusMessage( "Cannot skip this phase");
				break;
			case TieRoll:
				controller.setPermission( Permissions.Roll);
				controller.prepareForRollDice( 2, controller.getLastRollReason(), "Tie Roll, Roll again", controller.getLastRollTarget());
				break;
			case SeaHexChanged:
				controller.setPermission( Permissions.ExchangeHex);
				controller.showErrorMessage( "Rejected", message);
				controller.setStatusMessage( "Sea Hex exchange not possible");
				controller.undo();
				break;
			case HexOwnership:
				controller.setPermission( Permissions.MoveMarker);
				controller.setStatusMessage( "WARN - cannot own this hex");
				controller.undo();
				break;
			case RecruitThings:
				controller.setHasRecruited(false);
			default:
				controller.setStatusMessage( "WARN - Inavlid move");
				Logger.getStandardLogger().warn( "No handle for rejection of: " + data);
		}
	}
	
	private void manageRegularPhase( RegularPhase phase) {
		switch( phase){
			case COMBAT:
				controller.setPermission(Permissions.ResolveCombat);
				controller.setStatusMessage( "Select combat to resolve, if any");
				break;
			case CONSTRUCTION:
				controller.setPermission(Permissions.MoveTower);
				controller.setStatusMessage( "Construct or upgrade buildings, if any");
				break;
			case MOVEMENT:
				controller.setPermission(Permissions.PlayTreasure);
				controller.setStatusMessage( "Move things on board, if any");
				break;
			case RANDOM_EVENTS:
				controller.setPermission(Permissions.RandomEvents);
				controller.setStatusMessage( "Play random event, if any");
				break;
			case RECRUITING_CHARACTERS:
				controller.setPermission(Permissions.Roll);
				controller.setStatusMessage( "Select hero to recruit, if any");
				break;
			case RECRUITING_THINGS:
				controller.setPermission(Permissions.RecruitThings);
				controller.setHasRecruited(false);
				controller.setStatusMessage( "Recruit things");
				break;
			case SPECIAL_POWERS:
				controller.setPermission(Permissions.PlayTreasure);
				controller.setStatusMessage( "Use hero abilities, if any");
				break;
			default:
				break;
		}
	}
	
	private void manageCombatPhase(CombatPhase phase)
	{
		switch(phase)
		{
			case PLACE_THINGS:
				controller.setStatusMessage( "Place things in combat hex");
				break;
			case DETERMINE_DEFENDERS:
				controller.setPermission(Permissions.Roll);
				controller.prepareForRollDice( 1, RollReason.EXPLORE_HEX, "Roll to explore hex", controller.getLastCombatResolvedHex());
				break;
			default:
				break;
		}
	}

	private void manageSetupPhase( SetupPhase phase){
		switch( phase){
			case DETERMINE_PLAYER_ORDER:
				controller.setPermission( Permissions.Roll);
				controller.prepareForRollDice(2, RollReason.DETERMINE_PLAYER_ORDER, "Roll dice to determine order", null);
				break;
			case EXCHANGE_SEA_HEXES:
				controller.setPermission( Permissions.ExchangeHex);
				controller.setStatusMessage( "Exchange sea hexes, if any");
				break;
			case EXCHANGE_THINGS:
				controller.setPermission( Permissions.ExchangeThing);
				controller.setStatusMessage( "Exchange things, if any");
				break;
			case PICK_FIRST_HEX:
				controller.setPermission( Permissions.MoveMarker);
				controller.placeMarkers();
				controller.setStatusMessage( "Pick your first Hex");
				break;
			case PICK_SECOND_HEX:
				controller.setPermission( Permissions.MoveMarker);
				controller.setStatusMessage( "Pick your second Hex");
				break;
			case PICK_THIRD_HEX:
				controller.setPermission( Permissions.MoveMarker);
				controller.setStatusMessage( "Pick your third Hex");
				break;
			case PLACE_EXCHANGED_THINGS:
				controller.setPermission( Permissions.MoveFromRack);
				controller.setStatusMessage( "Place exchanged things on board, if any");
				break;
			case PLACE_FREE_THINGS:
				controller.setPermission( Permissions.MoveFromRack);
				controller.setStatusMessage( "Place things on board, if any");
				break;
			case PLACE_FREE_TOWER:
				controller.setPermission( Permissions.MoveTower);
				controller.placeTowers();
				controller.setStatusMessage( "Place one free tower on board");
				break;
			case SETUP_FINISHED:
				controller.setPermission( Permissions.NoMove);
				controller.setStatusMessage( "Setup Phase Complete");
				break;
			default:
				break;
		}
	}
}
	
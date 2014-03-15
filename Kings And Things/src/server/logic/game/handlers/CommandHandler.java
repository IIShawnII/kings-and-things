package server.logic.game.handlers;

import java.util.ArrayList;
import java.util.List;

import server.event.commands.DiceRolled;
import server.event.commands.EndPlayerTurnCommand;
import server.event.commands.PlayerWaivedRetreat;
import server.event.commands.RollDiceCommand;
import server.event.commands.SetupPhaseComplete;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.GameState;
import server.logic.game.Player;
import server.logic.game.RollModification;
import server.logic.game.validators.CommandValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.Logger;
import common.event.EventDispatch;
import common.event.notifications.DieRoll;
import common.event.notifications.HexOwnershipChanged;
import common.event.notifications.PlayerState;
import common.event.notifications.RackPlacement;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Roll;

public abstract class CommandHandler
{
	//sub classes can not and should not change these fields,
	//they are to be set only after handling a start game command
	private GameState currentState;
	private boolean isDemoMode;
	
	/**
	 * call this method to initialize this class before sending it commands
	 */
	public void initialize()
	{
		EventDispatch.registerOnInternalEvents(this);
	}
	
	/**
	 * call this method when you are done with the instance
	 */
	public void dispose()
	{
		EventDispatch.unregisterFromInternalEvents(this);
	}

	/**
	 * Call this to end the current players turn (progresses to the next phase)
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 */
	public void endPlayerTurn(int playerNumber){
		CommandValidator.validateCanEndPlayerTurn(playerNumber, currentState);
		if(currentState.getCurrentCombatPhase() == CombatPhase.ATTACKER_TWO_RETREAT || currentState.getCurrentCombatPhase() == CombatPhase.ATTACKER_ONE_RETREAT || 
				currentState.getCurrentCombatPhase() == CombatPhase.ATTACKER_THREE_RETREAT || currentState.getCurrentCombatPhase() == CombatPhase.DEFENDER_RETREAT)
		{
			new PlayerWaivedRetreat(this).postInternalEvent(playerNumber);
		}
		else
		{
			advanceActivePhasePlayer();
		}
	}

	/**
	 * Call this to roll dice for a player
	 * @param reasonForRoll The reason for this dice roll
	 * @param playerNumber The player who sent the command
	 * @param tile The target of the role, (could be hex, creature, building etc)
	 * @param rollValue The desired outcome of the roll, this value is ignored unless
	 * we are running in demo mode
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT
	 */
	public void rollDice(RollReason reasonForRoll, int playerNumber, ITileProperties tile, int rollValue)
	{
		CommandValidator.validateCanRollDice(reasonForRoll, playerNumber, tile, currentState);
		makeDiceRoll(reasonForRoll, playerNumber, tile, rollValue);
	}

	protected final GameState getCurrentState()
	{
		return currentState;
	}

	protected final boolean isDemoMode()
	{
		return isDemoMode;
	}

	protected void makeHexOwnedByPlayer(ITileProperties hex, int playerNumber)
	{
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsHex(hex))
			{
				p.removeHexFromOwnership(hex);
				break;
			}
		}
		currentState.getPlayerByPlayerNumber(playerNumber).addOwnedHex(hex);

		HexState hs = getCurrentState().getBoard().getHexStateForHex(hex);
		new HexOwnershipChanged(hs).postNetworkEvent( playerNumber);
	}

	protected void advanceActivePhasePlayer(){
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
		int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
		
		int indexOfActiveTurnPlayer = currentState.getPlayerOrder().indexOf(currentState.getActiveTurnPlayer().getID());
		if(indexOfActiveTurnPlayer == ((activePhasePlayerOrderIndex + 1) % currentState.getPlayers().size()))
		{
			if(nextSetupPhase != SetupPhase.SETUP_FINISHED)
			{
				nextSetupPhase = getNextSetupPhase();
			}
			else
			{
				nextRegularPhase = getNextRegularPhase();
				regularPhaseChanged(nextRegularPhase);
			}
		}
		else
		{
			currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
		}
		currentState.setCurrentSetupPhase(nextSetupPhase);
		currentState.setCurrentRegularPhase(nextRegularPhase);
		currentState.setCurrentCombatPhase(CombatPhase.NO_COMBAT);
		currentState.setCombatLocation(null);
		currentState.recordRollForSpecialCharacter(null);
		currentState.setDefendingPlayerNumber(-1);
		currentState.removeAllHexesWithBuiltInObjects();
		currentState.clearAllPlayerTargets();
		
		new PlayerState(currentState.getActivePhasePlayer().getPlayerInfo()).postNetworkEvent();
	}
	
	protected void advanceActiveTurnPlayer(){
		int activeTurnPlayerNumber = currentState.getActiveTurnPlayer().getID();
		int activeTurnPlayerOrderIndex = currentState.getPlayerOrder().indexOf(activeTurnPlayerNumber);
		int nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(++activeTurnPlayerOrderIndex % currentState.getPlayers().size());
		
		//in a 2 player game turn order doesn't swap
		if(currentState.getPlayers().size() == 2)
		{
			nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(0);
		}

		currentState.setActivePhasePlayer(nextActiveTurnPlayerNumber);
		currentState.setActiveTurnPlayer(nextActiveTurnPlayerNumber);
	}
	
	private SetupPhase getNextSetupPhase(){
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		
		if(nextSetupPhase == SetupPhase.SETUP_FINISHED)
		{
			return SetupPhase.SETUP_FINISHED;
		}
		else
		{
			int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
			int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
			currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
			
			int currentSetupPhaseIndex = nextSetupPhase.ordinal();
			for(SetupPhase sp : SetupPhase.values())
			{
				if(sp.ordinal() == (currentSetupPhaseIndex + 1))
				{
					setupPhaseChanged(sp);
					return sp;
				}
			}
		}
		
		throw new IllegalStateException("GameState contained invalid SetupPhase constant");
	}

	private RegularPhase getNextRegularPhase(){
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		if(nextRegularPhase == RegularPhase.SPECIAL_POWERS)
		{
			advanceActiveTurnPlayer();
			return RegularPhase.RECRUITING_CHARACTERS;
		}
		else
		{
			int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
			int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
			currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
			
			if(currentState.getBoard().getContestedHexes(currentState.getPlayers()).size() > 0)
			{
				return RegularPhase.COMBAT;
			}
			int currentRegularPhaseIndex = nextRegularPhase.ordinal();
			for(RegularPhase rp : RegularPhase.values())
			{
				if(rp.ordinal() == (currentRegularPhaseIndex + 1))
				{
					return rp;
				}
			}
		}
		
		throw new IllegalStateException("GameState contained invalid RegularPhase constant");
	}

	private void setupPhaseChanged(SetupPhase setupPhase)
	{
		switch(setupPhase)
		{
			case EXCHANGE_SEA_HEXES:
			{
				//we need to flip all board hexes face up
				for(HexState hs : currentState.getBoard().getHexesAsList())
				{
					hs.getHex().flip();
				}
				break;
			}
			case PLACE_FREE_TOWER:
			{
				//give players 10 gold each
				for(Player p : currentState.getPlayers())
				{
					p.addGold(10);
				}
				break;
			}
			case PLACE_FREE_THINGS:
			{
				//give all players 10 free things from cup, things are drawn randomly so player order
				//doesn't matter, unless we are in demo mode, in which case we must do it in player
				//order, so let's just do it in player order all the time
				ArrayList<Player> players = new ArrayList<Player>();
				ArrayList<Integer> playerOrder = new ArrayList<Integer>(currentState.getPlayerOrder());
				for(Integer i : playerOrder)
				{
					players.add(currentState.getPlayerByPlayerNumber(i));
				}

				for(Player p : players)
				{
					RackPlacement tray = new RackPlacement(10);
					for(int i=0; i<10; i++)
					{
						try
						{
							ITileProperties thing = currentState.getCup().drawTile();
							p.addThingToTray(thing);
							tray.getArray()[i] = thing;
						}
						catch (NoMoreTilesException e)
						{
							// should never happen
							Logger.getErrorLogger().error("Unable to draw 10 free things for: " + currentState.getActivePhasePlayer() + ", due to: ", e);
						}
					}
					
					tray.postNetworkEvent();
					new PlayerState(p.getPlayerInfo()).postNetworkEvent();
				}
				break;
			}
			case SETUP_FINISHED:
			{
				currentState.getBoardGenerator().setupFinished();
				regularPhaseChanged(currentState.getCurrentRegularPhase());
			}
			default:
				break;
		}
	}

	private void regularPhaseChanged(RegularPhase regularPhase)
	{
		switch(regularPhase)
		{
			case RECRUITING_CHARACTERS:
			{
				//do income phase automagically
				makeGoldCollected();
				break;
			}
			case COMBAT:
			{
				//replenish move points of all creatures in preparation for next round
				for(HexState hs : currentState.getBoard().getHexesAsList())
				{
					for(ITileProperties tp : hs.getCreaturesInHex())
					{
						tp.setMoveSpeed(4);
					}
				}
			}
			default:
				break;
		}
	}

	private void makeGoldCollected()
	{
		for(Player p : currentState.getPlayers())
		{
			p.addGold(p.getIncome());
		}
	}

	private void makeDiceRoll(RollReason reasonForRoll, int playerNumber, ITileProperties tile, int rollValue)
	{
		if(reasonForRoll == RollReason.ENTERTAINMENT)
		{
			currentState.addNeededRoll(new Roll(1, null, RollReason.ENTERTAINMENT, playerNumber));
		}
		
		Roll rollToAddTo = null;
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, reasonForRoll, playerNumber, tile) && r.needsRoll())
			{
				rollToAddTo = r;
				break;
			}
		}
		if(rollToAddTo == null && reasonForRoll == RollReason.RECRUIT_SPECIAL_CHARACTER)
		{
			rollToAddTo = new Roll(2, tile, RollReason.RECRUIT_SPECIAL_CHARACTER, playerNumber);
			currentState.addNeededRoll(rollToAddTo);
		}

		rollToAddTo.addBaseRoll(rollDie(rollValue));
		if(currentState.hasRollModificationFor(rollToAddTo))
		{
			List<RollModification> modifications = currentState.getRollModificationsFor(rollToAddTo);
			for(RollModification rm : modifications)
			{
				rollToAddTo.addRollModificationFor(rm.getRollIndexToModify(), rm.getAmountToAdd());
				currentState.removeRollModification(rm);
			}
		}
		//notifies players of die roll
		new DieRoll(rollToAddTo).postNetworkEvent(playerNumber);
		
		//if we are no longer waiting for more rolls, then we can apply the effects now
		if(!currentState.isWaitingForRolls())
		{
			new DiceRolled( this).postInternalEvent();
		}
	}

	private int rollDie(int rollValue)
	{
		return isDemoMode? rollValue : (int) Math.round((Math.random() * 5) + 1);
	}
	
	@Subscribe
	public void receiveGameStartedEvent(SetupPhaseComplete event)
	{
		currentState = event.getCurrentState();
		isDemoMode = event.isDemoMode();
	}

	@Subscribe
	public void recieveEndPlayerTurnCommand(EndPlayerTurnCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				endPlayerTurn(command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process EndPlayerTurnCommand due to: ", t);
			}
		}
	}

	@Subscribe
	public void receiveRollDiceCommand(RollDiceCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				rollDice(command.getReasonForRoll(), command.getID(), command.getTileToRollFor(), command.getRollValue());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process RollDieCommand due to: ", t);
			}
		}
	}
}
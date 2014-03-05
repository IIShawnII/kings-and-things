package server.logic.game.handlers;

import java.util.ArrayList;
import java.util.List;

import server.event.DiceRolled;
import server.event.GameStarted;
import server.event.commands.EndPlayerTurnCommand;
import server.event.commands.RollDiceCommand;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.BoardGenerator;
import server.logic.game.CupManager;
import server.logic.game.GameState;
import server.logic.game.HexTileManager;
import server.logic.game.Player;
import server.logic.game.RollModification;
import server.logic.game.SpecialCharacterManager;
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
	private CupManager cup;
	private HexTileManager bank;
	private BoardGenerator boardGenerator;
	private GameState currentState;
	private boolean isDemoMode;
	private SpecialCharacterManager bankHeroes;
	
	/**
	 * call this method to initialize this class before sending it commands
	 */
	public void initialize()
	{
		EventDispatch.registerForInternalEvents(this);
	}
	
	/**
	 * call this method when you are done with the instance
	 */
	public void dispose()
	{
		EventDispatch.unregisterForCommandEvents(this);
	}

	/**
	 * Call this to end the current players turn (progresses to the next phase)
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 */
	public void endPlayerTurn(int playerNumber){
		CommandValidator.validateCanEndPlayerTurn(playerNumber, currentState);
		advanceActivePhasePlayer();
	}

	/**
	 * Call this to roll dice for a player
	 * @param reasonForRoll The reason for this dice roll
	 * @param playerNumber The player who sent the command
	 * @param tile The target of the role, (could be hex, creature, building etc)
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT
	 */
	public void rollDice(RollReason reasonForRoll, int playerNumber, ITileProperties tile)
	{
		CommandValidator.validateCanRollDice(reasonForRoll, playerNumber, tile, currentState);
		makeDiceRoll(reasonForRoll, playerNumber, tile);
	}
	
	protected final CupManager getCup()
	{
		return cup;
	}

	protected final HexTileManager getBank()
	{
		return bank;
	}

	protected final BoardGenerator getBoardGenerator()
	{
		return boardGenerator;
	}
	
	protected final SpecialCharacterManager getBankHeroManager()
	{
		return bankHeroes;
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
		new HexOwnershipChanged(hs).postNotification( playerNumber);
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
		currentState.setDefendingPlayerNumber(-1);
		currentState.removeAllHexesWithBuiltInObjects();
		
		new PlayerState(currentState.getActivePhasePlayer().getPlayerInfo()).postNotification();
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
							ITileProperties thing = cup.drawTile();
							p.addThingToTray(thing);
							tray.getArray()[i] = thing;
						}
						catch (NoMoreTilesException e)
						{
							// should never happen
							Logger.getErrorLogger().error("Unable to draw 10 free things for: " + currentState.getActivePhasePlayer() + ", due to: ", e);
						}
					}
					
					tray.postNotification();
					new PlayerState(p.getPlayerInfo()).postNotification();
				}
				break;
			}
			case SETUP_FINISHED:
			{
				boardGenerator.setupFinished();
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

	private void makeDiceRoll(RollReason reasonForRoll, int playerNumber, ITileProperties tile)
	{
		if(reasonForRoll == RollReason.ENTERTAINMENT)
		{
			currentState.addNeededRoll(new Roll(1, null, RollReason.ENTERTAINMENT, playerNumber));
		}
		else if(reasonForRoll == RollReason.RECRUIT_SPECIAL_CHARACTER)
		{
			currentState.addNeededRoll(new Roll(2, tile, RollReason.RECRUIT_SPECIAL_CHARACTER, playerNumber));
		}
		
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, reasonForRoll, playerNumber, tile) && r.needsRoll())
			{
				r.addBaseRoll(rollDie());
				if(currentState.hasRollModificationFor(r))
				{
					List<RollModification> modifications = currentState.getRollModificationsFor(r);
					for(RollModification rm : modifications)
					{
						r.addRollModificationFor(rm.getRollIndexToModify(), rm.getAmountToAdd());
					}
				}
				//notifies players of die roll
				new DieRoll(r).postNotification(playerNumber);
				break;
			}
		}
		
		//if we are no longer waiting for more rolls, then we can apply the effects now
		if(!currentState.isWaitingForRolls())
		{
			new DiceRolled().postCommand();
		}
	}

	private static int rollDie()
	{
		return (int) Math.round((Math.random() * 5) + 1);
	}
	
	@Subscribe
	public void receiveGameStartedEvent(GameStarted event)
	{
		cup = event.getCup();
		bank = event.getBank();
		boardGenerator = event.getBoardGenerator();
		currentState = event.getCurrentState();
		bankHeroes = event.getBankHeroManager();
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
				rollDice(command.getReasonForRoll(), command.getID(), command.getTileToRollFor());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process RollDieCommand due to: ", t);
			}
		}
	}
}

package server.logic.game.handlers;

import java.util.Set;
import java.awt.Point;
import java.util.HashSet;
import java.util.ArrayList;

import server.event.DiceRolled;
import server.event.GameStarted;
import server.event.internal.ExchangeSeaHexCommand;
import server.event.internal.GiveHexToPlayerCommand;
import server.event.internal.StartSetupPhaseCommand;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.GameState;
import server.logic.game.validators.SetupPhaseValidator;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.game.Roll;
import common.game.Player;
import common.game.HexState;
import common.game.ITileProperties;
import common.Constants;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.UpdateInstruction;
import common.event.network.PlayersList;
import common.event.network.CurrentPhase;
import common.event.network.HexPlacement;
import common.event.network.CommandRejected;
import common.event.network.ExchangedSeaHex;

import static common.Constants.ALL_PLAYERS_ID;

public class SetupPhaseCommandHandler extends CommandHandler{
	
	/**
	 * Use this method to start a new game
	 * @param demoMode Set to true to stack the deck to match with the demo script
	 * @param players The players who will be playing this game
	 * @throws NoMoreTilesException If there are not enough tiles left in the bank
	 * to set up another board
	 * @throws IllegalArgumentException if the entered list of players is invalid
	 */
	public void startNewGame(boolean demoMode, Set<Player> players) throws NoMoreTilesException{
		SetupPhaseValidator.validateStartNewGame(demoMode, players);

		GameState currentState = new GameState(demoMode,players,new ArrayList<Integer>(),SetupPhase.DETERMINE_PLAYER_ORDER, RegularPhase.RECRUITING_CHARACTERS,0,0, CombatPhase.NO_COMBAT, Constants.PUBLIC, null);
		for(Player p : currentState.getPlayers())
		{
			currentState.addNeededRoll(new Roll(2, null, RollReason.DETERMINE_PLAYER_ORDER, p.getID()));
		}
		
		new PlayersList( players).postNetworkEvent( ALL_PLAYERS_ID);
		
		HexPlacement placement = new HexPlacement( players.size()==Constants.MAX_PLAYERS?Constants.MAX_HEXES:Constants.MIN_HEXES_ON_BOARD);
		currentState.getBoard().fillArray( placement.getHexes());
		placement.postNetworkEvent( ALL_PLAYERS_ID);
		
		//send to client only when client can interact with the cards
		/*SpecialCharacterManager bank = currentState.getBankHeroes();
		SpecialCharUpdate special = new SpecialCharUpdate( bank.getAvailableHeroes().size());
		bank.getAvailableHeroes().toArray( special.getSpecial());
		special.postNetworkEvent( ALL_PLAYERS_ID);*/
		
		new GameStarted(demoMode, currentState).postInternalEvent();
		//new Flip().postNetworkEvent();
		//new SetupPhaseComplete(demoMode, currentState, this).postInternalEvent();
		
		//send player list and current phase to clients
		new CurrentPhase<SetupPhase>( currentState.getPlayerInfoArray(), SetupPhase.DETERMINE_PLAYER_ORDER).postNetworkEvent( ALL_PLAYERS_ID);
	}

	/**
	 * Use this method to give control of a hex to a player
	 * @param hex The hex to change control of
	 * @param playerNumber The player sending the command
	 * @throws IllegalArgumentException if the entered hex is invalid, it is not
	 * the entered player's turn, or the command can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for selecting
	 * starting hexes
	 */
	public void giveHexToPlayer(ITileProperties hex, int playerNumber){
		SetupPhaseValidator.validateCanGiveHexToPlayer(hex, playerNumber, getCurrentState());
		makeHexOwnedByPlayer(hex,playerNumber);
		
		if(getCurrentState().getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && getCurrentState().getPlayers().size() == 2)
		{
			pickSecondPlayersHex();
		}
		else if(getCurrentState().getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && getCurrentState().getPlayers().size() == 4 && getCurrentState().getActivePhasePlayer().getID() == getCurrentState().getPlayerOrder().get(2))
		{
			//pick the last hex for player 4 automatically
			HashSet<ITileProperties> startingHexes = new HashSet<ITileProperties>();
			for(Point p : Constants.getValidStartingHexes(4))
			{
				startingHexes.add(getCurrentState().getBoard().getHexByXY(p.x, p.y).getHex());
			}
			
			for(ITileProperties tp : startingHexes)
			{
				boolean isOwned = false;
				for(Player p : getCurrentState().getPlayers())
				{
					if(p.ownsHex(tp))
					{
						isOwned = true;
						break;
					}
				}
				if(!isOwned)
				{
					makeHexOwnedByPlayer(tp,getCurrentState().getPlayerOrder().get(3));
					advanceActivePhasePlayer();
					break;
				}
			}
		}
		advanceActivePhasePlayer();
	}

	/**
	 * Call this method to swap a sea hex from the board with a random one from the bank
	 * @param hex The sea hex to swap
	 * @param playerNumber The player who sent the command
	 * @throws NoMoreTilesException If there are no more tiles left in the bank
	 * @throws IllegalArgumentException If hex is null, or is not a sea hex, or if the 
	 * sea hex can not be exchanged according to game rules
	 * @throws IllegalStateException If it is nor the proper phase for exchanging sea hexes
	 */
	public void exchangeSeaHex(ITileProperties hex, int playerNumber) throws NoMoreTilesException{
		SetupPhaseValidator.validateCanExchangeSeaHex(hex, playerNumber, getCurrentState());
		makeSeaHexExchanged(hex, playerNumber);
	}

	private void applyRollEffects()
	{
		ArrayList<Roll> handledRolls = new ArrayList<Roll>();
		for(Roll r : getCurrentState().getFinishedRolls())
		{
			switch(r.getRollReason())
			{
				case DETERMINE_PLAYER_ORDER:
					handledRolls.add(r);
					break;
				default:
					break;
				
			}
		}
		
		ArrayList<Roll> tiedRolls = new ArrayList<>();
		for(int i=0; i<handledRolls.size(); i++)
		{
			Roll nextRoll = handledRolls.get(i);
			for(int j=i+1; j<handledRolls.size(); j++)
			{
				Roll compareRoll = handledRolls.get(j);
				int totalFirstRoll = nextRoll.getFinalTotal();
				int totalSecondRoll = compareRoll.getFinalTotal();
				if(totalFirstRoll == totalSecondRoll)
				{
					tiedRolls.add(nextRoll);
					tiedRolls.add(compareRoll);
					getCurrentState().removeDoneRollTie( nextRoll.getRollingPlayerID());
					getCurrentState().removeDoneRollTie( compareRoll.getRollingPlayerID());
					new CommandRejected( getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(), getCurrentState().getPlayerByPlayerNumber( nextRoll.getRollingPlayerID()).getPlayerInfo(), "Tie", UpdateInstruction.TieRoll).postNetworkEvent( nextRoll.getRollingPlayerID());
					new CommandRejected( getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(), getCurrentState().getPlayerByPlayerNumber( compareRoll.getRollingPlayerID()).getPlayerInfo(), "Tie", UpdateInstruction.TieRoll).postNetworkEvent( compareRoll.getRollingPlayerID());
				}
			}
		}
		
		if(handledRolls.size()>0)
		{
			if(tiedRolls.size() == 0)
			{
				ArrayList<Integer> playerOrder = new ArrayList<Integer>();
				while(handledRolls.size() > 0)
				{
					int maxRoll = Integer.MIN_VALUE;
					for(Roll r : handledRolls)
					{
						maxRoll = Math.max(maxRoll, r.getFinalTotal());
					}
					Roll highestRoll = null;
					for(Roll r : handledRolls)
					{
						if(r.getFinalTotal() == maxRoll)
						{
							highestRoll = r;
							break;
						}
					}
					handledRolls.remove(highestRoll);
					getCurrentState().removeRoll(highestRoll);
					playerOrder.add(highestRoll.getRollingPlayerID());
				}
				getCurrentState().setPlayerOrder(playerOrder);
				getCurrentState().setActivePhasePlayer(getCurrentState().getPlayerOrder().get(0));
				getCurrentState().setActiveTurnPlayer(getCurrentState().getPlayerOrder().get(0));
				getCurrentState().setCurrentSetupPhase(SetupPhase.PICK_FIRST_HEX);
				new CurrentPhase<SetupPhase>( getCurrentState().getPlayerInfoArray(), getCurrentState().getCurrentSetupPhase()).postNetworkEvent( ALL_PLAYERS_ID);
			}
			else
			{
				for(Roll tie : tiedRolls)
				{
					getCurrentState().removeRoll(tie);
					getCurrentState().addNeededRoll(new Roll(tie.getDiceCount(), tie.getRollTarget(), tie.getRollReason(), tie.getRollingPlayerID()));
				}
			}
		}
	}

	/**
	 * In a 2 player game the second player must start opposite of the first
	 */
	private void pickSecondPlayersHex()
	{
		Point firstHexLocation = null;
		int firstPlayerNumber = -1;
		
		for(Player p : getCurrentState().getPlayers())
		{
			if(!p.getOwnedHexes().isEmpty())
			{
				firstPlayerNumber = p.getID();
				ITileProperties firstHex = p.getOwnedHexes().iterator().next();
				firstHexLocation = getCurrentState().getBoard().getXYCoordinatesOfHex(firstHex);
			}
		}
		
		int offsetX = 2 - firstHexLocation.x;
		int offsetY = 4 - firstHexLocation.y;
		
		ITileProperties hex = getCurrentState().getBoard().getHexByXY(2 + offsetX, 4 + offsetY).getHex();
		SetupPhaseValidator.validateIsHexStartingPosition(hex,getCurrentState());
		
		int secondPlayerNumber = -1;

		for(Player p : getCurrentState().getPlayers())
		{
			if(p.getID() != firstPlayerNumber)
			{
				secondPlayerNumber = p.getID();
				break;
			}
		}
		
		makeHexOwnedByPlayer(hex,secondPlayerNumber);
	}

	private void makeSeaHexExchanged(ITileProperties hex, int playerNumber) throws NoMoreTilesException
	{
		getCurrentState().getBoardGenerator().placeHexAside(hex);
		ITileProperties replacement = getCurrentState().getBank().drawTile();
		
		for(HexState hs : getCurrentState().getBoard().getHexesAsList())
		{
			if(hs.getHex().equals(hex))
			{
				hs.setHex(replacement);
				hs.setMarker( Constants.getPlayerMarker( playerNumber));
				ExchangedSeaHex msg = new ExchangedSeaHex(hs);
				msg.postNetworkEvent(ALL_PLAYERS_ID);
				break;
			}
		}
	}

	@Subscribe
	public void receiveApplyRoll(DiceRolled event)
	{
		try
		{
			applyRollEffects();
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process DiceRolled due to: ", t);
		}
	}

	@Subscribe
	public void recieveGameStartCommand(StartSetupPhaseCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				startNewGame(command.getDemoMode(), command.getPlayers());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process StartGameCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage()).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
	
	@Subscribe
	public void recieveExchangeSeaHexCommand(ExchangeSeaHexCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				exchangeSeaHex(command.getHex(), command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ExchangeSeaHexCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),UpdateInstruction.SeaHexChanged).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}

	@Subscribe
	public void recieveGiveHexToPlayerCommand(GiveHexToPlayerCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				giveHexToPlayer(command.getHex(), command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process GiveHexToPlayerCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),UpdateInstruction.HexOwnership).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
}

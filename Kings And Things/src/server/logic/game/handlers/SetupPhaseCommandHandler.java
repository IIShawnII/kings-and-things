package server.logic.game.handlers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import server.event.DiceRolled;
import server.event.GameStarted;
import server.event.commands.ExchangeSeaHexCommand;
import server.event.commands.GiveHexToPlayerCommand;
import server.event.commands.StartGameCommand;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.BoardGenerator;
import server.logic.game.CupManager;
import server.logic.game.GameState;
import server.logic.game.HexBoard;
import server.logic.game.HexTileManager;
import server.logic.game.Player;
import server.logic.game.Roll;
import server.logic.game.validators.SetupPhaseValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.Logger;
import common.event.notifications.Flip;
import common.event.notifications.HexPlacement;
import common.game.HexState;
import common.game.TileProperties;

public class SetupPhaseCommandHandler extends CommandHandler
{
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
		boolean isDemoMode = demoMode;
		CupManager cup = new CupManager(demoMode);
		HexTileManager bank = new HexTileManager(demoMode);
		BoardGenerator boardGenerator = new BoardGenerator(players.size(),bank);
		HexBoard board = boardGenerator.createNewBoard();
		HexPlacement placement = new HexPlacement( Constants.MAX_HEXES);
		board.fillArray( placement.getArray());
		placement.postNotification();
		List<Integer> playerOrder = determinePlayerOrder(players,demoMode);
		//TODO handle dice rolls for player order
		GameState currentState = new GameState(board,players,playerOrder,SetupPhase.PICK_FIRST_HEX, RegularPhase.RECRUITING_CHARACTERS,playerOrder.get(0),playerOrder.get(0), CombatPhase.NO_COMBAT, -1, null);
		new Flip().postNotification();
		
		new GameStarted(isDemoMode, cup, bank, boardGenerator, currentState).postCommand();
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
	public void giveHexToPlayer(TileProperties hex, int playerNumber){
		SetupPhaseValidator.validateCanGiveHexToPlayer(hex, playerNumber, getCurrentState());
		makeHexOwnedByPlayer(hex,playerNumber);
		
		if(getCurrentState().getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && getCurrentState().getPlayers().size() == 2)
		{
			pickSecondPlayersHex();
		}
		else if(getCurrentState().getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && getCurrentState().getPlayers().size() == 4 && getCurrentState().getActivePhasePlayer().getID() == getCurrentState().getPlayerOrder().get(2))
		{
			//pick the last hex for player 4 automatically
			HashSet<TileProperties> startingHexes = new HashSet<TileProperties>();
			for(Point p : Constants.getValidStartingHexes(4))
			{
				startingHexes.add(getCurrentState().getBoard().getHexByXY(p.x, p.y).getHex());
			}
			
			for(TileProperties tp : startingHexes)
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
	public void exchangeSeaHex(TileProperties hex, int playerNumber) throws NoMoreTilesException{
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
					//TODO handle
					break;
				default:
					break;
				
			}
		}
		
		for(Roll r : handledRolls)
		{
			getCurrentState().removeRoll(r);
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
				TileProperties firstHex = p.getOwnedHexes().iterator().next();
				firstHexLocation = getCurrentState().getBoard().getXYCoordinatesOfHex(firstHex);
			}
		}
		
		int offsetX = 2 - firstHexLocation.x;
		int offsetY = 4 - firstHexLocation.y;
		
		TileProperties hex = getCurrentState().getBoard().getHexByXY(2 + offsetX, 4 + offsetY).getHex();
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

	private List<Integer> determinePlayerOrder(Set<Player> playersIn, boolean demoMode)
	{
		ArrayList<Player> players = new ArrayList<Player>();
		for(Player p : playersIn)
		{
			players.add(p);
		}
		ArrayList<Integer> playerOrder = new ArrayList<Integer>(players.size());
		
		while(players.size()>1)
		{
			if(!demoMode)
			{
				int nextPlayerIndex = (int) Math.round(Math.random() * (players.size()-1));
				playerOrder.add(players.remove(nextPlayerIndex).getID());
			}
			else
			{
				int nextPlayerNumber = Integer.MAX_VALUE;
				for(Player p : players)
				{
					nextPlayerNumber = Math.min(nextPlayerNumber, p.getID());
				}
				Iterator<Player> it = players.iterator();
				while(it.hasNext())
				{
					Player nextPlayer = it.next();
					if(nextPlayer.getID() == nextPlayerNumber)
					{
						it.remove();
						playerOrder.add(nextPlayerNumber);
						break;
					}
				}
			}
		}
		playerOrder.add(players.get(0).getID());
		return Collections.unmodifiableList(playerOrder);
	}

	private void makeSeaHexExchanged(TileProperties hex, int playerNumber) throws NoMoreTilesException
	{
		getBoardGenerator().placeHexAside(hex);
		TileProperties replacement = getBank().drawTile();
		
		for(HexState hs : getCurrentState().getBoard().getHexesAsList())
		{
			if(hs.getHex().equals(hex))
			{
				hs.setHex(replacement);
				break;
			}
		}
	}

	@Subscribe
	public void receiveApplyRollCommand(DiceRolled event)
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
	public void recieveGameStartCommand(StartGameCommand command)
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
				exchangeSeaHex(command.getHex(), command.getPlayerID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ExchangeSeaHexCommand due to: ", t);
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
				giveHexToPlayer(command.getHex(), command.getPlayerID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process GiveHexToPlayerCommand due to: ", t);
			}
		}
	}
}

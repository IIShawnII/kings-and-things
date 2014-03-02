package server.logic.game.validators;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.GameState;
import server.logic.game.Player;

import common.Constants;
import common.Constants.Biome;
import common.game.HexState;
import common.game.ITileProperties;

public abstract class SetupPhaseValidator
{
	/**
	 * Use this method to validate the start new game commands
	 * @param demoMode Set to true to stack the deck to match with the demo script
	 * @param players The players who will be playing this game
	 * @throws NoMoreTilesException If there are not enough tiles left in the bank
	 * to set up another board
	 * @throws IllegalArgumentException if the entered list of players is invalid
	 */
	public static void validateStartNewGame(boolean demoMode, Set<Player> players)
	{
		CommandValidator.validateCollection(players,"players");
		if( !Constants.BYPASS_MIN_PLAYER && (players.size() < Constants.MIN_PLAYERS || Constants.MAX_PLAYERS < players.size()))
		{
			throw new IllegalArgumentException("Can only start a game with 2 to 4 players");
		}
	}

	/**
	 * Use this method to validate the give hex to player command
	 * @param hex The hex to change control of
	 * @param playerNumber The player sending the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException if the entered hex is invalid, it is not
	 * the entered player's turn, or the command can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for selecting
	 * starting hexes
	 */
	public static void validateCanGiveHexToPlayer(ITileProperties hex, int playerNumber, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber, currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		switch(currentState.getCurrentSetupPhase())
		{
			case PICK_FIRST_HEX:
			{
				validateIsHexStartingPosition(hex, currentState);
				break;
			}
			case PICK_SECOND_HEX:
			case PICK_THIRD_HEX:
			{
				validateCanPickSetupPhaseHex(hex, playerNumber, currentState);
				break;
			}
			default:
			{
				throw new IllegalStateException("Can not give hexes to players during the " + currentState.getCurrentSetupPhase() + " phase.");
			}
		}
	}

	/**
	 * Call this method to validate the swap sea hex command
	 * @param hex The sea hex to swap
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws NoMoreTilesException If there are no more tiles left in the bank
	 * @throws IllegalArgumentException If hex is null, or is not a sea hex, or if the 
	 * sea hex can not be exchanged according to game rules
	 * @throws IllegalStateException If it is nor the proper phase for exchanging sea hexes
	 */
	public static void validateCanExchangeSeaHex(ITileProperties hex, int playerNumber, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		if(hex == null)
		{
			throw new IllegalArgumentException("The entered tile must not be null.");
		}
		if(!hex.isHexTile())
		{
			throw new IllegalArgumentException("Can only exchange hex tiles.");
		}
		if(Biome.valueOf(hex.getName()) != Biome.Sea)
		{
			throw new IllegalArgumentException("Can only exchange sea hexes.");
		}
		
		ITileProperties startingHex = currentState.getPlayerByPlayerNumber(playerNumber).getOwnedHexes().iterator().next();
		List<HexState> adjacentHexes = currentState.getBoard().getAdjacentHexesTo(startingHex);
		
		HexState hexState = currentState.getBoard().getHexStateForHex(hex);
		
		int numSeaHexes = 0;
		for(HexState hs : adjacentHexes)
		{
			if(Biome.valueOf(hs.getHex().getName()) == Biome.Sea)
			{
				numSeaHexes++;
			}
		}
		if(!startingHex.equals(hex) && (numSeaHexes < 2 || !adjacentHexes.contains(hexState)))
		{
			throw new IllegalArgumentException("Can only exchange sea hexes on player starting position or adjacent to starting position when there are 2 or more sea hexes next to starting position.");
		}

		switch(currentState.getCurrentSetupPhase())
		{
			case EXCHANGE_SEA_HEXES:
			{
				break;
			}
			default:
			{
				throw new IllegalStateException("Can not exchange sea hexes during the " + currentState.getCurrentSetupPhase() + " phase.");
			}
		}
	}

	/**
	 * Use this method to check if a hex is one of the valid starting position choices
	 * @param hex The hex to check
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If hex is null, or is not a valid starting position
	 */
	public static void validateIsHexStartingPosition(ITileProperties hex, GameState currentState)
	{
		Point desiredHex = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HashSet<Point> validChoices = Constants.getValidStartingHexes(currentState.getPlayers().size());
		if(!validChoices.contains(desiredHex))
		{
			throw new IllegalArgumentException("The chosen hex was not a starting hex.");
		}
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsHex(hex))
			{
				throw new IllegalArgumentException("The chosen hex is already taken by player: " + p);
			}
		}
	}

	private static void validateCanPickSetupPhaseHex(ITileProperties hex, int playerNumber, GameState currentState)
	{
		boolean playerHasOneAdjacentHex = false;
		for(HexState hs : currentState.getBoard().getAdjacentHexesTo(hex))
		{
			for(Player p : currentState.getPlayers())
			{
				if(p.ownsHex(hs.getHex()))
				{
					if(p.getID() == playerNumber)
					{
						playerHasOneAdjacentHex = true;
					}
					else
					{
						throw new IllegalArgumentException("The chosen hex is adjacent to one of player: " + p + "'s hexes");
					}
				}
			}
		}
		if(!playerHasOneAdjacentHex)
		{
			throw new IllegalArgumentException("The chosen hex must be adjacent to a currently owned hex.");
		}
	}
}

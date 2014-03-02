package server.logic.game.validators;

import java.util.Collection;
import java.util.Set;

import server.logic.game.GameState;
import server.logic.game.Player;

import common.Constants;
import common.Constants.Building;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Roll;

/**
 * This class checks requested commands against game states and throws appropriate
 * exceptions if the command cannot be achieved, clients can use this to block
 * commands before they are rejected by the server.
 */
public abstract class CommandValidator
{
	/**
	 * Call this to validate the roll dice command
	 * @param reasonForRoll The reason this roll is being done
	 * @param playerNumber The player who sent the command
	 * @param tileToRollFor The tile being rolled for (might be creature, hex, tower etc)
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT
	 */
	public static void validateCanRollDice(RollReason reasonForRoll, int playerNumber, ITileProperties tileToRollFor, GameState currentState)
	{
		boolean rollNeeded = false;
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, reasonForRoll, playerNumber, tileToRollFor))
			{
				rollNeeded = true;
			}
		}
		if(reasonForRoll == RollReason.RECRUIT_SPECIAL_CHARACTER)
		{
			CommandValidator.validateIsPlayerActive(playerNumber, currentState);
			if(currentState.getCurrentRegularPhase() != RegularPhase.RECRUITING_CHARACTERS)
			{
				throw new IllegalArgumentException("Can only roll to recruit special characters during the recruit special characters phase.");
			}
			if(!tileToRollFor.isSpecialCharacter())
			{
				throw new IllegalArgumentException("Must specify a special character to roll for");
			}
		}
		if(!rollNeeded && reasonForRoll != RollReason.ENTERTAINMENT)
		{
			throw new IllegalArgumentException("Not currently waiting for " + reasonForRoll + " type roll from player " + playerNumber + " targeting tile: " + tileToRollFor);
		}
	}

	/**
	 * Call this to validate the end the current players turn command
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 * @throws IllegalStateException If the player can not end their turn
	 */
	public static void validateCanEndPlayerTurn(int playerNumber, GameState currentState)
	{
		CommandValidator.validateNoPendingRolls(currentState);
		if(currentState.getCurrentCombatPhase() == CombatPhase.NO_COMBAT)
		{
			CommandValidator.validateIsPlayerActive(playerNumber, currentState);
		}
		switch(currentState.getCurrentSetupPhase())
		{
			case PICK_FIRST_HEX:
			{
				throw new IllegalStateException("You must select a starting hex.");
			}
			case PICK_SECOND_HEX:
			case PICK_THIRD_HEX:
			{
				throw new IllegalStateException("You must select a hex to take ownership of.");
			}
			case PLACE_FREE_TOWER:
			{
				throw new IllegalStateException("You must select a hex to place your tower in.");
			}
			default:
				break;
		}
		switch(currentState.getCurrentCombatPhase())
		{
			case PLACE_THINGS:
			{
				Player player = currentState.getPlayerByPlayerNumber(playerNumber);
				if(!player.ownsHex(currentState.getCombatHex().getHex()))
				{
					throw new IllegalStateException("Must wait for combat winner to place things on hex");
				}
			}
			case NO_COMBAT:
				break;
			default:
				throw new IllegalStateException("You must resolve the combat in the hex before ending your turn");
			
		}
	}

	/**
	 * Checks to see if the game is waiting for players to roll for something
	 * @param currentState The current state of the game
	 * @throws IllegalStateException If some players still need to roll
	 */
	public static void validateNoPendingRolls(GameState currentState)
	{
		if(currentState.isWaitingForRolls())
		{
			throw new IllegalStateException("Some players must finish rolling dice first");
		}
	}

	/**
	 * Checks to see if the entered player should be taking action now
	 * @param playerNumber The player to check
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If the entered player is not currently
	 * active
	 */
	public static void validateIsPlayerActive(int playerNumber, GameState currentState)
	{
		if(currentState.getActivePhasePlayer().getID() != playerNumber)
		{
			throw new IllegalArgumentException("It is still: " + currentState.getActivePhasePlayer() + "'s turn to move.");
		}
	}
	
	/**
	 * Checks to see if collection, or anything in it, is null
	 * @param things The collection to check
	 * @param typeInCollection The type of data in the collection (for
	 * error message)
	 * @throws IllegalArgumentException If the collection or something inside
	 * of it is null
	 */
	public static void validateCollection(Collection<?> things, String typeInCollection)
	{
		if(things==null)
		{
			throw new IllegalArgumentException("The list of " + typeInCollection + " must not be null");
		}
		for(Object o : things)
		{
			if(o==null)
			{
				throw new IllegalArgumentException("The list of " + typeInCollection + " must not contain null elements");
			}
		}
	}
	
	/**
	 * Checks to make sure the hex creature limit is properly enforced
	 * @param playerNumber The player adding more things to the hex
	 * @param hex The hex to add to
	 * @param currentState The current State of the game
	 * @param toAdd The list of things the player wants to add
	 * @throws IllegalArgumentException If adding the things in toAdd would
	 * exceed the creature limit in the specified hex
	 */
	public static void validateCreatureLimitInHexNotExceeded(int playerNumber, ITileProperties hex, GameState currentState, Collection<ITileProperties> toAdd)
	{
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		HexState hs = currentState.getBoard().getHexStateForHex(hex);
		
		for(ITileProperties thing : toAdd)
		{
			if(thing.isCreature() && !(hs.hasBuilding() && hs.getBuilding().getName().equals(Building.Citadel.name())))
			{
				Set<ITileProperties> existingCreatures = hs.getCreaturesInHex();
				int ownedCreatureCount = 0;
				for(ITileProperties tp : existingCreatures)
				{
					if(player.ownsThingOnBoard(tp))
					{
						ownedCreatureCount++;
					}
				}
				
				if(ownedCreatureCount >= Constants.MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX)
				{
					throw new IllegalArgumentException("Can not place more than " + ownedCreatureCount + " friendly creatures in the same hex, unless it contains a Citadel.");
				}
			}
		}
	}
}

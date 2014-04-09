package server.logic.game.validators;

import java.util.Collection;
import java.util.Set;

import server.logic.game.GameState;
import common.Constants;
import common.Constants.Biome;
import common.Constants.Building;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
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
	 * @param roll The roll parameters
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT or RollReason.RECRUIT_SPECIAL_CHARACTER
	 */
	public static void validateCanRollDice(Roll roll, GameState currentState)
	{
		boolean rollNeeded = false;
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, roll.getRollReason(), roll.getRollingPlayerID(), roll.getRollTarget(), roll.getDiceCount()))
			{
				rollNeeded = true;
			}
		}
		if(roll.getRollReason() == RollReason.RECRUIT_SPECIAL_CHARACTER)
		{
			CommandValidator.validateIsPlayerActive(roll.getRollingPlayerID(), currentState);
			if(currentState.getCurrentRegularPhase() != RegularPhase.RECRUITING_CHARACTERS)
			{
				throw new IllegalArgumentException("Can only roll to recruit special characters during the recruit special characters phase.");
			}
			if(!roll.getRollTarget().isSpecialCharacter())
			{
				throw new IllegalArgumentException("Must specify a special character to roll for");
			}
			
			boolean isTerrainLord = false;
			for(Biome b : Biome.values())
			{
				if(Constants.getTerrainLordNameForBiome(b).equals(roll.getRollTarget().getName()))
				{
					isTerrainLord = true;
					break;
				}
			}
			
			if(isTerrainLord)
			{
				for(ITileProperties thing: currentState.getPlayerByPlayerNumber(roll.getRollingPlayerID()).getOwnedThingsOnBoard())
				{
					for(Biome b : Biome.values())
					{
						if(Constants.getTerrainLordNameForBiome(b).equals(thing.getName()))
						{
							throw new IllegalStateException("Can not recruit more than one terrain lord at a time");
						}
					}
				}
			}
		}
		if(!rollNeeded && roll.getRollReason() != RollReason.ENTERTAINMENT && roll.getRollReason() != RollReason.RECRUIT_SPECIAL_CHARACTER)
		{
			throw new IllegalArgumentException("Not currently waiting for " + roll.getRollReason() + " type roll from player " + roll.getRollingPlayerID() + " targeting tile: " +  roll.getRollTarget());
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
		if(currentState.getPlayerByPlayerNumber(playerNumber).hasCardsInHand())
		{
			throw new IllegalStateException("You must place the cards in your hand on the board, or return them to the cup.");
		}
		if(currentState.getCurrentCombatPhase() == CombatPhase.NO_COMBAT)
		{
			CommandValidator.validateIsPlayerActive(playerNumber, currentState);
		}
		else
		{
			if(currentState.getCombatHex().getThingsInHexOwnedByPlayer(currentState.getPlayerByPlayerNumber(playerNumber)).size()==0
					&& !currentState.getPlayerByPlayerNumber(playerNumber).ownsHex(currentState.getCombatHex().getHex()))
			{
				throw new IllegalStateException("It is not " + currentState.getPlayerByPlayerNumber(playerNumber) + " turn to move.");
			}
			switch(currentState.getCurrentCombatPhase())
			{
				case ATTACKER_ONE_RETREAT:
				{
					if(currentState.getAttackerByIndex(1).getID() != playerNumber)
					{
						throw new IllegalStateException("Player " + currentState.getAttackerByIndex(1) + " must choose to retreat first.");
					}
					break;
				}
				case ATTACKER_TWO_RETREAT:
				{
					if(currentState.getAttackerByIndex(2).getID() != playerNumber)
					{
						throw new IllegalStateException("Player " + currentState.getAttackerByIndex(2) + " must choose to retreat first.");
					}
					break;
				}
				case ATTACKER_THREE_RETREAT:
				{
					if(currentState.getAttackerByIndex(3).getID() != playerNumber)
					{
						throw new IllegalStateException("Player " + currentState.getAttackerByIndex(3) + " must choose to retreat first.");
					}
					break;
				}
				case DEFENDER_RETREAT:
				{
					if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsHex(currentState.getCombatHex().getHex()))
					{
						throw new IllegalStateException("Only the defender may choose to retreat or fight at this time.");
					}
					break;
				}
				case PLACE_THINGS:
				{
					if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsHex(currentState.getCombatHex().getHex()))
					{
						throw new IllegalStateException("Only the hex owner may place things in the hex after combat");
					}
					break;
				}
				default:
					break;
			}
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
	}
	
	public static void validateCanCallBluff(int playerNumber, ITileProperties creature, GameState currentState)
	{
		if(!creature.isFaceUp())
		{
			throw new IllegalStateException("Can not call bluff on face down creatures");
		}
		if(!creature.isCreature())
		{
			throw new IllegalStateException("Can only call bluff on creature tiles");
		}
		boolean owned = false;
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsThingOnBoard(creature))
			{
				owned = true;
			}
			if(p.ownsThingInTray(creature) || p.ownsThingInHand(creature))
			{
				throw new IllegalStateException("Can only call bluff on creatures on the board");
			}
		}
		if(!owned)
		{
			throw new IllegalStateException("Can not call bluff on exploration defenders");
		}
		if(creature.isSpecialCharacter())
		{
			throw new IllegalArgumentException("Can not call bluff on heroes");
		}
	}
	
	public static void validateCanRemoveThingsFromHex(int playerNumber, ITileProperties hexToRemoveFrom, Set<ITileProperties> thingsToRemove, GameState currentState)
	{
		Player p = currentState.getPlayerByPlayerNumber(playerNumber);
		HexState hex = currentState.getBoard().getHexStateForHex(hexToRemoveFrom);
		if(!p.ownsHex(hex.getHex()))
		{
			throw new IllegalArgumentException("You can only remove things from your own hex.");
		}
		//can only remove creatures to satisfy the constraints
		for(ITileProperties tp : thingsToRemove)
		{
			if(!p.ownsThingOnBoard(tp))
			{
				throw new IllegalArgumentException("Can only remove your own things");
			}
		}
		if(thingsToRemove.size()==1 && thingsToRemove.iterator().next().isSpecialIncomeCounter())
		{
			validateIsPlayerActive(playerNumber,currentState);
			if(currentState.getCurrentRegularPhase() == RegularPhase.COMBAT)
			{
				throw new IllegalStateException("You can not remove special income counters from your hexes during the combat phase.");
			}
		}
		else if(thingsToRemove.size()==1 && thingsToRemove.iterator().next().isSpecialCharacter() && currentState.getThingsToRemoveFromHex(hex)==0)
		{
			validateIsPlayerActive(playerNumber,currentState);
		}
		else
		{
			if(currentState.getThingsToRemoveFromHex(hex) < thingsToRemove.size())
			{
				throw new IllegalArgumentException("You only need to discard " + currentState.getThingsToRemoveFromHex(hex) + " things from that hex");
			}
			switch(currentState.getCurrentCombatPhase())
			{
				case ATTACKER_ONE_RETREAT:
				case ATTACKER_TWO_RETREAT:
				case ATTACKER_THREE_RETREAT:
				case DEFENDER_RETREAT:
				{
					//can only remove creatures to satisfy the constraints
					for(ITileProperties tp : thingsToRemove)
					{
						if(!tp.isCreature())
						{
							throw new IllegalArgumentException("Can only remove creatures");
						}
					}
					break;
				}
				default:
					break;
			}
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

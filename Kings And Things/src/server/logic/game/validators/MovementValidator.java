package server.logic.game.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import server.logic.game.GameState;
import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;

public abstract class MovementValidator
{
	/**
	 * Call this method to validate the move command
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game
	 * @param Hexes The list of hexes the player wants to move through
	 * @param Creatures The list of creatures the player wants to move
	 * @throws IllegalStateException If it isn't the movement phase
	 * @throws IllegalArgumentException If the move can't be completed due
	 * to game rules
	 */
	public static void validateCanMove(int playerNumber, GameState currentState, List<ITileProperties> Hexes, Collection<ITileProperties> Creatures) {
		
		// checks if it's player's turn
		CommandValidator.validateIsPlayerActive(playerNumber, currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		CommandValidator.validateCollection(Hexes,"hexes");
		CommandValidator.validateCollection(Creatures,"creatures");
		
		// the following conditional statement checks if it is the movement
		if (currentState.getCurrentSetupPhase() != SetupPhase.SETUP_FINISHED) {
			throw new IllegalStateException("Can't move during the setup phase");
		} else if (currentState.getCurrentRegularPhase() != RegularPhase.MOVEMENT) {
			throw new IllegalStateException("Can't move during the " + currentState.getCurrentRegularPhase() + " phase");
		}
		
		validateMovementConditions(playerNumber,currentState,Hexes,Creatures);
	}

	// private methods
	/*
	 * Checks to see if player can move through hexes
	 */
	private static void validateMovementConditions(int playerNumber, GameState currentState, List<ITileProperties> Hexes, Collection<ITileProperties> Creatures) {
		
		int moveSpeedTotal = 0;
		HashSet<HexState> pathOfHexes = new HashSet<>();
		HexState nextHex = null;

		HexState firstHex = currentState.getBoard().getHexStateForHex(Hexes.get(0));
		
		if(!currentState.getBoard().areHexesConnected(Hexes))
		{
			throw new IllegalArgumentException("You can only move through hexes that are adjacent to each other.");
		}
		
		CommandValidator.validateCreatureLimitInHexNotExceeded(playerNumber, Hexes.get(Hexes.size() - 1), currentState, Creatures);
		
		boolean seaHexesExist = false;
		boolean haveDeerHunter = false;
		for(ITileProperties thing : Creatures)
		{
			if(thing.getName().equals("Deerhunter"))
			{
				haveDeerHunter = true;
			}
		}
		
		boolean notOwnedHexesExist = false;
		
		for (int i = 1; i < Hexes.size(); i++) {
			ITileProperties hex = Hexes.get(i);
			
			if (i < Hexes.size() - 1) {
				boolean hexNotOwned = true;
				nextHex = currentState.getBoard().getHexStateForHex(Hexes.get(i));
				pathOfHexes.add(nextHex);
				for (Player p : currentState.getPlayers()) {
					if (p.ownsHex(hex)) {
						hexNotOwned = false;
					}
					if(p.getID() != playerNumber && nextHex.getFightingThingsInHexOwnedByPlayer(p).size()>0 && !(haveDeerHunter && i==1))
					{
						throw new IllegalArgumentException("Can not move through hexes with enemy counters with combat values");
					}
				}
				if (hexNotOwned) {
					notOwnedHexesExist = true;
				}
			}
			
			moveSpeedTotal += haveDeerHunter? 1 : hex.getMoveSpeed();
			
			if (!hex.isHexTile()) {
				throw new IllegalArgumentException("Can't move through non hexes");
			}
			
			if (Biome.Sea.name().equals(hex.getName())) {
				seaHexesExist = true;
			}
		}

		for (ITileProperties creature : Creatures) {
			if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsThingOnBoard(creature))
			{
				throw new IllegalArgumentException("You can only move your own creatures");
			}
			if (!creature.isCreature()) {
				throw new IllegalArgumentException("You can only move creatures");
			}
			if (creature.getMoveSpeed() < moveSpeedTotal) {
				throw new IllegalArgumentException("Creature cannot move that far");
			}
			if (!creature.isSpecialCreatureWithAbility(Ability.Fly) && seaHexesExist) {
				throw new IllegalArgumentException("Can't move through sea hexes");
			}
			if (!creature.isSpecialCreatureWithAbility(Ability.Fly) && notOwnedHexesExist) {
				throw new IllegalArgumentException("Can't move through unexplored hexes");
			}
			if(!firstHex.getThingsInHex().contains(creature))
			{
				throw new IllegalArgumentException("Can only move creatures contained in the first hex of the movement");
			}
		}
		
		List<Player> players = new ArrayList<>();
		
		for (Player player : currentState.getPlayers()) {
			if (player.getID() != playerNumber) {
				players.add(player);
			}
		}
		
		pathOfHexes.add(firstHex);
		Player playerMoving = currentState.getPlayerByPlayerNumber(playerNumber);
		
		HashSet<ITileProperties> thingsInHex = new HashSet<>();
		for (HexState newHex : pathOfHexes) {
			thingsInHex.clear();
			thingsInHex.addAll(newHex.getCreaturesInHex());
			
			if (newHex.hasBuilding()) {
				thingsInHex.add(newHex.getBuilding());
			}
		
			for (ITileProperties creature : thingsInHex) {
				if (!playerMoving.ownsThingOnBoard(creature)) {
					throw new IllegalArgumentException("Can't move out of hex with enemy creatures");
				}
			}
		}
		
		if (Biome.Sea.name().equals(Hexes.get(Hexes.size() - 1).getName())) {
			throw new IllegalArgumentException("Can't end movement on sea hex");
		}
	}
}

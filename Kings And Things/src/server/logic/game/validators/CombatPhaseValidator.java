package server.logic.game.validators;

import java.util.HashSet;
import java.util.Set;

import server.logic.game.GameState;
import common.Constants.Ability;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;

public abstract class CombatPhaseValidator
{
	/**
	 * Call this to validate the resolve combat in hex command
	 * @param hex The hex to resolve
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If parameters are invalid, or
	 * if combat in hex can not be resolved according to game rules
	 * @throws IllegalStateException If it is not the combat phase,
	 * or if another combat is already being resolved
	 */
	public static void validateCanResolveCombat(ITileProperties hex, int playerNumber, GameState currentState)
	{
		CommandValidator.validateNoPendingRolls(currentState);
		CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		if(currentState.getCurrentCombatPhase() != CombatPhase.NO_COMBAT)
		{
			throw new IllegalStateException("Must resolve existing combat before starting a new one.");
		}
		if(currentState.getCurrentRegularPhase() != RegularPhase.COMBAT)
		{
			throw new IllegalStateException("Can only resolve combat during the combat phase");
		}
		HexState combatHex = currentState.getBoard().getHexStateForHex(hex);
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		if(!player.ownsHex(hex) && combatHex.getThingsInHexOwnedByPlayer(player).size() == 0)
		{
			throw new IllegalArgumentException("Can only resolve combat in a hex that involves the player");
		}
		boolean otherPlayersOwnThingsInHex = false;
		
		for(Player p : currentState.getPlayers())
		{
			if(!p.equals(player) && combatHex.getThingsInHexOwnedByPlayer(p).size()!=0)
			{
				otherPlayersOwnThingsInHex = true;
				break;
			}
		}
		
		if(player.ownsHex(hex) && !otherPlayersOwnThingsInHex)
		{
			throw new IllegalArgumentException("The entered hex is not a combat hex");
		}
	}
	
	public static void validateCanBribeDefender(ITileProperties defender, int playerNumber, GameState currentState)
	{
		CommandValidator.validateNoPendingRolls(currentState);
		
		Set<ITileProperties> explorationDefenders = currentState.getCombatHex().getFightingThingsInHexNotOwnedByPlayers(currentState.getPlayers());
		HashSet<Player> playersInCombat = currentState.getPlayersStillFightingInCombatHex();
		if(!playersInCombat.contains(currentState.getPlayerByPlayerNumber(playerNumber)))
		{
			throw new IllegalArgumentException("The entered player number is not involved in combat.");
		}
		if(explorationDefenders.size()==0)
		{
			throw new IllegalArgumentException("There are no exploration defenders to bribe");
		}
		if(currentState.getCurrentCombatPhase() != CombatPhase.BRIBE_CREATURES)
		{
			throw new IllegalStateException("Can not bribe creatures during the " + currentState.getCurrentCombatPhase() + " phase");
		}
		if(!explorationDefenders.contains(defender))
		{
			throw new IllegalArgumentException("Can only bribe exploration defenders");
		}
		if(!defender.isCreature() && !defender.isBuilding())
		{
			throw new IllegalArgumentException("Can only bribe creatures and buildings");
		}
		boolean highAmount = false;
		
		for(ITileProperties thing : currentState.getCombatHex().getThingsInHex())
		{
			if(thing.isTreasure() || thing.isSpecialIncomeCounter() || thing.isMagicItem())
			{
				highAmount = true;
				break;
			}
		}
		
		int multiplier = highAmount? 2 : 1;
		int goldAmount = multiplier * defender.getValue();
		
		if(currentState.getPlayerByPlayerNumber(playerNumber).getGold() < goldAmount)
		{
			throw new IllegalStateException("You don't have enough gold for that");
		}
	}
	
	public static void validateCanTargetPlayer(int playerNumber, int targetPlayerID, GameState currentState)
	{
		CommandValidator.validateNoPendingRolls(currentState);
		
		HashSet<Player> playersInCombat = currentState.getPlayersStillFightingInCombatHex();
		
		if(!playersInCombat.contains(currentState.getPlayerByPlayerNumber(playerNumber)))
		{
			throw new IllegalArgumentException("The entered player number is not involved in combat.");
		}
		if(!playersInCombat.contains(currentState.getPlayerByPlayerNumber(targetPlayerID)))
		{
			throw new IllegalArgumentException("The entered target player is not involved in combat.");
		}
		if(currentState.getPlayersTarget(playerNumber) != null)
		{
			throw new IllegalStateException("The entered player has already chosen a target: " + currentState.getPlayerByPlayerNumber(targetPlayerID));
		}
	}
	
	public static void validateCanRetreatFromCombat(int playerNumber, ITileProperties destinationHex, GameState currentState)
	{
		CommandValidator.validateNoPendingRolls(currentState);

		HashSet<Player> playersInCombat = currentState.getPlayersStillFightingInCombatHex();
		
		if(!playersInCombat.contains(currentState.getPlayerByPlayerNumber(playerNumber)))
		{
			throw new IllegalArgumentException("The entered player number is not involved in combat.");
		}
		boolean adjacentHexFound = false;
		for(HexState hs : currentState.getBoard().getAdjacentHexesTo(currentState.getCombatHex().getHex()))
		{
			if(hs.getHex().equals(destinationHex))
			{
				adjacentHexFound = true;
				break;
			}
		}
		if(!adjacentHexFound)
		{
			throw new IllegalArgumentException("Can only retreat to an adjacent hex.");
		}
		if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsHex(destinationHex) || currentState.getBoard().getContestedHexes(playersInCombat).contains(currentState.getBoard().getHexStateForHex(destinationHex)))
		{
			throw new IllegalArgumentException("Can only retreat to a friendly non-combat hex.");
		}
		if(playerNumber == currentState.getDefendingPlayerNumber() && currentState.getCombatHex().getFightingThingsInHexNotOwnedByPlayers(currentState.getPlayers()).size()>0)
		{
			throw new IllegalArgumentException("Exploration defenders can not retreat");
		}
	}

	/**
	 * Call this to validate the apply hits command
	 * @param thing The thing to apply hits to
	 * @param playerNumber The player who sent the command
	 * @param hitCount The number of hits to apply
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If hits can not be applied according to game rules
	 */
	public static void validateCanApplyHits(ITileProperties thing, int playerNumber, int hitCount, GameState currentState)
	{
		CommandValidator.validateNoPendingRolls(currentState);
		if(currentState.getCurrentCombatPhase() != CombatPhase.APPLY_MAGIC_HITS && currentState.getCurrentCombatPhase() != CombatPhase.APPLY_MELEE_HITS
				&& currentState.getCurrentCombatPhase() != CombatPhase.APPLY_RANGED_HITS)
		{
			throw new IllegalStateException("Can not apply hits to creatures during the: " + currentState.getCurrentCombatPhase() + " combat phase.");
		}
		if(currentState.getHitsOnPlayer(playerNumber) < hitCount)
		{
			throw new IllegalArgumentException("You do not need to apply that many hits to your creatures");
		}
		if(!currentState.getCombatHex().getFightingThingsInHex().contains(thing))
		{
			throw new IllegalArgumentException("Can only apply hits to things in the combat hex");
		}
		if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsThingOnBoard(thing))
		{
			if(currentState.getDefendingPlayerNumber()!=playerNumber || !currentState.getCombatHex().getFightingThingsInHexNotOwnedByPlayers(currentState.getPlayers()).contains(thing))
			{
				throw new IllegalArgumentException("Can only apply hits to your own things");
			}
		}
		int absorbtionValue = thing.hasAbility(Ability.Armor)? thing.getValue() : 1;
		if(hitCount > absorbtionValue)
		{
			throw new IllegalArgumentException(thing.getName() + " can not take " + hitCount + " hits");
		}
	}
}

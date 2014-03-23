package server.logic.game.validators;

import java.util.ArrayList;
import java.util.Collection;

import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.GameState;
import server.logic.game.Player;

import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.ITileProperties;

public abstract class RecruitingThingsPhaseValidator
{
	/**
	 * Use this method to validate the exchange things command.
	 * @param things The things the player wants to exchange, will be placed back in the cup
	 * only AFTER replacements are drawn
	 * @param playerNumber The player sending the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws NoMoreTilesException If there are no more tiles left in the cup
	 * @throws IllegalArgumentException If it is not the entered player's turn, or if the collection
	 * of things is invalid
	 * @throws IllegalStateException If it is nor the proper phase for exchanging things
	 */
	public static void validateCanExchangeThings(Collection<ITileProperties> things, int playerNumber, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		CommandValidator.validateCollection(things,"things");
		
		SetupPhase sp = currentState.getCurrentSetupPhase();
		RegularPhase rp = currentState.getCurrentRegularPhase();
		if(sp != SetupPhase.EXCHANGE_THINGS && rp != RegularPhase.RECRUITING_THINGS)
		{
			throw new IllegalArgumentException("Can not exchange things during the " + (sp==SetupPhase.SETUP_FINISHED? rp : sp) + " phase");
		}
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		for(ITileProperties tp : things)
		{
			if(!player.ownsThingInTray(tp))
			{
				throw new IllegalArgumentException("Can not exchange something not in your tray");
			}
		}
		if(sp == SetupPhase.SETUP_FINISHED && things.size() % 2 > 0)
		{
			throw new IllegalArgumentException("Can only exchange things in twos");
		}
	}

	public static void validateCanDiscardThings(Collection<ITileProperties> things, int playerNumber, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		Player p = currentState.getPlayerByPlayerNumber(playerNumber);
		for(ITileProperties thing : things)
		{
			if(!p.ownsThingInHand(thing) && !p.ownsThingInTray(thing))
			{
				throw new IllegalArgumentException("Can only discard things from your hand or your tray.");
			}
		}
	}
	
	/**
	 * Call this method to validate the paid recruits command
	 * @param amountToSpend The amount of gold the player wants to spend on recruits
	 * @param playerNumber The player sending the command
	 * @param currentState The current state of the game
	 * @throws IllegalStateException If it is not the right phase for purchasing recruits
	 * @throws IllegalArgumentException if the command is invalid
	 */
	public static void validateCanPurchaseRecruits(int amountToSpend, int playerNumber, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		RegularPhase regularPhase = currentState.getCurrentRegularPhase();
		if(regularPhase != RegularPhase.RECRUITING_THINGS)
		{
			throw new IllegalStateException("Can not purchase recruits during the " + regularPhase + " phase");
		}
		if(amountToSpend < 0)
		{
			throw new IllegalArgumentException("The amount of gold being spent must be positive");
		}
		if(amountToSpend > 25)
		{
			throw new IllegalArgumentException("Can not purchase more than 5 extra recruits");
		}
		if(amountToSpend > currentState.getPlayerByPlayerNumber(playerNumber).getGold())
		{
			throw new IllegalArgumentException("Can not spend more than the player's total gold amount");
		}
		if(amountToSpend % 5 > 0)
		{
			throw new IllegalArgumentException("Gold amount must be divisible by 5");
		}
	}

	/**
	 * Use this method to validate the place thing on board command
	 * @param thing The thing to place on the board
	 * @param playerNumber The player sending the command
	 * @param hex The hex to place the thing on
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If it is not the entered player's turn, if the
	 * entered thing or hex tile is invalid, or if placement can not be made due to game
	 * rules
	 * @throws IllegalStateException If it is not the right phase for placing things on
	 * the board
	 */
	public static void validateCanPlaceThingOnBoard(final ITileProperties thing, int playerNumber, ITileProperties hex, GameState currentState)
	{
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		CombatPhase combatPhase = currentState.getCurrentCombatPhase();
		if(combatPhase != CombatPhase.PLACE_THINGS)
		{
			CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		}
		else if(!currentState.getCombatHex().getHex().equals(hex))
		{
			throw new IllegalArgumentException("Can only place things on the newly acquired hex");
		}
		
		CommandValidator.validateNoPendingRolls(currentState);
		SetupPhase setupPhase = currentState.getCurrentSetupPhase();
		RegularPhase regularPhase = currentState.getCurrentRegularPhase();
		if(setupPhase != SetupPhase.PLACE_FREE_THINGS && setupPhase != SetupPhase.PLACE_EXCHANGED_THINGS && regularPhase != RegularPhase.RECRUITING_THINGS && combatPhase != CombatPhase.PLACE_THINGS
				&& regularPhase != RegularPhase.RECRUITING_CHARACTERS)
		{
			throw new IllegalStateException("Can not place things on the board during the " + (setupPhase==SetupPhase.SETUP_FINISHED? regularPhase : setupPhase) + " phase");
		}
		HexState hs = currentState.getBoard().getHexStateForHex(hex);
		if(!player.ownsHex(hex))
		{
			throw new IllegalArgumentException("Can not place things onto someone else's hex");
		}
		if(!player.ownsThingInTray(thing) && !player.ownsThingInHand(thing))
		{
			throw new IllegalArgumentException("Can only place things that the player owns in their tray or in their hand");
		}
		
		ArrayList<ITileProperties> stuff = new ArrayList<>();
		stuff.add(thing);
		CommandValidator.validateCreatureLimitInHexNotExceeded(playerNumber,hex,currentState,stuff);
		
		hs.validateCanAddThingToHex(thing,true);
	}
}

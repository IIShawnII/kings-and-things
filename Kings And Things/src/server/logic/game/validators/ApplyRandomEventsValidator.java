package server.logic.game.validators;

import server.logic.game.GameState;
import common.Constants.RegularPhase;
import common.game.ITileProperties;

public class ApplyRandomEventsValidator
{
	public static void validateCanPlayRandomEvent(int playerNumber, ITileProperties event, ITileProperties target, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber, currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		if(currentState.getCurrentRegularPhase() != RegularPhase.RANDOM_EVENTS)
		{
			throw new IllegalArgumentException("Can only play random events during the random events phase");
		}
		if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsTile(event))
		{
			throw new IllegalArgumentException("Can only play random events that you own");
		}
		if(!event.isEvent())
		{
			throw new IllegalArgumentException("Can only play random events during this phase");
		}
	}
}

package server.logic.game.validators;

import server.logic.game.GameState;
import server.logic.game.Player;
import common.game.ITileProperties;

public class RecruitSpecialCharacterValidator {

	public static void validateCanModifySpecialCharacterRoll(ITileProperties target, int playerNumber, int goldAmount, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber, currentState);
		if(target == null)
		{
			throw new IllegalArgumentException("Must specify which special character the roll modification is for.");
		}
		if(!target.isSpecialCharacter())
		{
			throw new IllegalArgumentException("Can only recruit special characters in this manner.");
		}
		if(!currentState.getBankHeroes().heroIsAvailable(target.getName()))
		{
			throw new IllegalStateException("The entered hero is not available for hire.");
		}
		Player p = currentState.getPlayerByPlayerNumber(playerNumber);
		if(p.getGold() < goldAmount)
		{
			throw new IllegalArgumentException("The player does not have enough gold.");
		}
		if(goldAmount < 0)
		{
			throw new IllegalArgumentException("Must enter a positive gold amount.");
		}
		
		if(!currentState.hasRecordedRollForSpecialCharacter())
		{
			if(goldAmount % 5 !=0)
			{
				throw new IllegalArgumentException("The entered gold amount must be a multiple of 5.");
			}
		}
		else
		{
			if(goldAmount % 10 != 0)
			{
				throw new IllegalArgumentException("The entered gold amount must be divisible by 10.");
			}
		}
	}
}

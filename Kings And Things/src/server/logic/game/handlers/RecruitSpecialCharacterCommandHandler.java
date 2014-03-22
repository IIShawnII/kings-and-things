package server.logic.game.handlers;

import java.util.ArrayList;

import server.event.DiceRolled;
import server.event.internal.ModifyRollForSpecialCharacter;
import server.logic.game.Player;
import server.logic.game.RollModification;
import server.logic.game.validators.RecruitSpecialCharacterValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants.RollReason;
import common.Logger;
import common.game.ITileProperties;
import common.game.Roll;

public class RecruitSpecialCharacterCommandHandler extends CommandHandler
{
	public void handleSpecialCharacterRollModification(ITileProperties target, int playerNumber, int goldAmount)
	{
		RecruitSpecialCharacterValidator.validateCanModifySpecialCharacterRoll(target, playerNumber, goldAmount, getCurrentState());
		int modificationAmount = 0;
		if(getCurrentState().hasRecordedRollForSpecialCharacter())
		{
			modificationAmount = goldAmount/10;
		}
		else
		{
			modificationAmount = goldAmount/5;
		}
		Roll newRoll = getCurrentState().getRecordedRollForSpecialCharacter();
		if(newRoll !=null)
		{
			newRoll.addRollModificationFor(0, modificationAmount);
			
			int totalRoll = 0;
			
			for(int roll : newRoll.getFinalRolls())
			{
				totalRoll += roll;
			}
			if(totalRoll >= (2*newRoll.getRollTarget().getValue()))
			{
				getCurrentState().getPlayerByPlayerNumber(playerNumber).addCardToHand(newRoll.getRollTarget());
			}
		}
		else
		{
			getCurrentState().addRollModification(new RollModification(new Roll(2, target, RollReason.RECRUIT_SPECIAL_CHARACTER, playerNumber), modificationAmount, 0));
		}
		getCurrentState().getPlayerByPlayerNumber(playerNumber).removeGold(goldAmount);
	}
	
	private void applyRollEffects()
	{
		ArrayList<Roll> handledRolls = new ArrayList<Roll>();
		
		for(Roll r : getCurrentState().getFinishedRolls())
		{
			switch(r.getRollReason())
			{
				case RECRUIT_SPECIAL_CHARACTER:
				{
					handledRolls.add(r);
					
					Player rollingPlayer = getCurrentState().getPlayerByPlayerNumber(r.getRollingPlayerID());
					
					int totalRoll = 0;
					
					for(int roll : r.getFinalRolls())
					{
						totalRoll += roll;
					}
					if(totalRoll >= (2*r.getRollTarget().getValue()))
					{
						rollingPlayer.addCardToHand(r.getRollTarget());
					}
					else
					{
						getCurrentState().recordRollForSpecialCharacter(r);
					}
					break;
				}
				default:
					break;
			}
		}
		
		for(Roll r : handledRolls)
		{
			getCurrentState().removeRoll(r);
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
	public void receiveModifyRollForSpecialCharacterCommand(ModifyRollForSpecialCharacter command)
	{
		try
		{
			handleSpecialCharacterRollModification(command.getTarget(),command.getID(),command.retrieveGoldAmount());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process ModifyRollForSpecialCharacter due to: ", t);
		}
	}
}

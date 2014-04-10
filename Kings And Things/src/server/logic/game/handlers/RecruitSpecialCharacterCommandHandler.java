package server.logic.game.handlers;

import java.util.ArrayList;

import server.event.DiceRolled;
import server.event.internal.ModifyRollForSpecialCharacterCommand;
import server.logic.game.RollModification;
import server.logic.game.validators.RecruitSpecialCharacterValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.RollReason;
import common.Constants.UpdateInstruction;
import common.Logger;
import common.event.network.CommandRejected;
import common.event.network.PlayersList;
import common.game.ITileProperties;
import common.game.Roll;

public class RecruitSpecialCharacterCommandHandler extends CommandHandler
{
	public void handleSpecialCharacterRollModification(ITileProperties target, int playerNumber, int goldAmount)
	{
		RecruitSpecialCharacterValidator.validateCanModifySpecialCharacterRoll(target, playerNumber, goldAmount, getCurrentState());
		getCurrentState().getPlayerByPlayerNumber(playerNumber).removeGold(goldAmount);
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
			
			int totalRoll = newRoll.getFinalTotal();

			int goalValue = (2*newRoll.getRollTarget().getValue());
			if(newRoll.getRollTarget().getName().equals("Marksman"))
			{
				goalValue = 10;
			}
			
			if(totalRoll >= goalValue)
			{
				givePlayerSpecialCharacterAndNotifyClients(newRoll.getRollingPlayerID(), newRoll.getRollTarget());
			}
		}
		else
		{
			getCurrentState().addRollModification(new RollModification(new Roll(2, target, RollReason.RECRUIT_SPECIAL_CHARACTER, playerNumber), modificationAmount, 0));
		}
		new PlayersList(getCurrentState().getPlayers()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
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
					
					int totalRoll = r.getFinalTotal();
					
					int goalValue = (2*r.getRollTarget().getValue());
					if(r.getRollTarget().getName().equals("Marksman"))
					{
						goalValue = 10;
					}
					
					if(totalRoll >= goalValue)
					{
						givePlayerSpecialCharacterAndNotifyClients(r.getRollingPlayerID(), r.getRollTarget());
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
	public void receiveApplyRoll(DiceRolled event)
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
	public void receiveModifyRollForSpecialCharacterCommand(ModifyRollForSpecialCharacterCommand command)
	{
		try
		{
			handleSpecialCharacterRollModification(command.getTarget(),command.getID(),command.retrieveGoldAmount());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process ModifyRollForSpecialCharacter due to: ", t);
			new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),UpdateInstruction.BribeHero).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
		}
	}
}

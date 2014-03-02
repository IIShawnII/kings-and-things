package server.logic.game.handlers;

import java.util.ArrayList;

import server.event.DiceRolled;
import server.logic.game.Player;

import com.google.common.eventbus.Subscribe;
import common.Logger;
import common.game.Roll;

public class RecruitSpecialCharacterCommandHandler extends CommandHandler
{
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
						//TODO special character is recruited
					}
					else
					{
						
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
}

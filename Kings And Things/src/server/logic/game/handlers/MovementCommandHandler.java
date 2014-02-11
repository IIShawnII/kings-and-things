package server.logic.game.handlers;

import java.util.Collection;
import java.util.List;

import server.event.commands.MoveThingsCommand;
import server.logic.game.validators.MovementValidator;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.event.notifications.HexStatesChanged;
import common.game.HexState;
import common.game.TileProperties;

public class MovementCommandHandler extends CommandHandler
{
	/**
	 * Call this to move creatures during the movement phase
	 * @param things The list of things the player wants to move
	 * @param playerNumber The player who sent the command
	 * @param hexes The hexes the player wants to move through
	 */
	public void moveThings(Collection<TileProperties> things, int playerNumber, List<TileProperties> hexes)
	{
		MovementValidator.validateCanMove(playerNumber, getCurrentState(), hexes, things);
		makeThingsMoved(things, playerNumber, hexes);
	}
	
	private void makeThingsMoved(Collection<TileProperties> things, int playerNumber, List<TileProperties> hexes)
	{
		int moveCost = 0;
		for(int i=1; i<hexes.size(); i++)
		{
			moveCost += hexes.get(i).getMoveSpeed();
		}
		
		HexState firstHex = getCurrentState().getBoard().getHexStateForHex(hexes.get(0));
		HexState lastHex = getCurrentState().getBoard().getHexStateForHex(hexes.get(hexes.size()-1));
		
		for(TileProperties thing : things)
		{
			thing.setMoveSpeed(thing.getMoveSpeed() - moveCost);
			firstHex.removeThingFromHex(thing);
			lastHex.addThingToHex(thing);
		}
	}

	@Subscribe
	public void moveThingsCommand(MoveThingsCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				moveThings(command.getThings(),command.getPlayerID(), command.getHexes());

				HexStatesChanged changedHex = new HexStatesChanged(2);
				changedHex.getArray()[0] = getCurrentState().getBoard().getHexStateForHex(command.getHexes().get(0));
				changedHex.getArray()[0] = getCurrentState().getBoard().getHexStateForHex(command.getHexes().get(1));
				changedHex.postNotification();
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process MoveThingsCommand due to: ", t);
			}
		}
	}
}

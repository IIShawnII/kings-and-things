package server.logic.game.validators;

import server.logic.game.GameState;
import server.logic.game.Player;
import common.Constants.BuildableBuilding;
import common.Constants.SetupPhase;
import common.game.TileProperties;

public abstract class ConstructBuildingCommandValidator
{
	/**
	 * Use this method to validate the construct building or place free tower commands
	 * @param building The building type to be placed
	 * @param playerNumber The player sending the command
	 * @param hex The hex to put the building in
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If it is not the entered player's turn, if the entered
	 * building or hex tile is invalid, or if construction can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for building things
	 */
	public static void validateCanBuildBuilding(BuildableBuilding building, int playerNumber, TileProperties hex, GameState currentState)
	{
		CommandValidator.validateIsPlayerActive(playerNumber,currentState);
		CommandValidator.validateNoPendingRolls(currentState);
		if(building==null)
		{
			throw new IllegalArgumentException("Can not create a null building");
		}
		Player owningPlayer = currentState.getPlayerByPlayerNumber(playerNumber);
		if(!owningPlayer.ownsHex(hex))
		{
			throw new IllegalArgumentException("Can not create a tower in someone else's hex");
		}
		
		if(currentState.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED)
		{
			//TODO check gold/income requirements for general case
		}
		else if(currentState.getCurrentSetupPhase() != SetupPhase.PLACE_FREE_TOWER)
		{
			throw new IllegalStateException("Can not create tower during the: " + currentState.getCurrentSetupPhase() + ", phase");
		}
	}

}

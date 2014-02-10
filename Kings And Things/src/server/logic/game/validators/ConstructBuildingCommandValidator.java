package server.logic.game.validators;

import server.logic.game.BuildableBuildingGenerator;
import server.logic.game.GameState;
import server.logic.game.Player;
import common.Constants.BuildableBuilding;
import common.Constants.SetupPhase;
import common.game.HexState;
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
		
		HexState hexState = currentState.getBoard().getHexStateForHex(hex);
		
		if(building==null)
		{
			throw new IllegalArgumentException("You need a select a building to build");
		}
		
		Player owningPlayer = currentState.getPlayerByPlayerNumber(playerNumber);
		hexState.validateCanAddThingToHex(BuildableBuildingGenerator.createBuildingTileForType(building));
		
		if(!owningPlayer.ownsHex(hex))
		{
			throw new IllegalArgumentException("You can not create a building in someone else's hex");
		} else {
			if (currentState.getHexesContainingBuiltObjects().contains(hexState)) 
			{
				throw new IllegalArgumentException("You cannot build in the same hex twice");
			}
		}
		
		if(currentState.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED)
		{
			//TODO check gold/income requirements for general case
			if (owningPlayer.getGold() < 5) {
				throw new IllegalArgumentException("You need more than 5 gold pieces to build a building");
			}
			
			//Checks if a player is eligible to build a citadel
			if (currentState.getPlayers().size() == 4) {
				if ((owningPlayer.getGold() >= 5 && owningPlayer.getGold() < 20) && building == BuildableBuilding.Citadel) {
					throw new IllegalArgumentException("You are not eligible to build a citadel");
				}
			} else {
				if ((owningPlayer.getGold() >= 5 && owningPlayer.getGold() < 15) && building == BuildableBuilding.Citadel) {
					throw new IllegalArgumentException("You are not eligible to build a citadel");
				}
			}
			
			//Checks if player is correctly upgrading
			switch (building) {
			case Keep:
				if (hexState.getBuilding() == null || hexState.getBuilding().getName().equals(BuildableBuilding.Tower.name()))
				{
					throw new IllegalArgumentException("Can't build keep unless you have a tower");
				}
				break;
			case Castle:
				if (hexState.getBuilding() == null || hexState.getBuilding().getName().equals(BuildableBuilding.Keep.name()))
				{
					throw new IllegalArgumentException("Can't build keep unless you have a keep");
				}
				break;
			case Citadel:
				if (hexState.getBuilding() == null || hexState.getBuilding().getName().equals(BuildableBuilding.Castle.name()))
				{
					throw new IllegalArgumentException("Can't build keep unless you have a castle");
				}
				break;
			default:
				break;
			}
			
		}
		else if(currentState.getCurrentSetupPhase() != SetupPhase.PLACE_FREE_TOWER)
		{
			throw new IllegalStateException("Can not create tower during the: " + currentState.getCurrentSetupPhase() + ", phase");
		}
		else if(building != BuildableBuilding.Tower) {
			throw new IllegalStateException("You can only build a tower during the: " + currentState.getCurrentSetupPhase() + ", phase");
		}
	}

}

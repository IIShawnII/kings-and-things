package server.logic.game.handlers;

import server.event.commands.ConstructBuildingCommand;
import server.logic.game.BuildableBuildingGenerator;
import server.logic.game.validators.ConstructBuildingCommandValidator;

import com.google.common.eventbus.Subscribe;

import common.Logger;
import common.Constants.BuildableBuilding;
import common.Constants.SetupPhase;
import common.event.notifications.HexStatesChanged;
import common.game.HexState;
import common.game.TileProperties;

public class ConstructBuildingCommandHandler extends CommandHandler
{
	/**
	 * Use this method to construct a building or place a free tower
	 * @param building The building type to be placed
	 * @param playerNumber The player sending the command
	 * @param hex The hex to put the building in
	 * @throws IllegalArgumentException If it is not the entered player's turn, if the entered
	 * building or hex tile is invalid, or if construction can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for building things
	 */
	public void constructBuilding(BuildableBuilding building, int playerNumber, TileProperties hex){
		ConstructBuildingCommandValidator.validateCanBuildBuilding(building, playerNumber, hex, getCurrentState());
		makeBuildingConstructed(building, playerNumber, hex);
		if(getCurrentState().getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER)
		{
			advanceActivePhasePlayer();
		}
	}

	private void makeBuildingConstructed(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		HexState hs = getCurrentState().getBoard().getHexStateForHex(hex);
		TileProperties buildingTile = BuildableBuildingGenerator.createBuildingTileForType(building);
		hs.removeBuildingFromHex();
		hs.addThingToHex(buildingTile);
		getCurrentState().getPlayerByPlayerNumber(playerNumber).addOwnedThingOnBoard(buildingTile);
		if (getCurrentState().getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED) {
			getCurrentState().getPlayerByPlayerNumber(playerNumber).removeGold(5);
		}
		getCurrentState().addHexToListOfConstructedHexes(hs);
	}
	
	@Subscribe
	public void recieveConstructBuildingCommand(ConstructBuildingCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				constructBuilding(command.getBuilding(), command.getPlayerID(), command.getHex());

				HexStatesChanged changedHex = new HexStatesChanged(1);
				changedHex.getArray()[0] = getCurrentState().getBoard().getHexStateForHex(command.getHex());
				changedHex.postNotification();
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ConstructBuildingCommand due to: ", t);
			}
		}
	}
}

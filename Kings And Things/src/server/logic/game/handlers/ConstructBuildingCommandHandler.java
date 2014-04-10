package server.logic.game.handlers;

import server.event.internal.ConstructBuildingCommand;
import server.logic.game.BuildableBuildingGenerator;
import server.logic.game.validators.ConstructBuildingCommandValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants.BuildableBuilding;
import common.Constants.SetupPhase;
import common.Constants;
import common.Logger;
import common.event.network.CommandRejected;
import common.event.network.HexStatesChanged;
import common.event.network.PlayersList;
import common.game.HexState;
import common.game.ITileProperties;

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
	public void constructBuilding(BuildableBuilding building, int playerNumber, ITileProperties hex){
		ConstructBuildingCommandValidator.validateCanBuildBuilding(building, playerNumber, hex, getCurrentState());
		makeBuildingConstructed(building, playerNumber, hex);
		if(getCurrentState().getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER)
		{
			advanceActivePhasePlayer();
		}
	}

	private void makeBuildingConstructed(BuildableBuilding building, int playerNumber, ITileProperties hex)
	{
		HexState hs = getCurrentState().getBoard().getHexStateForHex(hex);
		ITileProperties buildingTile = BuildableBuildingGenerator.createBuildingTileForType(building);
		hs.removeBuildingFromHex();
		hs.addThingToHex(buildingTile);
		getCurrentState().getPlayerByPlayerNumber(playerNumber).addOwnedThingOnBoard(buildingTile);
		if (getCurrentState().getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED) {
			if (!getCurrentState().hasWillingWorkersPlayed() || building == BuildableBuilding.Citadel) {
				getCurrentState().getPlayerByPlayerNumber(playerNumber).removeGold(5);
			}
			new PlayersList(getCurrentState().getPlayers()).postNetworkEvent(Constants.ALL_PLAYERS_ID);
		}
		getCurrentState().setWillingWorkersPlayed(false);
		getCurrentState().addHexToListOfConstructedHexes(hs);
		HexStatesChanged msg = new HexStatesChanged(1);
		msg.getArray()[0] = hs;
		msg.postNetworkEvent(Constants.ALL_PLAYERS_ID);
	}
	
	@Subscribe
	public void recieveConstructBuildingCommand(ConstructBuildingCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				constructBuilding(command.getBuilding(), command.getID(), command.getHex());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ConstructBuildingCommand due to: ", t);
				new CommandRejected(getCurrentState().getCurrentRegularPhase(),getCurrentState().getCurrentSetupPhase(),getCurrentState().getActivePhasePlayer().getPlayerInfo(),t.getMessage(),null).postNetworkEvent(getCurrentState().getActivePhasePlayer().getID());
			}
		}
	}
}

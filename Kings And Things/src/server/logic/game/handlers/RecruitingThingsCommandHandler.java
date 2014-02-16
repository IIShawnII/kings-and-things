package server.logic.game.handlers;

import java.util.ArrayList;
import java.util.Collection;

import server.event.commands.ExchangeThingsCommand;
import server.event.commands.PlaceThingOnBoardCommand;
import server.event.commands.RecruitThingsCommand;
import server.logic.exceptions.NoMoreTilesException;
import server.logic.game.Player;
import server.logic.game.validators.RecruitingThingsPhaseValidator;

import com.google.common.eventbus.Subscribe;

import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.Logger;
import common.event.notifications.HexStatesChanged;
import common.event.notifications.RackPlacement;
import common.game.HexState;
import common.game.TileProperties;

public class RecruitingThingsCommandHandler extends CommandHandler
{
	/**
	 * Call this to take free recruits, exchange things, and buy things all at
	 * the same time
	 * @param gold The gold amount the player wants to pay to purchase new recruits
	 * @param thingsToExchange The list of things the player wants to exchange from
	 * their tray
	 * @param playerNumber The player who sent the command
	 * @throws NoMoreTilesException If the cup runs out of things
	 */
	public void recruitThings(int gold, Collection<TileProperties> thingsToExchange, int playerNumber) throws NoMoreTilesException
	{
		paidRecruits(gold,playerNumber);
		exchangeThings(thingsToExchange,playerNumber);
		drawFreeThings(playerNumber);
	}
	
	/**
	 * Use this method to exchange things from a player's rack with new things from the cup.
	 * @param things The things the player wants to exchange, will be placed back in the cup
	 * only AFTER replacements are drawn
	 * @param playerNumber The player sending the command
	 * @throws NoMoreTilesException If there are no more tiles left in the cup
	 * @throws IllegalArgumentException If it is not the entered player's turn, or if the collection
	 * of things is invalid
	 * @throws IllegalStateException If it is nor the proper phase for exchanging things
	 */
	public void exchangeThings(Collection<TileProperties> things, int playerNumber) throws NoMoreTilesException{
		RecruitingThingsPhaseValidator.validateCanExchangeThings(things, playerNumber, getCurrentState());
		makeThingsExchanged(things,playerNumber);
		if(getCurrentState().getCurrentSetupPhase() != SetupPhase.SETUP_FINISHED)
		{
			advanceActivePhasePlayer();
		}
	}

	/**
	 * Player pays gold to the bank in order to recruit things
	 * @param gold The gold amount the player wants to spend
	 * @param playerNumber The player who sent the command
	 * @throws NoMoreTilesException If there are no more tiles in the cup
	 * @throws IllegalArgumentException If the command can not be done
	 * according to the game rules
	 * @throws IllegalStateException If it is not the right phase for recruiting things
	 */
	public void paidRecruits(int gold, int playerNumber) throws NoMoreTilesException {
		RecruitingThingsPhaseValidator.validateCanPurchaseRecruits(gold, playerNumber, getCurrentState());
		
		// retrieve player with the passed in player number
		Player  player = getCurrentState().getPlayerByPlayerNumber(playerNumber);
		
		// removes gold from player
		player.removeGold(gold);
		
		// adds things to the players tray for every 5 gold pieces
		for(int i = 0; i < (gold/5); i++) {
			player.addThingToTray(getCup().drawTile());
		}
	}

	/**
	 * Use this method to place something from a player's tray onto the board
	 * @param thing The thing to place on the board
	 * @param playerNumber The player sending the command
	 * @param hex The hex to place the thing on
	 * @throws IllegalArgumentException If it is not the entered player's turn, if the
	 * entered thing or hex tile is invalid, or if placement can not be made due to game
	 * rules
	 * @throws IllegalStateException If it is not the right phase for placing things on
	 * the board
	 */
	public void placeThingOnBoard(TileProperties thing, int playerNumber, TileProperties hex){
		RecruitingThingsPhaseValidator.validateCanPlaceThingOnBoard(thing, playerNumber, hex, getCurrentState());
		makeThingOnBoard(thing, playerNumber, hex);
	}
	
	private void makeThingsExchanged(Collection<TileProperties> things, int playerNumber) throws NoMoreTilesException
	{
		int newThingCount = things.size();
		
		// During the Regular Phase, we draw 1/2 as many stuff as they are throwing away
		if(getCurrentState().getCurrentRegularPhase() == RegularPhase.RECRUITING_THINGS) {
			newThingCount /= 2;
		}
		
		Player player = getCurrentState().getPlayerByPlayerNumber(playerNumber);
		ArrayList<TileProperties> newThings = new ArrayList<TileProperties>(newThingCount);
		
		for(int i=0; i<newThingCount; i++)
		{
			newThings.add(getCup().drawTile());
		}
		for(TileProperties oldThing : things)
		{
			getCup().reInsertTile(oldThing);
		}
		for(TileProperties newThing : newThings)
		{
			player.addThingToTray(newThing);
		}
	}

	private void drawFreeThings(int playerNumber) throws NoMoreTilesException
	{
		Player player = getCurrentState().getActivePhasePlayer();
		int freeThings = (int) Math.ceil(((double)player.getOwnedHexes().size()) / (double)2);
		for(int i=0; i<freeThings; i++)
		{
			player.addThingToTray(getCup().drawTile());
		}
	}

	private void makeThingOnBoard(TileProperties thing, int playerNumber, TileProperties hex){
		HexState hs = getCurrentState().getBoard().getHexStateForHex(hex);
		if(thing.isCreature() && thing.isFaceUp())
		{
			//TODO check if this is a special character first
			thing.flip();
		}
		hs.addThingToHex(thing);
		getCurrentState().getPlayerByPlayerNumber(playerNumber).placeThingFromTrayOnBoard(thing);
	}
	
	private void notifyClientsOfPlayerTray(int playerNumber)
	{
		RackPlacement toClient = new RackPlacement(getCurrentState().getPlayerByPlayerNumber(playerNumber).getTrayThings().size());
		int i=0;
		for(TileProperties tp : getCurrentState().getPlayerByPlayerNumber(playerNumber).getTrayThings())
		{
			toClient.getArray()[i++] = tp;
		}
		toClient.postNotification();
	}

	@Subscribe
	public void recieveExchangeThingsCommand(ExchangeThingsCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				exchangeThings(command.getThings(), command.getID());
				//notify client
				notifyClientsOfPlayerTray(command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process ExchangeThingsCommand due to: ", t);
			}
		}
	}

	@Subscribe
	public void recievePlaceThingOnBoardCommand(PlaceThingOnBoardCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				placeThingOnBoard(command.getThing(), command.getID(), command.getHex());
				
				HexStatesChanged changedHex = new HexStatesChanged(1);
				changedHex.getArray()[0] = getCurrentState().getBoard().getHexStateForHex(command.getHex());
				changedHex.postNotification();
				//notify client
				notifyClientsOfPlayerTray(command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process PlaceThingOnBoardCommand due to: ", t);
			}
		}
	}

	@Subscribe
	public void receiveRecruitThingsCommand(RecruitThingsCommand command)
	{
		if(command.isUnhandled())
		{
			try
			{
				recruitThings(command.getGold(), command.getThingsToExchange(), command.getID());
				//notify client
				notifyClientsOfPlayerTray(command.getID());
			}
			catch(Throwable t)
			{
				Logger.getErrorLogger().error("Unable to process RecruitThingsCommand due to: ", t);
			}
		}
	}
}

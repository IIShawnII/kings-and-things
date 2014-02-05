package server.logic.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import server.event.SendCommandAcrossNetworkEvent;

import com.google.common.eventbus.Subscribe;

import common.Constants.BuildableBuilding;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.Logger;
import common.TileProperties;
import common.event.CommandEventBus;
import common.game.CommandValidator;
import common.game.GameState;
import common.game.HexState;
import common.game.Player;
import common.game.commands.ConstructBuildingCommand;
import common.game.commands.EndPlayerTurnCommand;
import common.game.commands.ExchangeSeaHexCommand;
import common.game.commands.ExchangeThingsCommand;
import common.game.commands.GiveHexToPlayerCommand;
import common.game.commands.PaidRecruitsCommand;
import common.game.commands.PlaceThingOnBoardCommand;
import common.game.commands.StartGameCommand;
import common.game.exceptions.NoMoreTilesException;

/**
 * This class is used to execute commands that change the state of a game
 */
public class GameFlowManager
{
	private CupManager cup;
	private HexTileManager bank;
	private BoardGenerator boardGenerator;
	private GameState currentState;
	
	/**
	 * call this method to initialize this class before sending it commands
	 */
	public void initialize()
	{
		CommandEventBus.BUS.register(this);
	}
	
	/**
	 * Use this method to start a new game
	 * @param demoMode Set to true to stack the deck to match with the demo script
	 * @param players The players who will be playing this game
	 * @throws NoMoreTilesException If there are not enough tiles left in the bank
	 * to set up another board
	 * @throws IllegalArgumentException if the entered list of players is invalid
	 */
	public void startNewGame(boolean demoMode, Set<Player> players) throws NoMoreTilesException
	{
		CommandValidator.validateStartNewGame(demoMode, players);
		cup = new CupManager(demoMode);
		bank = new HexTileManager(demoMode);
		boardGenerator = new BoardGenerator(players.size(),bank);
		List<Integer> playerOrder = determinePlayerOrder(players,demoMode);
		currentState = new GameState(boardGenerator.createNewBoard(),players,playerOrder,SetupPhase.PICK_FIRST_HEX, RegularPhase.RECRUITING_CHARACTERS,playerOrder.get(0),playerOrder.get(0));
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new StartGameCommand(demoMode, players)));
	}
	
	/**
	 * Use this method to give control of a hex to a player
	 * @param hex The hex to change control of
	 * @param playerNumber The player sending the command
	 * @throws IllegalArgumentException if the entered hex is invalid, it is not
	 * the entered player's turn, or the command can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for selecting
	 * starting hexes
	 */
	public void giveHexToPlayer(TileProperties hex, int playerNumber)
	{
		CommandValidator.validateCanGiveHexToPlayer(hex, playerNumber, currentState);
		makeHexOwnedByPlayer(hex,playerNumber);
		if(currentState.getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && currentState.getPlayers().size() == 2)
		{
			pickSecondPlayersHex();
		}
		advanceActivePhasePlayer();
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new GiveHexToPlayerCommand(hex,playerNumber)));
	}
	
	/**
	 * Use this method to construct a building or place a free tower
	 * @param building The building type to be placed
	 * @param playerNumber The player sending the command
	 * @param hex The hex to put the building in
	 * @throws IllegalArgumentException If it is not the entered player's turn, if the entered
	 * building or hex tile is invalid, or if construction can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for building things
	 */
	public void constructBuilding(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		CommandValidator.validateCanBuildBuilding(building, playerNumber, hex, currentState);
		makeBuildingConstructed(building, playerNumber, hex);
		if(currentState.getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER)
		{
			advanceActivePhasePlayer();
		}
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new ConstructBuildingCommand(building, playerNumber, hex)));
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
	public void placeThingOnBoard(TileProperties thing, int playerNumber, TileProperties hex)
	{
		CommandValidator.validateCanPlaceThingOnBoard(thing, playerNumber, hex, currentState);
		makeThingOnBoard(thing, playerNumber, hex);
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new PlaceThingOnBoardCommand(thing,playerNumber,hex)));
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
	public void exchangeThings(Collection<TileProperties> things, int playerNumber) throws NoMoreTilesException
	{
		CommandValidator.validateCanExchangeThings(things, playerNumber, currentState);
		makeThingsExchanged(things,playerNumber);
		advanceActivePhasePlayer();
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new ExchangeThingsCommand(things, playerNumber)));
	}
	
	/**
	 * Call this method to swap a sea hex from the board with a random one from the bank
	 * @param hex The sea hex to swap
	 * @param playerNumber The player who sent the command
	 * @throws NoMoreTilesException If there are no more tiles left in the bank
	 * @throws IllegalArgumentException If hex is null, or is not a sea hex, or if the 
	 * sea hex can not be exchanged according to game rules
	 * @throws IllegalStateException If it is nor the proper phase for exchanging sea hexes
	 */
	public void exchangeSeaHex(TileProperties hex, int playerNumber) throws NoMoreTilesException
	{
		CommandValidator.validateCanExchangeSeaHex(hex, playerNumber, currentState);
		makeSeaHexExchanged(hex, playerNumber);
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new ExchangeSeaHexCommand(hex, playerNumber)));
	}
	
	/**
	 * Call this to end the current players turn (progresses to the next phase)
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 */
	public void endPlayerTurn(int playerNumber)
	{
		CommandValidator.validateCanEndPlayerTurn(playerNumber, currentState);
		advanceActivePhasePlayer();
		
		CommandEventBus.BUS.post(new SendCommandAcrossNetworkEvent(new EndPlayerTurnCommand(playerNumber)));
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
		CommandValidator.validateCanPurchaseRecruits(gold, playerNumber, currentState);
		
		// retrieve player with the passed in player number
		Player  player = currentState.getPlayerByPlayerNumber(playerNumber);
		
		// removes gold from player
		player.removeGold(gold);
		
		// adds things to the players tray for every 5 gold pieces
		for(int i = 0; i < (gold/5); i++) {
			player.addThingToTray(cup.drawTile());
		}
	}
	
	private void advanceActivePhasePlayer()
	{
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		int activePhasePlayerNumber = currentState.getActivePhasePlayer().getPlayerNumber();
		int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
		
		if(currentState.getPlayerOrder().size()-1 == activePhasePlayerOrderIndex)
		{
			if(nextSetupPhase != SetupPhase.SETUP_FINISHED)
			{
				nextSetupPhase = getNextSetupPhase();
			}
			else
			{
				nextRegularPhase = getNextRegularPhase();
				regularPhaseChanged(nextRegularPhase);
			}
		}
		currentState = new GameState(currentState.getBoard(), currentState.getPlayers(), currentState.getPlayerOrder(),
										nextSetupPhase, nextRegularPhase, currentState.getActiveTurnPlayer().getPlayerNumber(),
										currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
	}
	
	private void advanceActiveTurnPlayer()
	{
		int activeTurnPlayerNumber = currentState.getActiveTurnPlayer().getPlayerNumber();
		int activeTurnPlayerOrderIndex = currentState.getPlayerOrder().indexOf(activeTurnPlayerNumber);
		int nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(++activeTurnPlayerOrderIndex % currentState.getPlayers().size());
		
		//in a 2 player game turn order doesn't swap
		if(currentState.getPlayers().size() == 2)
		{
			nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(0);
		}

		currentState = new GameState(currentState.getBoard(), currentState.getPlayers(), currentState.getPlayerOrder(),
									currentState.getCurrentSetupPhase(), currentState.getCurrentRegularPhase(), nextActiveTurnPlayerNumber, nextActiveTurnPlayerNumber);
	}
	
	private SetupPhase getNextSetupPhase()
	{
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		
		if(nextSetupPhase == SetupPhase.SETUP_FINISHED)
		{
			return SetupPhase.SETUP_FINISHED;
		}
		else
		{
			int currentSetupPhaseIndex = nextSetupPhase.ordinal();
			for(SetupPhase sp : SetupPhase.values())
			{
				if(sp.ordinal() == (currentSetupPhaseIndex + 1))
				{
					setupPhaseChanged(sp);
					return sp;
				}
			}
		}
		
		throw new IllegalStateException("GameState contained invalid SetupPhase constant");
	}

	private RegularPhase getNextRegularPhase()
	{
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		if(nextRegularPhase == RegularPhase.SPECIAL_POWERS)
		{
			advanceActiveTurnPlayer();
			return RegularPhase.RECRUITING_CHARACTERS;
		}
		else
		{
			int currentRegularPhaseIndex = nextRegularPhase.ordinal();
			for(RegularPhase rp : RegularPhase.values())
			{
				if(rp.ordinal() == (currentRegularPhaseIndex + 1))
				{
					return rp;
				}
			}
		}
		
		throw new IllegalStateException("GameState contained invalid RegularPhase constant");
	}
	
	private void makeThingOnBoard(TileProperties thing, int playerNumber, TileProperties hex)
	{
		Point coords = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HexState hs = currentState.getBoard().getHexByXY(coords.x, coords.y);
		if(thing.isCreature() && thing.isFaceUp())
		{
			//TODO check if this is a special character first
			thing.flip();
		}
		hs.addThingToHex(thing);
		currentState.getPlayerByPlayerNumber(playerNumber).placeThingFromTrayOnBoard(thing);
	}
	
	private void makeSeaHexExchanged(TileProperties hex, int playerNumber) throws NoMoreTilesException
	{
		boardGenerator.placeHexAside(hex);
		TileProperties replacement = bank.drawTile();
		
		for(HexState hs : currentState.getBoard().getHexesAsList())
		{
			if(hs.getHex().equals(hex))
			{
				hs.setHex(replacement);
				break;
			}
		}
	}
	
	private void makeBuildingConstructed(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		Point coords = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HexState hs = currentState.getBoard().getHexByXY(coords.x, coords.y);
		TileProperties buildingTile = BuildableBuildingGenerator.createBuildingTileForType(building);
		hs.removeBuildingFromHex();
		hs.addThingToHex(buildingTile);
		currentState.getPlayerByPlayerNumber(playerNumber).addOwnedThingOnBoard(buildingTile);
	}
	
	private void makeGoldCollected()
	{
		for(Player p : currentState.getPlayers())
		{
			p.addGold(p.getIncome());
		}
	}
	
	private void makeThingsExchanged(Collection<TileProperties> things, int playerNumber) throws NoMoreTilesException
	{
		int newThingCount = things.size();
		
		// During the Regular Phase, we draw 1/2 as many stuff as they are throwing away
		if(currentState.getCurrentRegularPhase() == RegularPhase.RECRUITING_THINGS) {
			newThingCount /= 2;
		}
		
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		ArrayList<TileProperties> newThings = new ArrayList<TileProperties>(newThingCount);
		
		for(int i=0; i<newThingCount; i++)
		{
			newThings.add(cup.drawTile());
		}
		for(TileProperties oldThing : things)
		{
			cup.reInsertTile(oldThing);
		}
		for(TileProperties newThing : newThings)
		{
			player.addThingToTray(newThing);
		}
	}

	private List<Integer> determinePlayerOrder(Set<Player> playersIn, boolean demoMode)
	{
		ArrayList<Player> players = new ArrayList<Player>();
		for(Player p : playersIn)
		{
			players.add(p);
		}
		ArrayList<Integer> playerOrder = new ArrayList<Integer>(players.size());
		
		while(players.size()>1)
		{
			if(!demoMode)
			{
				int nextPlayerIndex = (int) Math.round(Math.random() * (players.size()-1));
				playerOrder.add(players.remove(nextPlayerIndex).getPlayerNumber());
			}
			else
			{
				int nextPlayerNumber = Integer.MAX_VALUE;
				for(Player p : players)
				{
					nextPlayerNumber = Math.min(nextPlayerNumber, p.getPlayerNumber());
				}
				Iterator<Player> it = players.iterator();
				while(it.hasNext())
				{
					Player nextPlayer = it.next();
					if(nextPlayer.getPlayerNumber() == nextPlayerNumber)
					{
						it.remove();
						playerOrder.add(nextPlayerNumber);
						break;
					}
				}
			}
		}
		
		return Collections.unmodifiableList(playerOrder);
	}
	
	private void makeHexOwnedByPlayer(TileProperties hex, int playerNumber)
	{
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsHex(hex))
			{
				p.removeHexFromOwnership(hex);
				break;
			}
		}
		currentState.getPlayerByPlayerNumber(playerNumber).addOwnedHex(hex);
	}
	
	/**
	 * In a 2 player game the second player must start opposite of the first
	 */
	private void pickSecondPlayersHex()
	{
		Point firstHexLocation = null;
		int firstPlayerNumber = -1;
		
		for(Player p : currentState.getPlayers())
		{
			if(!p.getOwnedHexes().isEmpty())
			{
				firstPlayerNumber = p.getPlayerNumber();
				TileProperties firstHex = p.getOwnedHexes().iterator().next();
				firstHexLocation = currentState.getBoard().getXYCoordinatesOfHex(firstHex);
			}
		}
		
		int offsetX = 2 - firstHexLocation.x;
		int offsetY = 4 - firstHexLocation.y;
		
		TileProperties hex = currentState.getBoard().getHexByXY(2 + offsetX, 4 + offsetY).getHex();
		CommandValidator.validateIsHexStartingPosition(hex,currentState);
		
		int secondPlayerNumber = -1;

		for(Player p : currentState.getPlayers())
		{
			if(p.getPlayerNumber() != firstPlayerNumber)
			{
				secondPlayerNumber = p.getPlayerNumber();
				break;
			}
		}
		
		makeHexOwnedByPlayer(hex,secondPlayerNumber);
	}
	
	private void setupPhaseChanged(SetupPhase setupPhase)
	{
		switch(setupPhase)
		{
			case EXCHANGE_SEA_HEXES:
			{
				//we need to flip all board hexes face up
				for(HexState hs : currentState.getBoard().getHexesAsList())
				{
					hs.getHex().flip();
				}
				break;
			}
			case PLACE_FREE_TOWER:
			{
				//give players 10 gold each
				for(Player p : currentState.getPlayers())
				{
					p.addGold(10);
				}
				break;
			}
			case PLACE_FREE_THINGS:
			{
				//give all players 10 free things from cup
				for(Player p : currentState.getPlayers())
				{
					for(int i=0; i<10; i++)
					{
						try
						{
							p.addThingToTray(cup.drawTile());
						}
						catch (NoMoreTilesException e)
						{
							// should never happen
							Logger.getErrorLogger().error("Unable to draw 10 free things for: " + currentState.getActivePhasePlayer() + ", due to: ", e);
						}
					}
				}
				break;
			}
			case SETUP_FINISHED:
			{
				boardGenerator.setupFinished();
				regularPhaseChanged(currentState.getCurrentRegularPhase());
			}
			default:
				break;
		}
	}

	private void regularPhaseChanged(RegularPhase regularPhase)
	{
		switch(regularPhase)
		{
			case RECRUITING_CHARACTERS:
			{
				//do income phase automagically
				makeGoldCollected();
				break;
			}
			default:
				break;
		}
	}
	
	
	
	/*
	 * The methods below recieve commands that are posted to the event bus and
	 * call the appropriate methods on this class with the posted command's parameters
	 */
	
	@Subscribe
	public void recieveGameStartCommand(StartGameCommand command)
	{
		try
		{
			startNewGame(command.getDemoMode(), command.getPlayers());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process StartGameCommand due to: ", t);
		}
	}
	
	@Subscribe
	public void recieveConstructBuildingCommand(ConstructBuildingCommand command)
	{
		try
		{
			constructBuilding(command.getBuilding(), command.getPlayerNumber(), command.getHex());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process ConstructBuildingCommand due to: ", t);
		}
	}
	
	@Subscribe
	public void recieveEndPlayerTurnCommand(EndPlayerTurnCommand command)
	{
		try
		{
			endPlayerTurn(command.getPlayerNumber());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process EndPlayerTurnCommand due to: ", t);
		}
	}

	@Subscribe
	public void recieveExchangeSeaHexCommand(ExchangeSeaHexCommand command)
	{
		try
		{
			exchangeSeaHex(command.getHex(), command.getPlayerNumber());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process ExchangeSeaHexCommand due to: ", t);
		}
	}

	@Subscribe
	public void recieveExchangeThingsCommand(ExchangeThingsCommand command)
	{
		try
		{
			exchangeThings(command.getThings(), command.getPlayerNumber());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process ExchangeThingsCommand due to: ", t);
		}
	}

	@Subscribe
	public void recieveGiveHexToPlayerCommand(GiveHexToPlayerCommand command)
	{
		try
		{
			giveHexToPlayer(command.getHex(), command.getPlayerNumber());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process GiveHexToPlayerCommand due to: ", t);
		}
	}

	@Subscribe
	public void recievePlaceThingOnBoardCommand(PlaceThingOnBoardCommand command)
	{
		try
		{
			placeThingOnBoard(command.getThing(), command.getPlayerNumber(), command.getHex());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process PlaceThingOnBoardCommand due to: ", t);
		}
	}
	
	@Subscribe
	public void receivePaidRecruitsCommand(PaidRecruitsCommand command)
	{
		try
		{
			paidRecruits(command.getGold(), command.getplayerNumber());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process PaidRecruitsCommand due to: ", t);
		}
	}
}

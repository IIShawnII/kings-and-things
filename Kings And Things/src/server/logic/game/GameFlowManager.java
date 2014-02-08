package server.logic.game;

import java.util.Set;
import java.awt.Point;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import server.event.commands.MoveThingsCommand;
import server.event.commands.ResolveCombat;
import server.event.commands.RollDiceCommand;
import server.event.commands.StartGameCommand;
import server.event.commands.EndPlayerTurnCommand;
import server.event.commands.ExchangeSeaHexCommand;
import server.event.commands.ExchangeThingsCommand;
import server.event.commands.GiveHexToPlayerCommand;
import server.event.commands.RecruitThingsCommand;
import server.event.commands.PlaceThingOnBoardCommand;
import server.event.commands.ConstructBuildingCommand;
import server.logic.exceptions.NoMoreTilesException;

import com.google.common.eventbus.Subscribe;

import common.Constants.CombatPhase;
import common.Constants.RollReason;
import common.Logger;
import common.Constants;
import common.Constants.SetupPhase;
import common.Constants.RegularPhase;
import common.Constants.BuildableBuilding;
import common.event.EventDispatch;
import common.event.notifications.Flip;
import common.event.notifications.HexPlacement;
import common.game.HexState;
import common.game.TileProperties;

/**
 * This class is used to execute commands that change the state of a game
 */
public class GameFlowManager{
	
	private CupManager cup;
	private HexTileManager bank;
	private BoardGenerator boardGenerator;
	private GameState currentState;
	private boolean isDemoMode;
	
	/**
	 * call this method to initialize this class before sending it commands
	 */
	public void initialize(){
		EventDispatch.registerForCommandEvents(this);
	}
	
	/**
	 * Use this method to start a new game
	 * @param demoMode Set to true to stack the deck to match with the demo script
	 * @param players The players who will be playing this game
	 * @throws NoMoreTilesException If there are not enough tiles left in the bank
	 * to set up another board
	 * @throws IllegalArgumentException if the entered list of players is invalid
	 */
	public void startNewGame(boolean demoMode, Set<Player> players) throws NoMoreTilesException{
		CommandValidator.validateStartNewGame(demoMode, players);
		isDemoMode = demoMode;
		cup = new CupManager(demoMode);
		bank = new HexTileManager(demoMode);
		boardGenerator = new BoardGenerator(players.size(),bank);
		HexBoard board = boardGenerator.createNewBoard();
		HexPlacement placement = new HexPlacement( Constants.MAX_HEXES);
		board.fillArray( placement.getArray());
		placement.postNotification();
		List<Integer> playerOrder = determinePlayerOrder(players,demoMode);
		//TODO handle dice rolls for player order
		currentState = new GameState(board,players,playerOrder,SetupPhase.PICK_FIRST_HEX, RegularPhase.RECRUITING_CHARACTERS,playerOrder.get(0),playerOrder.get(0), CombatPhase.NO_COMBAT, -1, null);
		new Flip().postNotification();
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
	public void giveHexToPlayer(TileProperties hex, int playerNumber){
		CommandValidator.validateCanGiveHexToPlayer(hex, playerNumber, currentState);
		makeHexOwnedByPlayer(hex,playerNumber);
		if(currentState.getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && currentState.getPlayers().size() == 2)
		{
			pickSecondPlayersHex();
		}
		else if(currentState.getCurrentSetupPhase() == SetupPhase.PICK_FIRST_HEX && currentState.getPlayers().size() == 4 && currentState.getActivePhasePlayer().getID() == currentState.getPlayerOrder().get(2))
		{
			//pick the last hex for player 4 automatically
			HashSet<TileProperties> startingHexes = new HashSet<TileProperties>();
			for(Point p : Constants.getValidStartingHexes(4))
			{
				startingHexes.add(currentState.getBoard().getHexByXY(p.x, p.y).getHex());
			}
			
			for(TileProperties tp : startingHexes)
			{
				boolean isOwned = false;
				for(Player p : currentState.getPlayers())
				{
					if(p.ownsHex(tp))
					{
						isOwned = true;
						break;
					}
				}
				if(!isOwned)
				{
					makeHexOwnedByPlayer(tp,currentState.getPlayerOrder().get(3));
					advanceActivePhasePlayer();
					break;
				}
			}
		}
		advanceActivePhasePlayer();
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
	public void constructBuilding(BuildableBuilding building, int playerNumber, TileProperties hex){
		CommandValidator.validateCanBuildBuilding(building, playerNumber, hex, currentState);
		makeBuildingConstructed(building, playerNumber, hex);
		if(currentState.getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER)
		{
			advanceActivePhasePlayer();
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
		CommandValidator.validateCanPlaceThingOnBoard(thing, playerNumber, hex, currentState);
		makeThingOnBoard(thing, playerNumber, hex);
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
		CommandValidator.validateCanExchangeThings(things, playerNumber, currentState);
		makeThingsExchanged(things,playerNumber);
		if(currentState.getCurrentSetupPhase() != SetupPhase.SETUP_FINISHED)
		{
			advanceActivePhasePlayer();
		}
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
	public void exchangeSeaHex(TileProperties hex, int playerNumber) throws NoMoreTilesException{
		CommandValidator.validateCanExchangeSeaHex(hex, playerNumber, currentState);
		makeSeaHexExchanged(hex, playerNumber);
	}
	
	/**
	 * Call this to end the current players turn (progresses to the next phase)
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 */
	public void endPlayerTurn(int playerNumber){
		CommandValidator.validateCanEndPlayerTurn(playerNumber, currentState);
		advanceActivePhasePlayer();
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
	 * Call this to move creatures during the movement phase
	 * @param things The list of things the player wants to move
	 * @param playerNumber The player who sent the command
	 * @param hexes The hexes the player wants to move through
	 */
	public void moveThings(Collection<TileProperties> things, int playerNumber, List<TileProperties> hexes)
	{
		CommandValidator.validateCanMove(playerNumber, currentState, hexes, things);
		makeThingsMoved(things, playerNumber, hexes);
	}
	
	/**
	 * Call this to roll dice for a player
	 * @param reasonForRoll The reason for this dice roll
	 * @param playerNumber The player who sent the command
	 * @param tile The target of the role, (could be hex, creature, building etc)
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT
	 */
	public void rollDice(RollReason reasonForRoll, int playerNumber, TileProperties tile)
	{
		CommandValidator.validateCanRollDice(reasonForRoll, playerNumber, tile, currentState);
		makeDiceRoll(reasonForRoll, playerNumber, tile);
	}
	
	/**
	 * Call this to resolve combat in, or explore, a particular hex
	 * @param hex The hex to resolve
	 * @param playerNumber The player who sent the command
	 * @throws IllegalArgumentException If parameters are invalid, or
	 * if combat in hex can not be resolved according to game rules
	 * @throws IllegalStateException If it is not the combat phase,
	 * or if another combat is already being resolved
	 */
	public void resolveCombat(TileProperties hex, int playerNumber)
	{
		CommandValidator.validateCanResolveCombat(hex,playerNumber,currentState);
		beginCombatResolution(hex, playerNumber);
	}
	
	private void advanceActivePhasePlayer(){
		SetupPhase nextSetupPhase = currentState.getCurrentSetupPhase();
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		int activePhasePlayerNumber = currentState.getActivePhasePlayer().getID();
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
		currentState.setCurrentSetupPhase(nextSetupPhase);
		currentState.setCurrentRegularPhase(nextRegularPhase);
		currentState.setCurrentCombatPhase(CombatPhase.NO_COMBAT);
		currentState.setCombatLocation(null);
		currentState.setDefendingPlayerNumber(-1);
		currentState.setActivePhasePlayer(currentState.getPlayerOrder().get(++activePhasePlayerOrderIndex % currentState.getPlayers().size()));
	}
	
	private void advanceActiveTurnPlayer(){
		int activeTurnPlayerNumber = currentState.getActiveTurnPlayer().getID();
		int activeTurnPlayerOrderIndex = currentState.getPlayerOrder().indexOf(activeTurnPlayerNumber);
		int nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(++activeTurnPlayerOrderIndex % currentState.getPlayers().size());
		
		//in a 2 player game turn order doesn't swap
		if(currentState.getPlayers().size() == 2)
		{
			nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(0);
		}

		currentState.setActivePhasePlayer(nextActiveTurnPlayerNumber);
		currentState.setActiveTurnPlayer(nextActiveTurnPlayerNumber);
	}
	
	private SetupPhase getNextSetupPhase(){
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

	private RegularPhase getNextRegularPhase(){
		RegularPhase nextRegularPhase = currentState.getCurrentRegularPhase();
		
		if(nextRegularPhase == RegularPhase.SPECIAL_POWERS)
		{
			advanceActiveTurnPlayer();
			return RegularPhase.RECRUITING_CHARACTERS;
		}
		else
		{
			if(currentState.getBoard().getContestedHexes(currentState.getPlayers()).size() > 0)
			{
				return RegularPhase.COMBAT;
			}
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
	
	private void makeThingOnBoard(TileProperties thing, int playerNumber, TileProperties hex){
		HexState hs = currentState.getBoard().getHexStateForHex(hex);
		if(thing.isCreature() && thing.isFaceUp())
		{
			//TODO check if this is a special character first
			thing.flip();
		}
		hs.addThingToHex(thing);
		currentState.getPlayerByPlayerNumber(playerNumber).placeThingFromTrayOnBoard(thing);
	}

	private void makeThingsMoved(Collection<TileProperties> things, int playerNumber, List<TileProperties> hexes)
	{
		int moveCost = 0;
		for(int i=1; i<hexes.size(); i++)
		{
			moveCost += hexes.get(i).getMoveSpeed();
		}
		
		HexState firstHex = currentState.getBoard().getHexStateForHex(hexes.get(0));
		HexState lastHex = currentState.getBoard().getHexStateForHex(hexes.get(hexes.size()-1));
		
		for(TileProperties thing : things)
		{
			thing.setMoveSpeed(thing.getMoveSpeed() - moveCost);
			firstHex.removeThingFromHex(thing);
			lastHex.addThingToHex(thing);
		}

	}
	
	private void beginCombatResolution(TileProperties hex, int playerNumber)
	{
		boolean isExploration = true;
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsHex(hex))
			{
				isExploration = false;
			}
		}
		if(isExploration)
		{
			List<Integer> playerOrder = currentState.getPlayerOrder();
			int attackerIndex = playerOrder.indexOf(playerNumber);
			int defenderIndex = attackerIndex>0? attackerIndex-1 : playerOrder.size()-1;
			
			currentState.setCurrentCombatPhase(CombatPhase.DETERMINE_DEFENDERS);
			currentState.setDefendingPlayerNumber(playerOrder.get(defenderIndex));
			currentState.setCombatLocation(currentState.getBoard().getXYCoordinatesOfHex(hex));
			currentState.addNeededRoll(new Roll(1,currentState.getCombatHex().getHex(),RollReason.EXPLORE_HEX,playerNumber));
		}
		else
		{
			//TODO implement regular combat
		}
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
	
	private void drawFreeThings(int playerNumber) throws NoMoreTilesException
	{
		Player player = currentState.getActivePhasePlayer();
		int freeThings = (int) Math.ceil(((double)player.getOwnedHexes().size()) / (double)2);
		for(int i=0; i<freeThings; i++)
		{
			player.addThingToTray(cup.drawTile());
		}
	}
	
	private void makeBuildingConstructed(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		HexState hs = currentState.getBoard().getHexStateForHex(hex);
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
				playerOrder.add(players.remove(nextPlayerIndex).getID());
			}
			else
			{
				int nextPlayerNumber = Integer.MAX_VALUE;
				for(Player p : players)
				{
					nextPlayerNumber = Math.min(nextPlayerNumber, p.getID());
				}
				Iterator<Player> it = players.iterator();
				while(it.hasNext())
				{
					Player nextPlayer = it.next();
					if(nextPlayer.getID() == nextPlayerNumber)
					{
						it.remove();
						playerOrder.add(nextPlayerNumber);
						break;
					}
				}
			}
		}
		playerOrder.add(players.get(0).getID());
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
				firstPlayerNumber = p.getID();
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
			if(p.getID() != firstPlayerNumber)
			{
				secondPlayerNumber = p.getID();
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
				//give all players 10 free things from cup, things are drawn randomly so player order
				//doesn't matter, unless we are in demo mode, in which case we must do it in player
				//order, so let's just do it in player order all the time
				ArrayList<Player> players = new ArrayList<Player>();
				ArrayList<Integer> playerOrder = new ArrayList<Integer>(currentState.getPlayerOrder());
				for(Integer i : playerOrder)
				{
					players.add(currentState.getPlayerByPlayerNumber(i));
				}

				for(Player p : players)
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
			case COMBAT:
			{
				//replenish move points of all creatures in preparation for next round
				for(HexState hs : currentState.getBoard().getHexesAsList())
				{
					for(TileProperties tp : hs.getCreaturesInHex())
					{
						tp.setMoveSpeed(4);
					}
				}
			}
			default:
				break;
		}
	}
	
	private void makeDiceRoll(RollReason reasonForRoll, int playerNumber, TileProperties tile)
	{
		if(reasonForRoll == RollReason.ENTERTAINMENT)
		{
			currentState.addNeededRoll(new Roll(1, null, RollReason.ENTERTAINMENT, playerNumber));
		}
		
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, reasonForRoll, playerNumber, tile) && r.needsRoll())
			{
				r.addRoll(rollDie());
				//TODO notify players of die roll
				break;
			}
		}
		
		//if we are no longer waiting for more rolls, then we can apply the effects now
		if(!currentState.isWaitingForRolls())
		{
			applyRollEffects();
		}
	}
	
	private void applyRollEffects()
	{
		for(Roll r : currentState.getFinishedRolls())
		{
			switch(r.getRollReason())
			{
				case ATTACK_WITH_CREATURE:
					//TODO handle
					break;
				case CALCULATE_DAMAGE_TO_TILE:
					//TODO handle
					break;
				case DETERMINE_PLAYER_ORDER:
					//TODO handle
					break;
				case ENTERTAINMENT:
					//nothing to do
					break;
				case EXPLORE_HEX:
					if(isDemoMode)
					{
						//give hex to player
						makeHexOwnedByPlayer(r.getRollTarget(), r.getRollingPlayerID());
						currentState.setCurrentCombatPhase(CombatPhase.PLACE_THINGS);
					}
					else
					{
						//TODO implement regular exploration
					}
					break;
				default:
					break;
				
			}
		}
		
		currentState.removeAllRecordedRolls();
	}
	
	private static int rollDie()
	{
		return (int) Math.round((Math.random() * 5) + 1);
	}
	
	/**
	 * This method is useful for unit testing purposes,
	 * which does not use our event driven architecture
	 * @return The current state of the game, in it's
	 * entirety
	 */
	GameState getCurrentState()
	{
		return currentState;
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
			constructBuilding(command.getBuilding(), command.getPlayerID(), command.getHex());
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
			endPlayerTurn(command.getPlayerID());
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
			exchangeSeaHex(command.getHex(), command.getPlayerID());
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
			exchangeThings(command.getThings(), command.getPlayerID());
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
			giveHexToPlayer(command.getHex(), command.getPlayerID());
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
			placeThingOnBoard(command.getThing(), command.getPlayerID(), command.getHex());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process PlaceThingOnBoardCommand due to: ", t);
		}
	}
	
	@Subscribe
	public void receiveRecruitThingsCommand(RecruitThingsCommand command)
	{
		try
		{
			recruitThings(command.getGold(), command.getThingsToExchange(), command.getPlayerID());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process RecruitThingsCommand due to: ", t);
		}
	}

	@Subscribe
	public void moveThingsCommand(MoveThingsCommand command)
	{
		try
		{
			moveThings(command.getThings(),command.getPlayerID(), command.getHexes());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process MoveThingsCommand due to: ", t);
		}
	}
	
	@Subscribe
	public void receiveResolveCombatCommand(ResolveCombat command)
	{
		try
		{
			resolveCombat(command.getCombatHex(), command.getPlayerID());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process ResolveCombatCommand due to: ", t);
		}
	}

	@Subscribe
	public void receiveRollDiceCommand(RollDiceCommand command)
	{
		try
		{
			rollDice(command.getReasonForRoll(), command.getPlayerID(), command.getTileToRollFor());
		}
		catch(Throwable t)
		{
			Logger.getErrorLogger().error("Unable to process RollDieCommand due to: ", t);
		}
	}
}

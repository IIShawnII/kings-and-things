package server.logic.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.Constants.Ability;
import common.Constants.Biome;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.RollReason;
import common.Constants.SetupPhase;
import common.game.HexState;
import common.game.TileProperties;

/**
 * This class checks requested commands against game states and throws appropriate
 * exceptions if the command cannot be achieved, clients can use this to block
 * commands before they are rejected by the server.
 */
public abstract class CommandValidator
{
	/**
	 * Use this method to validate the start new game commands
	 * @param demoMode Set to true to stack the deck to match with the demo script
	 * @param players The players who will be playing this game
	 * @throws NoMoreTilesException If there are not enough tiles left in the bank
	 * to set up another board
	 * @throws IllegalArgumentException if the entered list of players is invalid
	 */
	@SuppressWarnings("unused")
	public static void validateStartNewGame(boolean demoMode, Set<Player> players)
	{
		validateCollection(players,"players");
		if( !Constants.BYPASS_MIN_PLAYER && (players.size() < Constants.MIN_PLAYERS || Constants.MAX_PLAYERS < players.size()))
		{
			throw new IllegalArgumentException("Can only start a game with 2 to 4 players");
		}
	}
	
	/**
	 * Use this method to validate the give hex to player command
	 * @param hex The hex to change control of
	 * @param playerNumber The player sending the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException if the entered hex is invalid, it is not
	 * the entered player's turn, or the command can not be done due to game rules
	 * @throws IllegalStateException if it is not the correct phase for selecting
	 * starting hexes
	 */
	public static void validateCanGiveHexToPlayer(TileProperties hex, int playerNumber, GameState currentState)
	{
		validateIsPlayerActive(playerNumber, currentState);
		validateNoPendingRolls(currentState);
		switch(currentState.getCurrentSetupPhase())
		{
			case PICK_FIRST_HEX:
			{
				validateIsHexStartingPosition(hex, currentState);
				break;
			}
			case PICK_SECOND_HEX:
			case PICK_THIRD_HEX:
			{
				validateCanPickSetupPhaseHex(hex, playerNumber, currentState);
				break;
			}
			default:
			{
				throw new IllegalStateException("Can not give hexes to players during the " + currentState.getCurrentSetupPhase() + " phase.");
			}
		}
	}


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
		validateIsPlayerActive(playerNumber,currentState);
		validateNoPendingRolls(currentState);
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

	/**
	 * Use this method to validate the exchange things command.
	 * @param things The things the player wants to exchange, will be placed back in the cup
	 * only AFTER replacements are drawn
	 * @param playerNumber The player sending the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws NoMoreTilesException If there are no more tiles left in the cup
	 * @throws IllegalArgumentException If it is not the entered player's turn, or if the collection
	 * of things is invalid
	 * @throws IllegalStateException If it is nor the proper phase for exchanging things
	 */
	public static void validateCanExchangeThings(Collection<TileProperties> things, int playerNumber, GameState currentState)
	{
		validateIsPlayerActive(playerNumber,currentState);
		validateNoPendingRolls(currentState);
		validateCollection(things,"things");
		
		SetupPhase sp = currentState.getCurrentSetupPhase();
		RegularPhase rp = currentState.getCurrentRegularPhase();
		if(sp != SetupPhase.EXCHANGE_THINGS && rp != RegularPhase.RECRUITING_THINGS)
		{
			throw new IllegalArgumentException("Can not exchange things during the " + (sp==SetupPhase.SETUP_FINISHED? rp : sp) + " phase");
		}
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		for(TileProperties tp : things)
		{
			if(!player.ownsThingInTray(tp))
			{
				throw new IllegalArgumentException("Can not exchange something not in your tray");
			}
		}
		if(sp == SetupPhase.SETUP_FINISHED && things.size() % 2 > 0)
		{
			throw new IllegalArgumentException("Can only exchange things in twos");
		}
	}

	/**
	 * Use this method to validate the place thing on board command
	 * @param thing The thing to place on the board
	 * @param playerNumber The player sending the command
	 * @param hex The hex to place the thing on
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If it is not the entered player's turn, if the
	 * entered thing or hex tile is invalid, or if placement can not be made due to game
	 * rules
	 * @throws IllegalStateException If it is not the right phase for placing things on
	 * the board
	 */
	public static void validateCanPlaceThingOnBoard(final TileProperties thing, int playerNumber, TileProperties hex, GameState currentState)
	{
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		CombatPhase combatPhase = currentState.getCurrentCombatPhase();
		if(combatPhase != CombatPhase.PLACE_THINGS)
		{
			validateIsPlayerActive(playerNumber,currentState);
		}
		else if(!currentState.getCombatHex().getHex().equals(hex))
		{
			throw new IllegalArgumentException("Can only place things on the newly acquired hex");
		}
		
		validateNoPendingRolls(currentState);
		SetupPhase setupPhase = currentState.getCurrentSetupPhase();
		RegularPhase regularPhase = currentState.getCurrentRegularPhase();
		if(setupPhase != SetupPhase.PLACE_FREE_THINGS && setupPhase != SetupPhase.PLACE_EXCHANGED_THINGS && regularPhase != RegularPhase.RECRUITING_THINGS && combatPhase != CombatPhase.PLACE_THINGS)
		{
			throw new IllegalStateException("Can not place things on the board during the " + (setupPhase==SetupPhase.SETUP_FINISHED? regularPhase : setupPhase) + " phase");
		}
		HexState hs = currentState.getBoard().getHexStateForHex(hex);
		if(!player.ownsHex(hex))
		{
			throw new IllegalArgumentException("Can not place things onto someone else's hex");
		}
		if(!player.ownsThingInTray(thing))
		{
			throw new IllegalArgumentException("Can only place things that the player owns in their tray");
		}
		
		ArrayList<TileProperties> stuff = new ArrayList<>();
		stuff.add(thing);
		validateCreatureLimitInHexNotExceeded(playerNumber,hex,currentState,stuff);
		
		hs.validateCanAddThingToHex(thing);
	}
	
	/**
	 * Call this method to validate the paid recruits command
	 * @param amountToSpend The amount of gold the player wants to spend on recruits
	 * @param playerNumber The player sending the command
	 * @param currentState The current state of the game
	 * @throws IllegalStateException If it is not the right phase for purchasing recruits
	 * @throws IllegalArgumentException if the command is invalid
	 */
	public static void validateCanPurchaseRecruits(int amountToSpend, int playerNumber, GameState currentState)
	{
		validateIsPlayerActive(playerNumber,currentState);
		validateNoPendingRolls(currentState);
		RegularPhase regularPhase = currentState.getCurrentRegularPhase();
		if(regularPhase != RegularPhase.RECRUITING_THINGS)
		{
			throw new IllegalStateException("Can not purchase recruits during the " + regularPhase + " phase");
		}
		if(amountToSpend < 0)
		{
			throw new IllegalArgumentException("The amount of gold being spent must be positive");
		}
		if(amountToSpend > 25)
		{
			throw new IllegalArgumentException("Can not purchase more than 5 extra recruits");
		}
		if(amountToSpend > currentState.getPlayerByPlayerNumber(playerNumber).getGold())
		{
			throw new IllegalArgumentException("Can not spend more than the player's total gold amount");
		}
		if(amountToSpend % 5 > 0)
		{
			throw new IllegalArgumentException("Gold amount must be divisible by 5");
		}
	}
	
	/**
	 * Call this method to validate the move command
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game
	 * @param Hexes The list of hexes the player wants to move through
	 * @param Creatures The list of creatures the player wants to move
	 * @throws IllegalStateException If it isn't the movement phase
	 * @throws IllegalArgumentException If the move can't be completed due
	 * to game rules
	 */
	public static void validateCanMove(int playerNumber, GameState currentState, List<TileProperties> Hexes, Collection<TileProperties> Creatures) {
		
		// checks if it's player's turn
		validateIsPlayerActive(playerNumber, currentState);
		validateNoPendingRolls(currentState);
		validateCollection(Hexes,"hexes");
		validateCollection(Creatures,"creatures");
		
		// the following conditional statement checks if it is the movement
		if (currentState.getCurrentSetupPhase() != SetupPhase.SETUP_FINISHED) {
			throw new IllegalStateException("Can't move during the setup phase");
		} else if (currentState.getCurrentRegularPhase() != RegularPhase.MOVEMENT) {
			throw new IllegalStateException("Can't move during the " + currentState.getCurrentRegularPhase() + " phase");
		}
		
		validateMovementConditions(playerNumber,currentState,Hexes,Creatures);
	}

	/**
	 * Call this method to validate the swap sea hex command
	 * @param hex The sea hex to swap
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws NoMoreTilesException If there are no more tiles left in the bank
	 * @throws IllegalArgumentException If hex is null, or is not a sea hex, or if the 
	 * sea hex can not be exchanged according to game rules
	 * @throws IllegalStateException If it is nor the proper phase for exchanging sea hexes
	 */
	public static void validateCanExchangeSeaHex(TileProperties hex, int playerNumber, GameState currentState)
	{
		validateIsPlayerActive(playerNumber,currentState);
		validateNoPendingRolls(currentState);
		if(hex == null)
		{
			throw new IllegalArgumentException("The entered tile must not be null.");
		}
		if(!hex.isHexTile())
		{
			throw new IllegalArgumentException("Can only exchange hex tiles.");
		}
		if(Biome.valueOf(hex.getName()) != Biome.Sea)
		{
			throw new IllegalArgumentException("Can only exchange sea hexes.");
		}
		
		TileProperties startingHex = currentState.getPlayerByPlayerNumber(playerNumber).getOwnedHexes().iterator().next();
		List<HexState> adjacentHexes = currentState.getBoard().getAdjacentHexesTo(startingHex);
		
		HexState hexState = currentState.getBoard().getHexStateForHex(hex);
		
		int numSeaHexes = 0;
		for(HexState hs : adjacentHexes)
		{
			if(Biome.valueOf(hs.getHex().getName()) == Biome.Sea)
			{
				numSeaHexes++;
			}
		}
		if(!startingHex.equals(hex) && (numSeaHexes < 2 || !adjacentHexes.contains(hexState)))
		{
			throw new IllegalArgumentException("Can only exchange sea hexes on player starting position or adjacent to starting position when there are 2 or more sea hexes next to starting position.");
		}

		switch(currentState.getCurrentSetupPhase())
		{
			case EXCHANGE_SEA_HEXES:
			{
				break;
			}
			default:
			{
				throw new IllegalStateException("Can not exchange sea hexes during the " + currentState.getCurrentSetupPhase() + " phase.");
			}
		}
	}

	/**
	 * Use this method to check if a hex is one of the valid starting position choices
	 * @param hex The hex to check
	 * @param currentState The current state of the game to do the validation check on
	 */
	public static void validateIsHexStartingPosition(TileProperties hex, GameState currentState)
	{
		Point desiredHex = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HashSet<Point> validChoices = Constants.getValidStartingHexes(currentState.getPlayers().size());
		if(!validChoices.contains(desiredHex))
		{
			throw new IllegalArgumentException("The chosen hex was not a starting hex.");
		}
		for(Player p : currentState.getPlayers())
		{
			if(p.ownsHex(hex))
			{
				throw new IllegalArgumentException("The chosen hex is already taken by player: " + p);
			}
		}
	}
	
	/**
	 * Call this to validate the resolve combat in hex command
	 * @param hex The hex to resolve
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If parameters are invalid, or
	 * if combat in hex can not be resolved according to game rules
	 * @throws IllegalStateException If it is not the combat phase,
	 * or if another combat is already being resolved
	 */
	public static void validateCanResolveCombat(TileProperties hex, int playerNumber, GameState currentState)
	{
		validateNoPendingRolls(currentState);
		validateIsPlayerActive(playerNumber,currentState);
		if(currentState.getCurrentCombatPhase() != CombatPhase.NO_COMBAT)
		{
			throw new IllegalStateException("Must resolve existing combat before starting a new one.");
		}
		if(currentState.getCurrentRegularPhase() != RegularPhase.COMBAT)
		{
			throw new IllegalStateException("Can only resolve combat during the combat phase");
		}
		HexState combatHex = currentState.getBoard().getHexStateForHex(hex);
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		if(!player.ownsHex(hex) && combatHex.getThingsInHexOwnedByPlayer(player).size() == 0)
		{
			throw new IllegalArgumentException("Can only resolve combat in a hex that involves the player");
		}
		boolean otherPlayersOwnThingsInHex = false;
		
		for(Player p : currentState.getPlayers())
		{
			if(!p.equals(player) && combatHex.getThingsInHexOwnedByPlayer(p).size()!=0)
			{
				otherPlayersOwnThingsInHex = true;
				break;
			}
		}
		
		if(player.ownsHex(hex) && !otherPlayersOwnThingsInHex)
		{
			throw new IllegalArgumentException("The entered hex is not a combat hex");
		}
	}

	/**
	 * Call this to validate the roll dice command
	 * @param reasonForRoll The reason this roll is being done
	 * @param playerNumber The player who sent the command
	 * @param tileToRollFor The tile being rolled for (might be creature, hex, tower etc)
	 * @param currentState The current state of the game
	 * @throws IllegalArgumentException If the game is not currently waiting for any
	 * rolls, and the reason for rolling is not RollReason.ENTERTAINMENT
	 */
	public static void validateCanRollDice(RollReason reasonForRoll, int playerNumber, TileProperties tileToRollFor, GameState currentState)
	{
		boolean rollNeeded = false;
		for(Roll r : currentState.getRecordedRolls())
		{
			if(Roll.rollSatisfiesParameters(r, reasonForRoll, playerNumber, tileToRollFor))
			{
				rollNeeded = true;
			}
		}
		if(!rollNeeded && reasonForRoll != RollReason.ENTERTAINMENT)
		{
			throw new IllegalArgumentException("Not currently waiting for " + reasonForRoll + " type roll from player " + playerNumber + " targeting tile: " + tileToRollFor);
		}
	}
	
	/**
	 * Call this to validate the end the current players turn command
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If it is not the entered player's turn
	 * @throws IllegalStateException If the player can not end their turn
	 */
	public static void validateCanEndPlayerTurn(int playerNumber, GameState currentState)
	{
		validateNoPendingRolls(currentState);
		if(currentState.getCurrentCombatPhase() == CombatPhase.NO_COMBAT)
		{
			validateIsPlayerActive(playerNumber, currentState);
		}
		switch(currentState.getCurrentSetupPhase())
		{
			case PICK_FIRST_HEX:
			{
				throw new IllegalStateException("You must select a starting hex.");
			}
			case PICK_SECOND_HEX:
			case PICK_THIRD_HEX:
			{
				throw new IllegalStateException("You must select a hex to take ownership of.");
			}
			case PLACE_FREE_TOWER:
			{
				throw new IllegalStateException("You must select a hex to place your tower in.");
			}
			default:
				break;
		}
		switch(currentState.getCurrentCombatPhase())
		{
			case PLACE_THINGS:
			{
				Player player = currentState.getPlayerByPlayerNumber(playerNumber);
				if(!player.ownsHex(currentState.getCombatHex().getHex()))
				{
					throw new IllegalStateException("Must wait for combat winner to place things on hex");
				}
			}
			case NO_COMBAT:
				break;
			default:
				throw new IllegalStateException("You must resolve the combat in the hex before ending your turn");
			
		}
	}
	
	// private methods

	private static void validateCanPickSetupPhaseHex(TileProperties hex, int playerNumber, GameState currentState)
	{
		boolean playerHasOneAdjacentHex = false;
		for(HexState hs : currentState.getBoard().getAdjacentHexesTo(hex))
		{
			for(Player p : currentState.getPlayers())
			{
				if(p.ownsHex(hs.getHex()))
				{
					if(p.getID() == playerNumber)
					{
						playerHasOneAdjacentHex = true;
					}
					else
					{
						throw new IllegalArgumentException("The chosen hex is adjacent to one of player: " + p + "'s hexes");
					}
				}
			}
		}
		if(!playerHasOneAdjacentHex)
		{
			throw new IllegalArgumentException("The chosen hex must be adjacent to a currently owned hex.");
		}
	}

	private static void validateIsPlayerActive(int playerNumber, GameState currentState)
	{
		if(currentState.getActivePhasePlayer().getID() != playerNumber)
		{
			throw new IllegalArgumentException("It is still: " + currentState.getActivePhasePlayer() + "'s turn to move.");
		}
	}
	
	private static void validateCollection(Collection<?> things, String typeInCollection)
	{
		if(things==null)
		{
			throw new IllegalArgumentException("The list of " + typeInCollection + " must not be null");
		}
		for(Object o : things)
		{
			if(o==null)
			{
				throw new IllegalArgumentException("The list of " + typeInCollection + " must not contain null elements");
			}
		}
	}
	
	/*
	 * Checks to see if the number of creatures exceeds the limit
	 */
	private static void validateCreatureLimitInHexNotExceeded(int playerNumber, TileProperties hex, GameState currentState, Collection<TileProperties> toAdd)
	{
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		HexState hs = currentState.getBoard().getHexStateForHex(hex);
		
		for(TileProperties thing : toAdd)
		{
			if(thing.isCreature() && !(hs.hasBuilding() && hs.getBuilding().getName().equals(Building.Citadel.name())))
			{
				Set<TileProperties> existingCreatures = hs.getCreaturesInHex();
				int ownedCreatureCount = 0;
				for(TileProperties tp : existingCreatures)
				{
					if(player.ownsThingOnBoard(tp))
					{
						ownedCreatureCount++;
					}
				}
				
				if(ownedCreatureCount >= Constants.MAX_FRIENDLY_CREATURES_FOR_NON_CITADEL_HEX)
				{
					throw new IllegalArgumentException("Can not place more than " + ownedCreatureCount + " friendly creatures in the same hex, unless it contains a Citadel.");
				}
			}
		}
	}
	
	/*
	 * Checks to see if player can move through hexes
	 */
	private static void validateMovementConditions(int playerNumber, GameState currentState, List<TileProperties> Hexes, Collection<TileProperties> Creatures) {
		
		int moveSpeedTotal = 0;
		HashSet<HexState> pathOfHexes = new HashSet<>();
		HexState nextHex = null;

		HexState firstHex = currentState.getBoard().getHexStateForHex(Hexes.get(0));
		
		if(!currentState.getBoard().areHexesConnected(Hexes))
		{
			throw new IllegalArgumentException("You can only move through hexes that are adjacent to each other.");
		}
		
		validateCreatureLimitInHexNotExceeded(playerNumber, Hexes.get(Hexes.size() - 1), currentState, Creatures);
		
		boolean seaHexesExist = false;
		
		boolean hexNotOwned = true;
		
		for (int i = 1; i < Hexes.size(); i++) {
			TileProperties hex = Hexes.get(i);
			
			if (i < Hexes.size() - 1) {
				nextHex = currentState.getBoard().getHexStateForHex(Hexes.get(i));
				pathOfHexes.add(nextHex);
				for (Player p : currentState.getPlayers()) {
					if (p.ownsHex(hex)) {
						hexNotOwned = false;
					}
				}
				if (hexNotOwned) {
					throw new IllegalArgumentException("Can't move through unexplored hexes");
				}
			}
			
			moveSpeedTotal += hex.getMoveSpeed();
			
			if (!hex.isHexTile()) {
				throw new IllegalArgumentException("Can't move through non hexes");
			}
			
			if (Biome.Sea.name().equals(hex.getName())) {
				seaHexesExist = true;
			}
		}

		for (TileProperties creature : Creatures) {
			if(!currentState.getPlayerByPlayerNumber(playerNumber).ownsThingOnBoard(creature))
			{
				throw new IllegalArgumentException("You can only move your own creatures");
			}
			if (!creature.isCreature()) {
				throw new IllegalArgumentException("You can only move creatures");
			}
			if (creature.getMoveSpeed() < moveSpeedTotal) {
				throw new IllegalArgumentException("Creature cannot move that far");
			}
			if (!creature.isSpecialCreatureWithAbility(Ability.Fly) && seaHexesExist) {
				throw new IllegalArgumentException("Can't move through sea hexes");
			}
			if(!firstHex.getThingsInHex().contains(creature))
			{
				throw new IllegalArgumentException("Can only move creatures contained in the first hex of the movement");
			}
		}
		
		List<Player> players = new ArrayList<>();
		
		for (Player player : currentState.getPlayers()) {
			if (player.getID() != playerNumber) {
				players.add(player);
			}
		}
		
		pathOfHexes.add(firstHex);
		Player playerMoving = currentState.getPlayerByPlayerNumber(playerNumber);
		
		HashSet<TileProperties> thingsInHex = new HashSet<>();
		for (HexState newHex : pathOfHexes) {
			thingsInHex.clear();
			thingsInHex.addAll(newHex.getCreaturesInHex());
			
			if (newHex.hasBuilding()) {
				thingsInHex.add(newHex.getBuilding());
			}
		
			for (TileProperties creature : thingsInHex) {
				if (!playerMoving.ownsThingOnBoard(creature)) {
					throw new IllegalArgumentException("Can't move out of hex with enemy creatures");
				}
			}
		}
		
		if (Biome.Sea.name().equals(Hexes.get(Hexes.size() - 1).getName())) {
			throw new IllegalArgumentException("Can't end movement on sea hex");
		}
	}
	
	private static void validateNoPendingRolls(GameState currentState)
	{
		if(currentState.isWaitingForRolls())
		{
			throw new IllegalStateException("Some players must finish rolling dice first");
		}
	}
}

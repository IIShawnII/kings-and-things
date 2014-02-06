package server.logic.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.Player;
import common.Constants.Biome;
import common.Constants.BuildableBuilding;
import common.Constants.Building;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.TileProperties;

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
	public static void validateStartNewGame(boolean demoMode, Set<Player> players)
	{
		validateCollection(players,"players");
		if(players.size() < 2 || 4 < players.size())
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
		validateIsPlayerActive(playerNumber,currentState);
		SetupPhase setupPhase = currentState.getCurrentSetupPhase();
		RegularPhase regularPhase = currentState.getCurrentRegularPhase();
		if(setupPhase != SetupPhase.PLACE_FREE_THINGS && setupPhase != SetupPhase.PLACE_EXCHANGED_THINGS && regularPhase != RegularPhase.RECRUITING_THINGS)
		{
			throw new IllegalStateException("Can not place things on the board during the " + (setupPhase==SetupPhase.SETUP_FINISHED? regularPhase : setupPhase) + " phase");
		}
		Point coords = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HexState hs = currentState.getBoard().getHexByXY(coords.x, coords.y);
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
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
	 * @param
	 * @param
	 * @param
	 * @throws
	 * @throws
	 * @throws
	 */
	public static void validateCanMove(int playerNumber, GameState currentState, Collection<TileProperties> Hexes, Collection<TileProperties> Creatures) {
		
		// checks if it's player's turn
		validateIsPlayerActive(playerNumber, currentState);
		validateCollection(Hexes,"hexes");
		validateCollection(Creatures,"creatures");
		
		// the following conditional statement checks if it is the movement
		if (currentState.getCurrentSetupPhase() != SetupPhase.SETUP_FINISHED) {
			throw new IllegalStateException("Can't move during the setup phase");
		} else if (currentState.getCurrentRegularPhase() != RegularPhase.MOVEMENT) {
			throw new IllegalStateException("Can't move during the " + currentState.getCurrentRegularPhase() + " phase");
		} else {
			
		}
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
		
		Point hexCoords = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HexState hexState = currentState.getBoard().getHexByXY(hexCoords.x, hexCoords.y);
		
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
		ArrayList<Point> validChoices = new ArrayList<Point>();
		if(currentState.getPlayers().size() == 4)
		{
			validChoices.add(new Point(1,2));
			validChoices.add(new Point(1,2));
			validChoices.add(new Point(5,10));
			validChoices.add(new Point(5,10));
		}
		else
		{
			validChoices.add(new Point(0,2));
			validChoices.add(new Point(0,6));
			validChoices.add(new Point(2,0));
			validChoices.add(new Point(2,8));
			validChoices.add(new Point(4,2));
			validChoices.add(new Point(4,6));
		}
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
	 * Call this to validate the end the current players turn command
	 * @param playerNumber The player who sent the command
	 * @param currentState The current state of the game to do the validation check on
	 * @throws IllegalArgumentException If it is not the entered player's turn, or if
	 * the player can not end their turn (this is only true during the setup phase)
	 */
	public static void validateCanEndPlayerTurn(int playerNumber, GameState currentState)
	{
		switch(currentState.getCurrentSetupPhase())
		{
			case PICK_FIRST_HEX:
			{
				throw new IllegalArgumentException("You must select a starting hex.");
			}
			case PICK_SECOND_HEX:
			case PICK_THIRD_HEX:
			{
				throw new IllegalArgumentException("You must select a hex to take ownership of.");
			}
			case PLACE_FREE_TOWER:
			{
				throw new IllegalArgumentException("You must select a hex to place your tower in.");
			}
			default:
				break;
		}
		validateIsPlayerActive(playerNumber, currentState);
	}

	private static void validateCanPickSetupPhaseHex(TileProperties hex, int playerNumber, GameState currentState)
	{
		boolean playerHasOneAdjacentHex = false;
		for(HexState hs : currentState.getBoard().getAdjacentHexesTo(hex))
		{
			for(Player p : currentState.getPlayers())
			{
				if(p.ownsHex(hs.getHex()))
				{
					if(p.getPlayerNumber() == playerNumber)
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
		if(currentState.getActivePhasePlayer().getPlayerNumber() != playerNumber)
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
	
	private static void validateCreatureLimitInHexNotExceeded(int playerNumber, TileProperties hex, GameState currentState, Collection<TileProperties> toAdd)
	{
		Player player = currentState.getPlayerByPlayerNumber(playerNumber);
		Point xy = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HexState hs = currentState.getBoard().getHexByXY(xy.x, xy.y);
		
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
	
	@SuppressWarnings("unused")
	//TODO add functionality
	private static void validateMovementConditions(int playerNumber, GameState currentState, Collection<TileProperties> Hexes, Collection<TileProperties> Creatures) {
		
		int moveSpeedTotal = 0;
		
		for (TileProperties hex : Hexes) {
			moveSpeedTotal += hex.getMoveSpeed();
			if (!hex.isHexTile()) {
				throw new IllegalArgumentException("Can't move through non hexes");
			}
		}
		
		for (TileProperties creature : Creatures) {
			currentState.getPlayerByPlayerNumber(playerNumber).ownsThingOnBoard(creature);
			if (!creature.isCreature()) {
				throw new IllegalArgumentException("You can only move creatures");
			}
			if (creature.getMoveSpeed() < moveSpeedTotal) {
				throw new IllegalArgumentException("Creature cannot move that far");
			}
			
		}
	}
}

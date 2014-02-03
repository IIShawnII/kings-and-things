package server.logic.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import server.exceptions.NoMoreTilesException;
import common.Constants.Biome;
import common.Constants.BuildableBuilding;
import common.Constants.SetupPhase;
import common.Logger;
import common.TileProperties;
import common.game.GameState;
import common.game.HexState;
import common.game.Player;

public class GameFlowManager
{
	private CupManager cup;
	private HexTileManager bank;
	private BoardGenerator boardGenerator;
	private GameState currentState;
	
	public void startNewGame(boolean demoMode, HashSet<Player> players) throws NoMoreTilesException
	{
		cup = new CupManager(demoMode);
		bank = new HexTileManager(demoMode);
		boardGenerator = new BoardGenerator(players.size(),bank);
		List<Integer> playerOrder = determinePlayerOrder(players,demoMode);
		currentState = new GameState(boardGenerator.createNewBoard(),players,playerOrder,SetupPhase.PICK_FIRST_HEX,playerOrder.get(0),playerOrder.get(0));
		sendGameStateToClient();
	}
	
	public void giveHexToPlayer(TileProperties hex, int playerNumber)
	{
		validateIsPlayerActive(playerNumber);
		switch(currentState.getCurrentSetupPhase())
		{
			case PICK_FIRST_HEX:
			{
				validateIsHexStartingPosition(hex);
				makeHexOwnedByPlayer(hex,playerNumber);
				if(currentState.getPlayers().size() == 2)
				{
					pickSecondPlayersHex();
				}
				advanceActivePhasePlayer();
				if(currentState.getCurrentSetupPhase() == SetupPhase.EXCHANGE_SEA_HEXES)
				{
					//we need to flip all board hexes face up
					for(HexState hs : currentState.getBoard().getHexesAsList())
					{
						hs.getHex().flip();
					}
					//send updated board state to client
					sendGameStateToClient();
				}
				break;
			}
			case PICK_SECOND_HEX:
			case PICK_THIRD_HEX:
			{
				validateCanPickSetupPhaseHex(hex, playerNumber);
				makeHexOwnedByPlayer(hex,playerNumber);
				advanceActivePhasePlayer();
				
				if(currentState.getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER)
				{
					//give players 10 gold each
					for(Player p : currentState.getPlayers())
					{
						p.addGold(10);
					}
					//send updated board state to client
					sendGameStateToClient();
				}
				break;
			}
			default:
			{
				throw new IllegalStateException("Can not give hexes to players during the " + currentState.getCurrentSetupPhase() + " phase.");
			}
		}
	}
	
	public void constructBuilding(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		validateIsPlayerActive(playerNumber);
		validateCanBuildBuilding(building,playerNumber,hex);
		makeBuildingConstructed(building, playerNumber, hex);
		if(currentState.getCurrentSetupPhase() == SetupPhase.PLACE_FREE_TOWER)
		{
			advanceActivePhasePlayer();
			if(currentState.getCurrentSetupPhase() == SetupPhase.PLACE_FREE_THINGS)
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
				//send updated state to client
				sendGameStateToClient();
			}
		}
	}
	
	public void placeThingOnBoard(TileProperties thing, int playerNumber, TileProperties hex)
	{
		//TODO implement this method
	}
	
	public void exchangeThings(Collection<TileProperties> things, int playerNumber)
	{
		//TODO implement this method
	}
	
	public void exchangeSeaHex(TileProperties hex, int playerNumber) throws NoMoreTilesException
	{
		validateIsPlayerActive(playerNumber);
		validateCanExchangeSeaHex(hex,playerNumber);
		switch(currentState.getCurrentSetupPhase())
		{
			case EXCHANGE_SEA_HEXES:
			{
				makeSeaHexExchanged(hex, playerNumber);
			}
			default:
			{
				throw new IllegalStateException("Can not exchange sea hexes during the " + currentState.getCurrentSetupPhase() + " phase.");
			}
		}
	}
	
	public void endPlayerTurn(int playerNumber)
	{
		validateIsPlayerActive(playerNumber);
		advanceActivePhasePlayer();
	}
	
	private void sendGameStateToClient()
	{
		//TODO implement this method
	}
	
	private void advanceActivePhasePlayer()
	{
		int activePhasePlayerNumber = currentState.getActivePhasePlayer().getPlayerNumber();
		int activePhasePlayerOrderIndex = currentState.getPlayerOrder().indexOf(activePhasePlayerNumber);
		if(currentState.getPlayerOrder().size()-1 == activePhasePlayerOrderIndex)
		{
			advanceActiveTurnPlayer();
		}
		else
		{
			currentState = new GameState(currentState.getBoard(), currentState.getPlayers(), currentState.getPlayerOrder(),
										currentState.getCurrentSetupPhase(), currentState.getActiveTurnPlayer().getPlayerNumber(),
										currentState.getPlayerOrder().get(activePhasePlayerOrderIndex+1));
		}
		
		sendGameStateToClient();
	}
	
	private void advanceActiveTurnPlayer()
	{
		SetupPhase nextSetupPhase = null;
		if(currentState.getCurrentSetupPhase() == SetupPhase.SETUP_FINISHED)
		{
			nextSetupPhase = SetupPhase.SETUP_FINISHED;
		}
		else
		{
			int currentSetupPhaseIndex = currentState.getCurrentSetupPhase().ordinal();
			for(SetupPhase sp : SetupPhase.values())
			{
				if(sp.ordinal() == (currentSetupPhaseIndex + 1))
				{
					nextSetupPhase = sp;
					break;
				}
			}
		}

		int activeTurnPlayerNumber = currentState.getActiveTurnPlayer().getPlayerNumber();
		int activeTurnPlayerOrderIndex = currentState.getPlayerOrder().indexOf(activeTurnPlayerNumber);
		int nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(++activeTurnPlayerOrderIndex % currentState.getPlayers().size());
		
		//in a 2 player game turn order doesn't swap
		if(currentState.getPlayers().size() == 2)
		{
			nextActiveTurnPlayerNumber = currentState.getPlayerOrder().get(0);
		}

		currentState = new GameState(currentState.getBoard(), currentState.getPlayers(), currentState.getPlayerOrder(),
									nextSetupPhase, nextActiveTurnPlayerNumber, nextActiveTurnPlayerNumber);
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

		sendGameStateToClient();
	}
	
	private void makeBuildingConstructed(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
		Point coords = currentState.getBoard().getXYCoordinatesOfHex(hex);
		HexState hs = currentState.getBoard().getHexByXY(coords.x, coords.y);
		TileProperties buildingTile = BuildableBuildingGenerator.createBuildingTileForType(building);
		hs.removeBuildingFromHex();
		hs.addThingToHex(buildingTile);
		currentState.getPlayerByPlayerNumber(playerNumber).addOwnedThingOnBoard(buildingTile);

		sendGameStateToClient();
	}
	
	private void validateCanBuildBuilding(BuildableBuilding building, int playerNumber, TileProperties hex)
	{
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
	}
	
	private void validateCanExchangeSeaHex(TileProperties hex, int playerNumber)
	{
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
	}
	
	private List<Integer> determinePlayerOrder(HashSet<Player> playersIn, boolean demoMode)
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
	
	/**
	 * Call this method only AFTER validation!
	 * @param hex - hex to change ownership
	 * @param playerNumber - player to gain control of hex
	 */
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
		validateIsHexStartingPosition(hex);
		
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
	
	private void validateIsHexStartingPosition(TileProperties hex)
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
	
	private void validateCanPickSetupPhaseHex(TileProperties hex, int playerNumber)
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
	
	private void validateIsPlayerActive(int playerNumber)
	{
		if(currentState.getActivePhasePlayer().getPlayerNumber() != playerNumber)
		{
			throw new IllegalArgumentException("It is still: " + currentState.getActivePhasePlayer() + "'s turn to move.");
		}
	}
}

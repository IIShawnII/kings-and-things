package server.logic.game;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import server.logic.exceptions.NoMoreTilesException;
import common.Constants;
import common.Constants.BuildableBuilding;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.game.ITileProperties;
import common.game.Player;
import common.game.PlayerInfo;

public class StateGenerator
{
	public enum GeneratorType{EXPLORATION, MOVEMENT, CONSTRUCTION, MINIMAL_DEMO}
	
	private final String fileName;
	private final boolean isLoadOperation;
	private final GameState generatedState;
	private final GeneratorType type = GeneratorType.MINIMAL_DEMO;
	
	public StateGenerator(String fileName, boolean load) throws ClassNotFoundException, FileNotFoundException, IOException
	{
		this.fileName = fileName;
		isLoadOperation = load;
		generatedState = load? loadStateFromFile() : saveStateToFile();
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public boolean isLoadOperation()
	{
		return isLoadOperation;
	}
	
	public GameState getGeneratedState()
	{
		return generatedState;
	}
	
	private GameState saveStateToFile() throws FileNotFoundException, IOException
	{
		switch(type)
		{
			case EXPLORATION:
				return generateExplorationState();
			case MOVEMENT:
				return generateMovementState();
			case CONSTRUCTION:
				return generateConstructionState();
			case MINIMAL_DEMO:
				try
				{
					return generateMinimalFunctionalityDemoState();
				}
				catch (NoMoreTilesException e)
				{
					e.printStackTrace();
				}
		}
		
		return null;
	}
	
	private GameState loadStateFromFile() throws ClassNotFoundException, FileNotFoundException, IOException
	{
		try(FileInputStream fs = new FileInputStream(fileName);ObjectInputStream ois = new ObjectInputStream(fs))
		{
			return (GameState) ois.readObject();
		}
	}

	private GameState generateConstructionState() throws IOException
	{
		Player p1 = new Player(new PlayerInfo("Erik",Constants.PLAYER_1_ID,true));
		Player p2 = new Player(new PlayerInfo("Archit",Constants.PLAYER_2_ID,true));
		Player p3 = new Player(new PlayerInfo("Shariar",Constants.PLAYER_3_ID,true));
		Player p4 = new Player(new PlayerInfo("Nadra",Constants.PLAYER_4_ID,true));
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		
		ArrayList<Integer> playerOrder = new ArrayList<>();
		playerOrder.add(p1.getID());
		playerOrder.add(p2.getID());
		playerOrder.add(p3.getID());
		playerOrder.add(p4.getID());
		GameState state = new GameState(true, players, playerOrder, SetupPhase.EXCHANGE_SEA_HEXES, RegularPhase.RECRUITING_CHARACTERS, p1.getID(), p1.getID(),
				CombatPhase.NO_COMBAT, Constants.PUBLIC, null);
		
		p1.addOwnedHex(state.getBoard().getHexByXY(1, 2).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(3, 6).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(2, 5).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(2, 7).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(4, 5).getHex());
		
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 2).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(1, 10).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(5, 10).getHex());
		
		p1.addGold(50);
		
		ITileProperties tower1 = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Tower);
		ITileProperties tower2 = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Tower);
		ITileProperties keep = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Keep);
		ITileProperties castle = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Castle);
		ITileProperties citadel = BuildableBuildingGenerator.createBuildingTileForType(BuildableBuilding.Citadel);
			
		state.getBoard().getHexByXY(1, 2).addThingToHex(tower1);
		state.getBoard().getHexByXY(3, 6).addThingToHex(tower2);
		state.getBoard().getHexByXY(2, 5).addThingToHex(keep);
		state.getBoard().getHexByXY(2, 7).addThingToHex(castle);
		state.getBoard().getHexByXY(4, 5).addThingToHex(citadel);
			
		p1.addOwnedThingOnBoard(tower1);
		p1.addOwnedThingOnBoard(tower2);
		p1.addOwnedThingOnBoard(keep);
		p1.addOwnedThingOnBoard(castle);
		p1.addOwnedThingOnBoard(citadel);
			
		state.setCurrentSetupPhase(SetupPhase.SETUP_FINISHED);
		state.setCurrentRegularPhase(RegularPhase.CONSTRUCTION);
		state.setActivePhasePlayer(p1.getID());
		state.setActiveTurnPlayer(p1.getID());
		
		for(Player p : players)
		{
			for(ITileProperties tp : p.getOwnedHexes())
			{
				state.getBoard().getHexStateForHex(tp).setMarker(Constants.getPlayerMarker( p.getID()));
			}
		}
		
		try(FileOutputStream fs = new FileOutputStream(fileName);ObjectOutputStream os = new ObjectOutputStream(fs))
		{
			os.writeObject(state);
			os.flush();
			
			return state;
		}
	}

	private GameState generateMinimalFunctionalityDemoState() throws IOException, NoMoreTilesException
	{
		Player p1 = new Player(new PlayerInfo("Xaphan",Constants.PLAYER_1_ID,true));
		Player p2 = new Player(new PlayerInfo("Leviathan",Constants.PLAYER_2_ID,true));
		Player p3 = new Player(new PlayerInfo("Abaddon",Constants.PLAYER_3_ID,true));
		Player p4 = new Player(new PlayerInfo("Lilith",Constants.PLAYER_4_ID,true));
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		
		ArrayList<Integer> playerOrder = new ArrayList<>();
		playerOrder.add(p1.getID());
		playerOrder.add(p2.getID());
		playerOrder.add(p3.getID());
		playerOrder.add(p4.getID());
		GameState state = new GameState(true, players, playerOrder, SetupPhase.SETUP_FINISHED, RegularPhase.SPECIAL_POWERS, p1.getID(), p1.getID(),
				CombatPhase.NO_COMBAT, Constants.PUBLIC, null);
		
		p1.addOwnedHex(state.getBoard().getHexByXY(4, 1).getHex());
		addBuildingToHexForPlayer(4, 1, p1, BuildableBuilding.Keep, state);
		p1.addOwnedHex(state.getBoard().getHexByXY(3, 2).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(4, 3).getHex());
		addBuildingToHexForPlayer(4, 3, p1, BuildableBuilding.Castle, state);
		p1.addOwnedHex(state.getBoard().getHexByXY(5, 2).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(6, 3).getHex());
		addBuildingToHexForPlayer(6, 3, p1, BuildableBuilding.Castle, state);
		p1.addOwnedHex(state.getBoard().getHexByXY(5, 4).getHex());
		addBuildingToHexForPlayer(5, 4, p1, BuildableBuilding.Tower, state);
		p1.addOwnedHex(state.getBoard().getHexByXY(4, 5).getHex());
		addBuildingToHexForPlayer(4, 5, p1, BuildableBuilding.Tower, state);
		addThingByNameToHexForPlayer("Crocodiles", new Point(4,5), p1, state);
		addThingByNameToHexForPlayer("Mountain_Men", new Point(4,5), p1, state);
		addThingByNameToHexForPlayer("Giant_Lizard", new Point(4,5), p1, state);
		addThingByNameToHexForPlayer("Slime_Beast", new Point(4,5), p1, state);
		addThingByNameToHexForPlayer("Killer_Racoon", new Point(4,5), p1, state);
		addThingByNameToHexForPlayer("Farmers", new Point(4,5), p1, state);
		addThingByNameToHexForPlayer("Wild_Cat", new Point(4,5), p1, state);
		p1.addOwnedHex(state.getBoard().getHexByXY(3, 6).getHex());
		addBuildingToHexForPlayer(3, 6, p1, BuildableBuilding.Keep, state);
		
		p2.addOwnedHex(state.getBoard().getHexByXY(3, 8).getHex());
		p2.addOwnedHex(state.getBoard().getHexByXY(4, 7).getHex());
		addBuildingToHexForPlayer(4, 7, p2, BuildableBuilding.Tower, state);
		addThingByNameToHexForPlayer("Thing", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Giant_Lizard", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Swamp_Rat", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Unicorn", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Bears", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Giant_Spider", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Camel_Corps", new Point(4,7), p2, state);
		addThingByNameToHexForPlayer("Sandworm", new Point(4,7), p2, state);
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 6).getHex());
		addBuildingToHexForPlayer(5, 6, p2, BuildableBuilding.Keep, state);
		p2.addOwnedHex(state.getBoard().getHexByXY(6, 5).getHex());
		addBuildingToHexForPlayer(6, 5, p2, BuildableBuilding.Keep, state);
		p2.addOwnedHex(state.getBoard().getHexByXY(6, 7).getHex());
		addBuildingToHexForPlayer(6, 7, p2, BuildableBuilding.Tower, state);
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 8).getHex());
		addBuildingToHexForPlayer(5, 8, p2, BuildableBuilding.Keep, state);
		p2.addOwnedHex(state.getBoard().getHexByXY(4, 9).getHex());
		addBuildingToHexForPlayer(4, 9, p2, BuildableBuilding.Castle, state);
		p2.addOwnedHex(state.getBoard().getHexByXY(4, 11).getHex());
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 10).getHex());
		p2.addOwnedHex(state.getBoard().getHexByXY(6, 9).getHex());
		
		p3.addOwnedHex(state.getBoard().getHexByXY(3, 12).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(2, 11).getHex());
		addBuildingToHexForPlayer(2, 11, p3, BuildableBuilding.Keep, state);
		p3.addOwnedHex(state.getBoard().getHexByXY(1, 10).getHex());
		addBuildingToHexForPlayer(1, 10, p3, BuildableBuilding.Tower, state);
		p3.addOwnedHex(state.getBoard().getHexByXY(2, 9).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(2, 7).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(3, 10).getHex());
		
		p4.addOwnedHex(state.getBoard().getHexByXY(0, 3).getHex());
		addBuildingToHexForPlayer(0, 3, p4, BuildableBuilding.Keep, state);
		p4.addOwnedHex(state.getBoard().getHexByXY(0, 5).getHex());
		addBuildingToHexForPlayer(0, 5, p4, BuildableBuilding.Keep, state);
		p4.addOwnedHex(state.getBoard().getHexByXY(0, 7).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(1, 2).getHex());
		addBuildingToHexForPlayer(1, 2, p4, BuildableBuilding.Castle, state);
		p4.addOwnedHex(state.getBoard().getHexByXY(1, 4).getHex());
		addBuildingToHexForPlayer(1, 4, p4, BuildableBuilding.Tower, state);
		p4.addOwnedHex(state.getBoard().getHexByXY(2, 3).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(2, 1).getHex());
		
		for(Player p : players)
		{
			for(ITileProperties tp : p.getOwnedHexes())
			{
				state.getBoard().getHexStateForHex(tp).setMarker(Constants.getPlayerMarker( p.getID()));
			}
		}
		
		try(FileOutputStream fs = new FileOutputStream(fileName);ObjectOutputStream os = new ObjectOutputStream(fs))
		{
			os.writeObject(state);
			os.flush();
			
			return state;
		}
	}

	private GameState generateExplorationState() throws IOException
	{
		Player p1 = new Player(new PlayerInfo("Erik",Constants.PLAYER_1_ID,true));
		Player p2 = new Player(new PlayerInfo("Archit",Constants.PLAYER_2_ID,true));
		Player p3 = new Player(new PlayerInfo("Shariar",Constants.PLAYER_3_ID,true));
		Player p4 = new Player(new PlayerInfo("Nadra",Constants.PLAYER_4_ID,true));
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		
		ArrayList<Integer> playerOrder = new ArrayList<>();
		playerOrder.add(p1.getID());
		playerOrder.add(p2.getID());
		playerOrder.add(p3.getID());
		playerOrder.add(p4.getID());
		GameState state = new GameState(true, players, playerOrder, SetupPhase.EXCHANGE_SEA_HEXES, RegularPhase.RECRUITING_CHARACTERS, p1.getID(), p1.getID(),
				CombatPhase.NO_COMBAT, Constants.PUBLIC, null);
		
		p1.addOwnedHex(state.getBoard().getHexByXY(1, 2).getHex());
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 2).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(1, 10).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(5, 10).getHex());
		
		p1.addGold(20);
		try
		{
			addThingsToHexForPlayer(5,new Point(3, 6),p1,state);
			addTreasureToHex(new Point(3,6),state);
		}
		catch (NoMoreTilesException e)
		{
			common.Logger.getErrorLogger().error("Unable to generate game state due to: ", e);
		}
		state.setCurrentSetupPhase(SetupPhase.SETUP_FINISHED);
		state.setCurrentRegularPhase(RegularPhase.COMBAT);
		state.setActivePhasePlayer(p1.getID());
		state.setActiveTurnPlayer(p1.getID());
		
		for(Player p : players)
		{
			for(ITileProperties tp : p.getOwnedHexes())
			{
				state.getBoard().getHexStateForHex(tp).setMarker(Constants.getPlayerMarker( p.getID()));
			}
		}
		
		try(FileOutputStream fs = new FileOutputStream(fileName);ObjectOutputStream os = new ObjectOutputStream(fs))
		{
			os.writeObject(state);
			os.flush();
			
			return state;
		}
	}

	private GameState generateMovementState() throws IOException
	{
		Player p1 = new Player(new PlayerInfo("Erik",Constants.PLAYER_1_ID,true));
		Player p2 = new Player(new PlayerInfo("Archit",Constants.PLAYER_2_ID,true));
		Player p3 = new Player(new PlayerInfo("Shariar",Constants.PLAYER_3_ID,true));
		Player p4 = new Player(new PlayerInfo("Nadra",Constants.PLAYER_4_ID,true));
		
		HashSet<Player> players = new HashSet<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		players.add(p4);
		
		ArrayList<Integer> playerOrder = new ArrayList<>();
		playerOrder.add(p1.getID());
		playerOrder.add(p2.getID());
		playerOrder.add(p3.getID());
		playerOrder.add(p4.getID());
		GameState state = new GameState(true, players, playerOrder, SetupPhase.EXCHANGE_SEA_HEXES, RegularPhase.RECRUITING_CHARACTERS, p1.getID(), p1.getID(),
				CombatPhase.NO_COMBAT, Constants.PUBLIC, null);
		
		p1.addOwnedHex(state.getBoard().getHexByXY(1, 2).getHex());
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 2).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(1, 10).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(5, 10).getHex());

		p1.addOwnedHex(state.getBoard().getHexByXY(3, 6).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(2, 7).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(2, 5).getHex());
		try
		{
			addThingsToHexForPlayer(5,new Point(3, 6),p1,state);
			addThingByNameToHexForPlayer("Flying_Squirrel",new Point(3, 6),p1,state);
			addThingByNameToHexForPlayer("Great_Owl",new Point(3, 6),p1,state);
			addThingByNameToHexForPlayer("Pixies",new Point(3, 6),p1,state);
		}
		catch (NoMoreTilesException e)
		{
			common.Logger.getErrorLogger().error("Unable to generate game state due to: ", e);
		}
		state.setCurrentSetupPhase(SetupPhase.SETUP_FINISHED);
		state.setCurrentRegularPhase(RegularPhase.MOVEMENT);
		state.setActivePhasePlayer(p1.getID());
		state.setActiveTurnPlayer(p1.getID());
		
		for(Player p : players)
		{
			for(ITileProperties tp : p.getOwnedHexes())
			{
				state.getBoard().getHexStateForHex(tp).setMarker(Constants.getPlayerMarker( p.getID()));
			}
		}
		
		try(FileOutputStream fs = new FileOutputStream(fileName);ObjectOutputStream os = new ObjectOutputStream(fs))
		{
			os.writeObject(state);
			os.flush();
			
			return state;
		}
	}

	private void addTreasureToHex(Point loc, GameState state) throws NoMoreTilesException
	{
		ITileProperties nextThing = state.getCup().drawTile();
		ArrayList<ITileProperties> removedThings = new ArrayList<>();
		while(!nextThing.isTreasure() || nextThing.isSpecialIncomeCounter())
		{
			removedThings.add(nextThing);
			nextThing = state.getCup().drawTile();
		}
		for(ITileProperties thing : removedThings)
		{
			state.getCup().reInsertTile(thing);
		}
		state.getBoard().getHexByXY(loc.x, loc.y).addThingToHexForExploration(nextThing);
	}
	
	private void addThingsToHexForPlayer(int count, Point loc, Player p, GameState state) throws NoMoreTilesException
	{
		ArrayList<ITileProperties> removedThings = new ArrayList<>();
		for(int i=0; i<count; i++)
		{
			ITileProperties thing = state.getCup().drawTile();
			while(!thing.isCreature())
			{
				removedThings.add(thing);
				thing = state.getCup().drawTile();
			}
			p.addOwnedThingOnBoard(thing);
			state.getBoard().getHexByXY(loc.x,loc.y).addThingToHex(thing);
		}
		for(ITileProperties thing : removedThings)
		{
			state.getCup().reInsertTile(thing);
		}
	}
	
	private void addBuildingToHexForPlayer(int x, int y, Player p, BuildableBuilding b, GameState state)
	{
		ITileProperties building = BuildableBuildingGenerator.createBuildingTileForType(b);
		state.getBoard().getHexByXY(x, y).addThingToHex(building);
		p.addOwnedThingOnBoard(building);
	}

	private void addThingByNameToHexForPlayer(String name, Point loc, Player p, GameState state) throws NoMoreTilesException
	{
		ArrayList<ITileProperties> removedThings = new ArrayList<>();
		ITileProperties nextThing = state.getCup().drawTile();
		while(!nextThing.getName().equals(name))
		{
			removedThings.add(nextThing);
			nextThing = state.getCup().drawTile();
		}
		p.addOwnedThingOnBoard(nextThing);
		state.getBoard().getHexByXY(loc.x,loc.y).addThingToHex(nextThing);
		
		for(ITileProperties thing : removedThings)
		{
			state.getCup().reInsertTile(thing);
		}
	}
}

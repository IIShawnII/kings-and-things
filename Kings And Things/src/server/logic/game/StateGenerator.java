package server.logic.game;

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
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.game.ITileProperties;
import common.game.Player;
import common.game.PlayerInfo;

public class StateGenerator
{
	private final String fileName;
	private final boolean isLoadOperation;
	private final GameState generatedState;
	
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
		
		p1.addOwnedHex(state.getBoard().getHexByXY(1, 2).getHex());
		p2.addOwnedHex(state.getBoard().getHexByXY(5, 2).getHex());
		p3.addOwnedHex(state.getBoard().getHexByXY(1, 10).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(5, 10).getHex());

		p1.addOwnedHex(state.getBoard().getHexByXY(3, 6).getHex());
		p1.addOwnedHex(state.getBoard().getHexByXY(2, 5).getHex());
		p4.addOwnedHex(state.getBoard().getHexByXY(2, 7).getHex());
		try
		{
			addTenThingsToHexForPlayer(p1,state);
			addTenThingsToHexForPlayer(p2,state);
			addTenThingsToHexForPlayer(p3,state);
			addTenThingsToHexForPlayer(p4,state);
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
	
	private GameState loadStateFromFile() throws ClassNotFoundException, FileNotFoundException, IOException
	{
		try(FileInputStream fs = new FileInputStream(fileName);ObjectInputStream ois = new ObjectInputStream(fs))
		{
			return (GameState) ois.readObject();
		}
	}
	
	private void addTenThingsToHexForPlayer(Player p, GameState state) throws NoMoreTilesException
	{
		for(int i=0; i<10; i++)
		{
			ITileProperties thing = state.getCup().drawTile();
			p.addOwnedThingOnBoard(thing);
			state.getBoard().getHexByXY(3, 6).addThingToHex(thing);
		}
	}
}

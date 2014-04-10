package common.event.network;

import java.util.Set;
import java.util.HashMap;

import common.game.Player;
import common.game.HexState;
import common.game.PlayerInfo;
import common.game.ITileProperties;
import common.Constants.SetupPhase;
import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.event.AbstractNetwrokEvent;

public class GameStateProgress extends AbstractNetwrokEvent{

	private static final long serialVersionUID = -9167218483656968836L;

	private boolean isFlipped;
	private SetupPhase setup;
	private CombatPhase combat;
	private RegularPhase regular;
	private HexState[] hexes; 
	private PlayerInfo[] players;
	private ITileProperties[] special;
	private HashMap< Integer, Set<ITileProperties>> racks;
	
	public GameStateProgress(){
		super();
	}
	
	public void setPhases( SetupPhase setup, RegularPhase regular, CombatPhase combat){
		this.setup = setup;
		this.regular = regular;
		this.combat = combat;
	}
	
	public void setFlipped( boolean flipped){
		isFlipped = flipped;
	}
	
	public void setPlayersAndRacks( Set<Player> players){
		int index = 0;
		this.players = new PlayerInfo[ players.size()];
		racks = new HashMap<Integer, Set<ITileProperties>>( players.size());
		for( Player p : players){
			this.players[index] = p.getPlayerInfo();
			racks.put( p.getID(), p.getTrayThings());
			index++;
		}
	}
	
	/**
	 * this method is used to set and get array, input zero or
	 * negative integer to get the current array, or a positive
	 * none-zero integer to get a new empty array to fill.
	 * @param size - positive for size of new array, negative for current array
	 * @return empty or current array depending on argument provided
	 */
	public ITileProperties[] getSpecial( int size){
		if(size>0){
			special = new ITileProperties[ size];
		}
		return special;
	}
	
	/**
	 * this method is used to set and get array, input zero or
	 * negative integer to get the current array, or a positive
	 * none-zero integer to get a new empty array to fill.
	 * @param size - positive for size of new array, negative for current array
	 * @return empty or current array depending on argument provided
	 */
	public HexState[] getHexes( int size){
		if(size>0){
			hexes = new HexState[ size];
		}
		return hexes;
	}
	
	public ITileProperties[] getRack( final int ID){
		Set<ITileProperties> rack = racks.get( ID);
		return rack.toArray( new ITileProperties[0]);
	}
	
	public PlayerInfo[] getPlayers(){
		return players;
	}
		
	
	public boolean isFlipped() {
		return isFlipped;
	}

	
	public SetupPhase getSetup() {
		return setup;
	}

	
	public RegularPhase getRegular() {
		return regular;
	}

	
	public CombatPhase getCombat() {
		return combat;
	}

	@Override
	public String toString(){
		return "Network/GameState";
	}
}

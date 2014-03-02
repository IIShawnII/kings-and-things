package client.event;

import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.event.AbstractEvent;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.PlayerInfo;

public class BoardUpdate extends AbstractEvent {

	private int[] list;
	private boolean flipAll=false;
	private HexState[] hexes;
	private PlayerInfo current;
	private PlayerInfo[] players;
	private ITileProperties[] props;
	
	private SetupPhase setup = null;
	private CombatPhase combat = null;
	private RegularPhase regular = null;
	
	public BoardUpdate( boolean flipAll){
		this.flipAll = flipAll;
		hexes = null;
	}
	
	public BoardUpdate( SetupPhase phase) {
		setup = phase;
	}
	
	public BoardUpdate( ITileProperties[] array) {
		props = array;
	}
	
	public BoardUpdate( HexState[] array) {
		hexes = array;
	}
	
	public BoardUpdate( int[] list) {
		this.list = list;
	}
	
	public BoardUpdate( PlayerInfo[] players) {
		this( players, null, null, null);
	}
	
	public BoardUpdate( PlayerInfo[] players, PlayerInfo current) {
		this( players, null, null, null);
		this.current = current;
	}
	
	private BoardUpdate( PlayerInfo[] infos, SetupPhase setup, RegularPhase regular, CombatPhase combat){
		this.setup = setup;
		this.regular = regular;
		this.combat = combat;
		this.players = infos;
	}
	
	public BoardUpdate( PlayerInfo[] infos, SetupPhase phase){
		this( infos, phase, null, CombatPhase.NO_COMBAT);
	}
	
	public BoardUpdate( PlayerInfo[] infos, RegularPhase phase){
		this( infos, SetupPhase.SETUP_FINISHED, phase, CombatPhase.NO_COMBAT);
	}
	
	public BoardUpdate( PlayerInfo[] infos, CombatPhase phase){
		this( infos, SetupPhase.SETUP_FINISHED, RegularPhase.COMBAT, phase);
	}

	
	public SetupPhase getSetup() {
		return setup;
	}
	
	public boolean isSetupPhase(){
		return setup!=null && setup!=SetupPhase.SETUP_FINISHED;
	}

	public RegularPhase getRegular() {
		return regular;
	}
	
	public boolean isRegularPhase(){
		return regular!=null && setup==SetupPhase.SETUP_FINISHED;
	}

	public CombatPhase getCombat() {
		return combat;
	}
	
	public boolean isCombatPhase(){
		return combat!=null && combat!=CombatPhase.NO_COMBAT;
	}
	
	public boolean hasPlayerInfo(){
		return players!=null;
	}

	public PlayerInfo[] getPlayers(){
		return players;
	}

	public PlayerInfo getCurrent(){
		return current;
	}

	public ITileProperties[] getTileProperties(){
		return props;
	}

	public HexState[] getHexes(){
		return hexes;
	}

	public int[] getPlayerOrder(){
		return list;
	}
	
	public boolean hasHexes(){
		return hexes!=null;
	}
	
	public boolean flipAll(){
		return flipAll;
	}
	
	public boolean isPlayerOder(){
		return list!=null;
	}

	public boolean isRack() {
		return props!=null;
	}
}

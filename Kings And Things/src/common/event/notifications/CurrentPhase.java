package common.event.notifications;

import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.event.AbstractNetwrokEvent;
import common.game.PlayerInfo;

public class CurrentPhase extends AbstractNetwrokEvent {

	private static final long serialVersionUID = 8886517362195787165L;
	
	private SetupPhase setup = null;
	private RegularPhase regular = null;
	private CombatPhase combat = null;
	private PlayerInfo[] infos;
	
	private CurrentPhase( PlayerInfo[] infos, SetupPhase setup, RegularPhase regular, CombatPhase combat){
		this.setup = setup;
		this.regular = regular;
		this.combat = combat;
		this.infos = infos;
	}
	
	public CurrentPhase( PlayerInfo[] infos, SetupPhase phase){
		this( infos, phase, null, CombatPhase.NO_COMBAT);
	}
	
	public CurrentPhase( PlayerInfo[] infos, RegularPhase phase){
		this( infos, SetupPhase.SETUP_FINISHED, phase, CombatPhase.NO_COMBAT);
	}
	
	public CurrentPhase( PlayerInfo[] infos, CombatPhase phase){
		this( infos, SetupPhase.SETUP_FINISHED, null, phase);
	}

	
	public SetupPhase getSetup() {
		return setup;
	}
	
	public boolean isSetupPhase(){
		return setup!=SetupPhase.SETUP_FINISHED;
	}

	public RegularPhase getRegular() {
		return regular;
	}
	
	public boolean isRegularPhase(){
		return setup==SetupPhase.SETUP_FINISHED;
	}

	public CombatPhase getCombat() {
		return combat;
	}
	
	public boolean isCombatPhase(){
		return regular==RegularPhase.COMBAT&&combat!=CombatPhase.NO_COMBAT;
	}
	
	public PlayerInfo[] getPlayers(){
		return infos;
	}
	
	@Override
	public String toString(){
		return "Network/CurrentPhase: " + (isSetupPhase()?setup:(isCombatPhase()?combat:regular));
	}
}

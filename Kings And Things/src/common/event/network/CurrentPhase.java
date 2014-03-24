package common.event.network;

import common.Constants.CombatPhase;
import common.Constants.RegularPhase;
import common.Constants.SetupPhase;
import common.event.AbstractNetwrokEvent;
import common.game.PlayerInfo;

public class CurrentPhase<T extends Enum<?>> extends AbstractNetwrokEvent {

	private static final long serialVersionUID = -8772101566749846648L;
	
	private T phase = null;
	private PlayerInfo[] players;
	
	public CurrentPhase( PlayerInfo[] players, T phase){
		this.phase = phase;
		this.players = players;
	}

	public T getPhase() {
		return phase;
	}
	
	public boolean isSetupPhase(){
		return SetupPhase.class==phase.getClass();
	}
	
	public boolean isRegularPhase(){
		return RegularPhase.class==phase.getClass();
	}
	
	public boolean isCombatPhase(){
		return CombatPhase.class==phase.getClass();
	}
	
	public PlayerInfo[] getPlayers(){
		return players;
	}
	
	@Override
	public String toString(){
		return "Network/CurrentPhase: " + phase;
	}
}

package common.event.notifications;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import common.event.AbstractEvent;

@XmlRootElement
public class ActivePlayerChanged extends AbstractEvent{

	private static final long serialVersionUID = -6193606863154860188L;
	
	@XmlAttribute
	private final int activePhasePlayer;
	@XmlAttribute
	private final int activeTurnPlayer;
	
	public ActivePlayerChanged(int activePhasePlayer, int activeTurnPlayer){
		this.activePhasePlayer = activePhasePlayer;
		this.activeTurnPlayer = activeTurnPlayer;
	}

	public int getActiveTurnPlayer(){
		return activeTurnPlayer;
	}
	
	public int getActivePhasePlayer(){
		return activePhasePlayer;
	}
}

package common.event.notifications;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ActivePlayerChangedNotification extends AbstractNotification
{
	@XmlAttribute
	private final int activePhasePlayer;
	@XmlAttribute
	private final int activeTurnPlayer;
	
	public ActivePlayerChangedNotification(int activePhasePlayer, int activeTurnPlayer)
	{
		super();
		this.activePhasePlayer = activePhasePlayer;
		this.activeTurnPlayer = activeTurnPlayer;
	}

	public int getActiveTurnPlayer()
	{
		return activeTurnPlayer;
	}
	
	public int getActivePhasePlayer()
	{
		return activePhasePlayer;
	}
}

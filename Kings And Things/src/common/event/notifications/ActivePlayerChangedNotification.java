package common.event.notifications;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ActivePlayerChangedNotification extends Notification
{
	@XmlAttribute
	private final int activePhasePlayer;
	@XmlAttribute
	private final int activeTurnPlayer;
	
	public ActivePlayerChangedNotification(int activePhasePlayer, int activeTurnPlayer)
	{
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

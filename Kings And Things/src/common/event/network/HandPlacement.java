package common.event.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import common.event.AbstractNetwrokEvent;
import common.game.ITileProperties;

public class HandPlacement extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = 342396100596288007L;
	private final HashSet<ITileProperties> cardsInHand;
	
	public HandPlacement(Collection<ITileProperties> cardsInHand)
	{
		this.cardsInHand = new HashSet<ITileProperties>(cardsInHand);
		for(ITileProperties card : cardsInHand)
		{
			if(!card.isBuilding() && !card.isSpecialCharacter() && !card.isFaceUp())
			{
				card.flip();
			}
		}
	}
	
	public Set<ITileProperties> getCardsInHand()
	{
		return Collections.unmodifiableSet(cardsInHand);
	}
}

package common.event.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Constants.CombatPhase;
import common.event.AbstractNetwrokEvent;
import common.game.HexState;
import common.game.Player;

public class InitiateCombat extends AbstractNetwrokEvent
{
	private static final long serialVersionUID = -7434684705634535670L;
	
	private final HexState combatHex;
	private final HashSet<Player> playersInvolved;
	private final int defenderID;
	private final ArrayList<Integer> playerOrder;
	private final CombatPhase currPhase;
	
	public InitiateCombat(HexState combatHex, Collection<Player> playersInvolved, int defenderID, Collection<Integer> playerOrder, CombatPhase currPhase)
	{
		this.combatHex = combatHex;
		this.defenderID = defenderID;
		this.playersInvolved = new HashSet<Player>(playersInvolved);
		this.playerOrder = new ArrayList<Integer>(playerOrder);
		this.currPhase = currPhase;
	}
	
	public HexState getCombatHexState()
	{
		return combatHex;
	}
	
	public Set<Player> getInvolvedPlayers()
	{
		return Collections.unmodifiableSet(playersInvolved);
	}
	
	public Player getDefendingPlayer()
	{
		for(Player p : playersInvolved)
		{
			if(p.getID() == defenderID)
			{
				return p;
			}
		}
		return null;
	}
	
	public int getDefendingPlayerID()
	{
		return defenderID;
	}
	
	public CombatPhase getCurrentCombatPhase()
	{
		return currPhase;
	}
	
	public List<Integer> getPlayerOrder()
	{
		return Collections.unmodifiableList(playerOrder);
	}
}

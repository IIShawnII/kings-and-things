package client.gui.components.combat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;
import common.event.EventDispatch;
import common.event.network.CombatHits;
import common.event.network.PlayerTargetChanged;
import common.game.ITileProperties;

public abstract class AbstractCombatArmyPanel extends JPanel
{
	private static final long serialVersionUID = 468496960335415903L;
	private static final String HITS_TAKEN_STRING = "Damage taken: ";
	
	private final String playerName;
	private String targetPlayerName;
	private final HashMap<ITileProperties,JButton> army;
	private final JLabel targetArmyLabel;
	private final JLabel armyLabel;
	private final JLabel hitsToApply;
	private final int playerID;
	private JPanel mainArmyPanel;
	private JPanel armyHeaderPanel;
	
	public AbstractCombatArmyPanel(String playerName, int playerID, String targetPlayerName)
	{
		this.playerName = playerName;
		this.targetPlayerName = targetPlayerName;
		this.army = new HashMap<ITileProperties,JButton>();
		this.playerID = playerID;
		
		armyLabel = new JLabel(playerName + "'s rag tag army");
		armyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		armyLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		targetArmyLabel = new JLabel(generateTargetLabelString());
		targetArmyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		targetArmyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		
		hitsToApply = new JLabel(HITS_TAKEN_STRING + 0);
		hitsToApply.setHorizontalAlignment(SwingConstants.CENTER);
		hitsToApply.setHorizontalTextPosition(SwingConstants.CENTER);
	}
	
	public void init(Collection<ITileProperties> things)
	{
		mainArmyPanel = generateMainArmyPanel(things);
		armyHeaderPanel = generateArmyHeaderPanel();
		
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		
		add(armyHeaderPanel,constraints);
		
		constraints.gridy++;
		constraints.gridwidth = 1;
		constraints.weighty = 1;
		
		add(mainArmyPanel,constraints);
		
		EventDispatch.registerOnInternalEvents(this);
	}
	
	public void setHitsToApply(int hitCount)
	{
		hitsToApply.setText(HITS_TAKEN_STRING + hitCount);
	}
	
	public void setTargetPlayerName(String targetPlayerName)
	{
		this.targetPlayerName = targetPlayerName;
		targetArmyLabel.setText(generateTargetLabelString());
	}
	
	public void removeThingsNotInList(Collection<ITileProperties> things, boolean retreat)
	{
		if(army.size()>0)
		{
			Iterator<Entry<ITileProperties,JButton>> it = army.entrySet().iterator();
			while(it.hasNext())
			{
				Entry<ITileProperties,JButton> entry = it.next();
				if(!things.contains(entry.getKey()))
				{
					mainArmyPanel.remove(entry.getValue());
					thingRemoved(entry.getKey());
					it.remove();
				}
			}
			if(army.size()==0)
			{
				targetArmyLabel.setText(retreat?"Ran like a wipped dog":"Has been destroyed!");
				armyHeaderPanel.remove(hitsToApply);
				EventDispatch.unregisterFromInternalEvents(this);
			}
		}
	}
	
	public int getPlayerID()
	{
		return playerID;
	}
	
	public String getPlayerName()
	{
		return playerName;
	}
	
	public String getTargetPlayerName()
	{
		return targetPlayerName;
	}

	protected void thingRemoved(ITileProperties thing)
	{
	}
	
	protected JLabel getArmyLabel()
	{
		return armyLabel;
	}

	protected JLabel getTargetArmyLabel()
	{
		return targetArmyLabel;
	}
	
	protected JLabel getHitsToApplyLabel()
	{
		return hitsToApply;
	}
	
	protected JPanel getMainArmyPanel()
	{
		return mainArmyPanel;
	}
	
	protected JPanel getArmyHeaderPanel()
	{
		return armyHeaderPanel;
	}
	
	protected void addArmyMapping(JButton button, ITileProperties thing)
	{
		army.put(thing, button);
	}
	
	protected abstract JPanel generateArmyHeaderPanel();
	protected abstract JPanel generateMainArmyPanel(Collection<ITileProperties> things);
	
	private String generateTargetLabelString()
	{
		return "Targetting " + targetPlayerName + "'s rag tag army";
	}

	@Subscribe
	public void recieveCombatHits(final CombatHits evt)
	{
		if(evt.getPlayerReceivingHitID() == getPlayerID())
		{
			Runnable logic = new Runnable(){
				@Override
				public void run(){
					setHitsToApply(evt.getNumberOfHits());
				}
			};
			if(!SwingUtilities.isEventDispatchThread())
			{
				SwingUtilities.invokeLater(logic);
			}
			else
			{
				logic.run();
			}
		}
	}
	
	@Subscribe
	public void recieveTargetChanged(final PlayerTargetChanged evt)
	{
		if(evt.getTargettingPlayer().getID() == getPlayerID())
		{
			Runnable logic = new Runnable(){
				@Override
				public void run(){
					setTargetPlayerName(evt.getPlayersTarget().getName());
				}
			};
			if(!SwingUtilities.isEventDispatchThread())
			{
				SwingUtilities.invokeLater(logic);
			}
			else
			{
				logic.run();
			}
		}
	}
}

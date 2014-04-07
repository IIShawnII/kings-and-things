package client.gui.components.combat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.CombatPhase;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.CurrentPhase;
import common.event.network.HexStatesChanged;
import common.event.network.InitiateCombat;
import common.game.HexState;
import common.game.ITileProperties;

public class ExplorationResultsPanel extends JPanel
{
	private static final long serialVersionUID = 5153140105269319475L;
	
	private final HashMap<ITileProperties,LabelButtonPair> creatures;
	private final int explorerID;
	private final JFrame parent;
	private HexState hs;

	public ExplorationResultsPanel(int explorerID, HexState hex, JFrame parent)
	{
		creatures = new HashMap<>();
		this.explorerID = explorerID;
		hs = hex;
		this.parent = parent;
		parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	public void init(Collection<ITileProperties> thingsInHex)
	{
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		
		JLabel resultsLabel = new JLabel("Exploration results");
		resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		resultsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		add(resultsLabel,constraints);
		
		constraints.gridy++;
		constraints.gridwidth = 1;
		constraints.weighty = 1;
		
		for(final ITileProperties thing : thingsInHex)
		{
			ImageIcon image = new ImageIcon(Constants.getImageForTile(thing));
			
			JLabel imageLabel = new JLabel(image);
			add(imageLabel,constraints);
			
			if(thing.isCreature())
			{
				constraints.gridy++;
				JButton bribe = new JButton("Bribe");
				bribe.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						new UpdatePackage(UpdateInstruction.BribeCreature, UpdateKey.ThingArray, new ITileProperties[]{thing}, "Exploration results panel for: " + explorerID).postNetworkEvent(explorerID);
					}});
				add(bribe,constraints);
				
				creatures.put(thing, new LabelButtonPair(imageLabel,bribe));
				constraints.gridy--;
			}
			constraints.gridx++;
		}
		constraints.gridx = 0;
		constraints.gridy+=2;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weighty = 0;
		JButton done = new JButton("Done");
		done.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(creatures.size() == 0)
				{
					close();
				}
				else
				{
					new UpdatePackage(UpdateInstruction.Skip, "Combat Panel for: " + explorerID).postNetworkEvent(explorerID);
				}
			}});
		add(done,constraints);
		
		EventDispatch.registerOnInternalEvents(this);
	}
	
	private void explorationHexChanged(HexState hs)
	{
		this.hs = hs;

		Iterator<Entry<ITileProperties,LabelButtonPair>> it = creatures.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<ITileProperties,LabelButtonPair> entry = it.next();
			if(!hs.getThingsInHex().contains(entry.getKey()))
			{
				remove(entry.getValue().label);
				remove(entry.getValue().button);
				it.remove();
			}
		}
		parent.validate();
	}
	
	private void close()
	{
		EventDispatch.unregisterFromInternalEvents(this);
		parent.dispose();
	}

	@Subscribe
	public void recieveInitiateCombat(final InitiateCombat evt)
	{
		Runnable logic = new Runnable(){
			@Override
			public void run(){
				close();
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

	@Subscribe
	public void recieveCombatPhase(final CurrentPhase<CombatPhase> evt)
	{
		switch(evt.getPhase())
		{
			case BRIBE_CREATURES:
			{
				break;
			}
			default:
			{
				Runnable logic = new Runnable(){
					@Override
					public void run(){
						close();
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

	@Subscribe
	public void recieveHexChanged(final HexStatesChanged evt)
	{
		for(final HexState hs : evt.getArray())
		{
			if(hs.getHex().equals(this.hs.getHex()))
			{
				Runnable logic = new Runnable(){
					@Override
					public void run(){
						explorationHexChanged(hs);
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

	private class LabelButtonPair
	{
		private final JLabel label;
		private final JButton button;
		
		public LabelButtonPair(JLabel l, JButton b)
		{
			label = l;
			button = b;
		}
	}
}

package client.gui.components;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import common.game.ITileProperties;

public class TileSelectionPanel extends JPanel
{
	private static final long serialVersionUID = 2315187271759152079L;
	private final HashSet<ITileProperties> selectedThings;
	private final HashMap<ITileProperties,TileButton> buttons;
	private final JFrame parent;
	
	public TileSelectionPanel(final JFrame parent, String headerLabel, Collection<ITileProperties> thingsInHex,final ISelectionListener<ITileProperties> listener)
	{
		selectedThings = new HashSet<>();
		buttons = new HashMap<>();
		this.parent = parent;

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
		
		JLabel header = new JLabel(headerLabel);
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setHorizontalTextPosition(SwingConstants.CENTER);
		add(header,constraints);
		
		constraints.gridwidth = 1;
		constraints.weighty = 1;
		constraints.gridy++;
		
		for(ITileProperties thing : thingsInHex)
		{
			final TileButton tile = new TileButton(thing);
			tile.setBorder(null);
			tile.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					boolean alreadyAdded = selectedThings.contains(tile.getTile());
					if(alreadyAdded)
					{
						selectedThings.remove(tile.getTile());
						tile.setBorder(null);
					}
					else
					{
						selectedThings.add(tile.getTile());
						tile.setBorder(new LineBorder(Color.GREEN, 3, true));
					}
					listener.selectionChanged(Collections.unmodifiableSet(selectedThings));
				}});
			add(tile,constraints);
			buttons.put(thing,tile);
			constraints.gridx++;
		}
		parent.validate();
	}
	
	public void removeThingsNotInList(Collection<ITileProperties> things)
	{
		Iterator<Entry<ITileProperties,TileButton>> it = buttons.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<ITileProperties,TileButton> nextEntry = it.next();
			if(!things.contains(nextEntry.getKey()))
			{
				remove(nextEntry.getValue());
				it.remove();
			}
		}
		
		Iterator<ITileProperties> iter = selectedThings.iterator();
		while(iter.hasNext())
		{
			if(!things.contains(iter.next()))
			{
				iter.remove();
			}
		}
		parent.validate();
	}
	
	public int getNumThingsRemaining()
	{
		return buttons.size();
	}
}

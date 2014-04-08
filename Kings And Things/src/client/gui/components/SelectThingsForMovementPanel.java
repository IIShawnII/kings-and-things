package client.gui.components;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import common.game.ITileProperties;

public class SelectThingsForMovementPanel extends JPanel
{
	private static final long serialVersionUID = 2315187271759152079L;
	private final HashSet<ITileProperties> selectedThings;
	
	public SelectThingsForMovementPanel(final JFrame parent, Collection<ITileProperties> thingsInHex,final ISelectionListener<ITileProperties> listener)
	{
		selectedThings = new HashSet<>();

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
		
		JLabel header = new JLabel("Select things to move");
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setHorizontalTextPosition(SwingConstants.CENTER);
		add(header,constraints);
		
		constraints.gridwidth = 1;
		constraints.weighty = 1;
		constraints.gridy++;
		
		for(ITileProperties thing : thingsInHex)
		{
			if(!thing.isFaceUp() && thing.isCreature() && !thing.isSpecialCharacter())
			{
				thing.flip();
			}
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
			constraints.gridx++;
		}
	}
}

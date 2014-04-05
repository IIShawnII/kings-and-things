package client.gui.components.combat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.Constants;
import common.game.ITileProperties;

public class ExplorationResultsPanel extends JPanel
{
	private static final long serialVersionUID = 5153140105269319475L;

	public ExplorationResultsPanel(Collection<ITileProperties> results)
	{
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		for(ITileProperties thing : results)
		{
			ImageIcon image = null;
			if(!thing.isBuildableBuilding())
			{
				image = new ImageIcon(Constants.IMAGES.get(thing.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
			}
			else
			{
				for(ITileProperties b : Constants.BUILDING.values())
				{
					if(b.getName().equals(thing.getName()))
					{
						image = new ImageIcon(Constants.IMAGES.get(b.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
					}
				}
			}
			
			JLabel imageLabel = new JLabel(image);
			add(imageLabel,constraints);
			constraints.gridx++;
		}
	}
}

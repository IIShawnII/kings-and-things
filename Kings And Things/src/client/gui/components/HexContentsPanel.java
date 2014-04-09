package client.gui.components;

import java.awt.FlowLayout;
import java.awt.Image;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.Constants;
import common.game.ITileProperties;

public class HexContentsPanel extends JPanel
{
	private static final long serialVersionUID = 7410672134640031418L;
	
	/**
	 * Simple panel for displaying a hex's contents
	 * @param model The hex to display contents of
	 */
	public HexContentsPanel(Collection<ITileProperties> contents)
	{
		init(contents);
	}
	
	private void init(Collection<ITileProperties> contents)
	{
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		for(ITileProperties tp : contents)
		{
			Image tileImage = Constants.getImageForTile(tp);
			add(new JLabel(new ImageIcon(tileImage)));
		}
	}
}

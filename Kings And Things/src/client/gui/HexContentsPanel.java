package client.gui;

import java.awt.FlowLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.Constants;
import common.game.HexState;
import common.game.ITileProperties;

public class HexContentsPanel extends JPanel
{
	private static final long serialVersionUID = 7410672134640031418L;
	
	private HexState model;
	
	/**
	 * Simple panel for displaying a hex's contents
	 * @param model The hex to display contents of
	 */
	public HexContentsPanel(HexState model)
	{
		this.model = model;
		init();
	}
	
	private void init()
	{
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		for(ITileProperties tp : model.getThingsInHex())
		{
			Image tileImage = Constants.IMAGES.get(tp.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT);
			add(new JLabel(new ImageIcon(tileImage)));
		}
	}
}

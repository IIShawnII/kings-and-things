package client.gui.components;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import common.Constants;
import common.game.ITileProperties;

public class TileButton extends JButton {

	private static final long serialVersionUID = 2060049574719702711L;
	private final ITileProperties tile;
	
	public TileButton(ITileProperties tile)
	{
		super(getIconForTile(tile));
		this.tile = tile;
	}
	
	public ITileProperties getTile()
	{
		return tile;
	}
	
	private static ImageIcon getIconForTile(ITileProperties tile)
	{
		ImageIcon image = null;
		if(tile.isCreature())
		{
			image = new ImageIcon(Constants.IMAGES.get(tile.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
		}
		else
		{
			for(ITileProperties b : Constants.BUILDING.values())
			{
				if(b.getName().equals(tile.getName()))
				{
					image = new ImageIcon(Constants.IMAGES.get(b.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
				}
			}
		}
		
		return image;
	}
}

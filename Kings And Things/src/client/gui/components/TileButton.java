package client.gui.components;

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
		ImageIcon image = new ImageIcon(Constants.getImageForTile(tile));
		return image;
	}
}

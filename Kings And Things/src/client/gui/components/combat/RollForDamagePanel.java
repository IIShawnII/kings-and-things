package client.gui.components.combat;

import static common.Constants.DICE_SIZE;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import client.gui.die.DiceRoller;

import common.Constants;
import common.Constants.RollReason;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.Roll;

public class RollForDamagePanel extends JPanel
{
	private static final long serialVersionUID = -2881029431754333752L;
	
	private final HashMap<ITileProperties, DiceRoller> rollerMap;
	private final HexState hex;
	private final Player p;
	
	public RollForDamagePanel(HexState hs, Player p)
	{
		hex = hs;
		this.p = p;
		rollerMap = new HashMap<>(2);
		init();
	}
	
	private void init()
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
		constraints.weighty = 0;
		
		if(hex.hasBuilding())
		{
			ImageIcon image = null;
			for(ITileProperties b : Constants.BUILDING.values())
			{
				if(b.getName().equals(hex.getBuilding().getName()))
				{
					image = new ImageIcon(Constants.IMAGES.get(b.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
				}
			}
			JLabel tileLabel = new JLabel(image);
			add(tileLabel,constraints);
			
			final DiceRoller roller = new DiceRoller();
			roller.init();
			Dimension d = roller.getPreferredSize();
			roller.setDiceCount(1);
			
			d.height = (int)(DICE_SIZE*1.2);
			d.width = DICE_SIZE*3;
			roller.setPreferredSize(d);
			roller.addMouseListener(new MouseListener(){
				@Override
				public void mouseClicked(MouseEvent arg0)
				{
					roller.roll();
					
					int rollValue = Integer.parseInt(JOptionPane.showInputDialog(RollForDamagePanel.this, "Select desired roll value", "RollValue", JOptionPane.PLAIN_MESSAGE));
					Roll r = new Roll(roller.getDiceCount(), hex.getBuilding(), RollReason.CALCULATE_DAMAGE_TO_TILE, p.getID(), rollValue);
					//TODO send roll command to server
				}
				@Override
				public void mouseEntered(MouseEvent arg0)
				{
					roller.expand();
				}
				@Override
				public void mouseExited(MouseEvent arg0)
				{
					roller.shrink();
				}
				@Override
				public void mousePressed(MouseEvent arg0)
				{
				}
				@Override
				public void mouseReleased(MouseEvent arg0)
				{
				}});
			
			constraints.gridx++;
			constraints.weightx = 0;
			constraints.weighty = 0;
			add(roller,constraints);
			
			rollerMap.put(hex.getBuilding(), roller);

			constraints.gridx--;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.gridy++;
		}

		if(hex.hasSpecialIncomeCounter())
		{
			ImageIcon image = new ImageIcon(Constants.IMAGES.get(hex.getSpecialIncomeCounter().hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
			JLabel tileLabel = new JLabel(image);
			add(tileLabel,constraints);
			
			final DiceRoller roller = new DiceRoller();
			roller.init();
			Dimension d = roller.getPreferredSize();
			roller.setDiceCount(1);
			
			d.height = (int)(DICE_SIZE*1.2);
			d.width = DICE_SIZE*3;
			roller.setPreferredSize(d);
			roller.addMouseListener(new MouseListener(){
				@Override
				public void mouseClicked(MouseEvent arg0)
				{
					roller.roll();
					
					int rollValue = Integer.parseInt(JOptionPane.showInputDialog(RollForDamagePanel.this, "Select desired roll value", "RollValue", JOptionPane.PLAIN_MESSAGE));
					Roll r = new Roll(roller.getDiceCount(), hex.getSpecialIncomeCounter(), RollReason.CALCULATE_DAMAGE_TO_TILE, p.getID(), rollValue);
					//TODO send roll command to server
				}
				@Override
				public void mouseEntered(MouseEvent arg0)
				{
					roller.expand();
				}
				@Override
				public void mouseExited(MouseEvent arg0)
				{
					roller.shrink();
				}
				@Override
				public void mousePressed(MouseEvent arg0)
				{
				}
				@Override
				public void mouseReleased(MouseEvent arg0)
				{
				}});
			
			constraints.gridx++;
			constraints.weightx = 0;
			constraints.weighty = 0;
			add(roller,constraints);
			
			rollerMap.put(hex.getSpecialIncomeCounter(), roller);

			constraints.gridx--;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.gridy++;
		}
	}
}

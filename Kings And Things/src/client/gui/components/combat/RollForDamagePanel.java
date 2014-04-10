package client.gui.components.combat;

import static common.Constants.DICE_SIZE;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import client.gui.die.DiceRoller;

import com.google.common.eventbus.Subscribe;
import common.Constants;
import common.Constants.CombatPhase;
import common.Constants.RollReason;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.CurrentPhase;
import common.event.network.DieRoll;
import common.event.network.HexStatesChanged;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;
import common.game.Roll;

public class RollForDamagePanel extends JPanel
{
	private static final long serialVersionUID = -2881029431754333752L;
	
	private final HashMap<ITileProperties, DiceRoller> rollerMap;
	private HexState hex;
	private final Player p;
	private final JFrame parent;
	
	public RollForDamagePanel(HexState hs, Player p, JFrame parent)
	{
		hex = hs;
		this.p = p;
		rollerMap = new HashMap<ITileProperties, DiceRoller>(2);
		this.parent = parent;
		parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	public void init()
	{
		updateAfterHexChange();
		EventDispatch.registerOnInternalEvents(this);
	}
	
	private void updateAfterHexChange()
	{
		removeAll();
		rollerMap.clear();
		
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
			ImageIcon image =  new ImageIcon(Constants.getImageForTile(hex.getBuilding()));
			
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
					new UpdatePackage(UpdateInstruction.NeedRoll, UpdateKey.Roll, r, "Determine damage panel for: " + p).postNetworkEvent(p.getID());
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
			ImageIcon image = new ImageIcon(Constants.getImageForTile(hex.getSpecialIncomeCounter()));
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
					new UpdatePackage(UpdateInstruction.NeedRoll, UpdateKey.Roll, r, "Determine damage panel for: " + p).postNetworkEvent(p.getID());
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
	
	private void close()
	{
		EventDispatch.unregisterFromInternalEvents(this);
		parent.dispose();
	}
	
	public void combatHexChanged(HexState hs)
	{
		this.hex = hs;
		updateAfterHexChange();
	}

	public void setRollResults(final Roll result)
	{
		final DiceRoller roller = rollerMap.get(result.getRollTarget());
		roller.setResult(Constants.convertToDice(result.getFinalTotal(), result.getDiceCount()));
	}

	@Subscribe
	public void recieveHexChanged(final HexStatesChanged evt)
	{
		for(final HexState hs : evt.getArray())
		{
			if(hs.getHex().equals(this.hex.getHex()))
			{
				Runnable logic = new Runnable(){
					@Override
					public void run(){
						combatHexChanged(hs);
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
	public void recieveCombatPhaseChanged(final CurrentPhase<CombatPhase> evt)
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
	public void recieveDieRoll(final DieRoll evt)
	{
		if(evt.getDieRoll().getRollingPlayerID() == p.getID())
		{
			Runnable logic = new Runnable(){
				@Override
				public void run(){
					setRollResults(evt.getDieRoll());
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

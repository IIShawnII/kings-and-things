package client.gui.components.combat;

import static common.Constants.DICE_SIZE;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import client.gui.die.DiceRoller;

import com.google.common.eventbus.Subscribe;
import common.Constants;
import common.Constants.Ability;
import common.Constants.RollReason;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.UpdatePackage;
import common.event.network.DieRoll;
import common.game.ITileProperties;
import common.game.Roll;

public class ActiveCombatArmyPanel extends AbstractCombatArmyPanel
{
	private static final long serialVersionUID = 8925191597551225983L;
	private final JButton targetArmyButton;
	private final JButton retreatButton;
	private final JButton fightOnButton;
	private final HashMap<ITileProperties, DiceRoller> rollerMap;
	
	public ActiveCombatArmyPanel(String playerName, int playerID, String targetPlayerName)
	{
		super(playerName, playerID, targetPlayerName);

		targetArmyButton = new JButton(new ImageIcon(Constants.CROSSHAIR.getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
		retreatButton = new JButton(new ImageIcon(Constants.RUN_AWAY));
		fightOnButton = new JButton(new ImageIcon(Constants.FIGHT_ON));
		rollerMap = new HashMap<>();
	}

	public void addRetreatButtonListener(ActionListener listener)
	{
		retreatButton.addActionListener(listener);
	}
	
	public void addFightOnButtonListener(ActionListener listener)
	{
		fightOnButton.addActionListener(listener);
	}
	
	public void addTargetSelectActionListener(ActionListener listener)
	{
		targetArmyButton.addActionListener(listener);
	}

	public void setRollResults(final Roll result)
	{
		final DiceRoller roller = rollerMap.get(result.getRollTarget());
		roller.setResult(Constants.convertToDice(result.getFinalTotal(), result.getDiceCount()));
	}
	
	@Override
	protected JPanel generateArmyHeaderPanel()
	{
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		headerPanel.add(getArmyLabel(),constraints);

		constraints.gridy++;
		headerPanel.add(getTargetArmyLabel(),constraints);
		
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx++;
		constraints.weightx = 0;
		constraints.weighty = 1;
		headerPanel.add(targetArmyButton,constraints);

		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx--;
		constraints.weightx = 1;
		constraints.gridy++;
		constraints.gridwidth = 1;

		headerPanel.add(getHitsToApplyLabel(),constraints);
		
		return headerPanel;
	}

	@Override
	protected JPanel generateMainArmyPanel(Collection<ITileProperties> things)
	{
		JPanel mainArmyPanel = new JPanel();
		mainArmyPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		for(final ITileProperties tile : things)
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
			JButton button = new JButton(image);
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					new UpdatePackage(UpdateInstruction.ApplyHit,UpdateKey.ThingArray,tile,"Combat Panel for: " + getPlayerName()).postNetworkEvent(getPlayerID());
				}});
			mainArmyPanel.add(button,constraints);
			
			final DiceRoller roller = new DiceRoller();
			roller.init();
			Dimension d = roller.getPreferredSize();
			if(tile.hasAbility(Ability.Charge))
			{
				roller.setDiceCount(2);
			}
			else
			{
				roller.setDiceCount(1);
			}
			d.height = (int)(DICE_SIZE*1.2);
			d.width = DICE_SIZE*3;
			roller.setPreferredSize(d);
			roller.addMouseListener(new MouseListener(){
				@Override
				public void mouseClicked(MouseEvent arg0)
				{
					roller.roll();
					
					int rollValue = Integer.parseInt(JOptionPane.showInputDialog(ActiveCombatArmyPanel.this, "Select desired roll value", "RollValue", JOptionPane.PLAIN_MESSAGE));
					Roll r = new Roll(roller.getDiceCount(), tile, RollReason.ATTACK_WITH_CREATURE, getPlayerID(), rollValue);
					new UpdatePackage(UpdateInstruction.NeedRoll, UpdateKey.Roll, r, "Combat Panel for: " + getPlayerName()).postNetworkEvent(getPlayerID());
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
			mainArmyPanel.add(roller,constraints);
			
			addArmyMapping(button,tile);
			rollerMap.put(tile, roller);

			constraints.gridx--;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.gridy++;
		}
		constraints.weighty = 0;
		constraints.gridwidth = 1;
		mainArmyPanel.add(retreatButton,constraints);
		
		constraints.gridx++;
		mainArmyPanel.add(fightOnButton,constraints);
		
		return mainArmyPanel;
	}
	
	@Override
	protected void thingRemoved(ITileProperties thing)
	{
		remove(rollerMap.remove(thing));
	}
	
	@Subscribe
	public void recieveDieRoll(final DieRoll evt)
	{
		if(evt.getDieRoll().getRollingPlayerID() == getPlayerID())
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

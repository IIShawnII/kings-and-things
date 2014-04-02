package client.gui.components;

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
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import client.gui.die.DiceRoller;

import common.Constants;
import common.Constants.Ability;
import common.Constants.RollReason;
import common.Logger;
import common.game.ITileProperties;
import common.game.Roll;

public class CombatArmyPanel extends JPanel
{
	private static final long serialVersionUID = 468496960335415903L;
	private static final String HITS_TAKEN_STRING = "Damage taken: ";
	
	private final String playerName;
	private String targetPlayerName;
	private final HashMap<JButtonTilePair,DiceRoller> army;
	private final JLabel targetArmyLabel;
	private final JLabel armyLabel;
	private final JButton targetArmyButton;
	private final JButton retreatButton;
	private final JButton fightOnButton;
	private final JLabel hitsToApply;
	private final int playerID;
	
	public CombatArmyPanel(String playerName, int playerID, String targetPlayerName, Collection<ITileProperties> army)
	{
		this.playerName = playerName;
		this.targetPlayerName = targetPlayerName;
		this.army = new HashMap<>(army.size());
		this.playerID = playerID;
		
		targetArmyLabel = new JLabel();
		armyLabel = new JLabel();
		targetArmyButton = new JButton(new ImageIcon(Constants.CROSSHAIR.getScaledInstance(60, 60, Image.SCALE_DEFAULT)));
		retreatButton = new JButton(new ImageIcon(Constants.RUN_AWAY));
		fightOnButton = new JButton(new ImageIcon(Constants.FIGHT_ON));
		hitsToApply = new JLabel();
		init(army);
	}
	
	private void init(Collection<ITileProperties> things)
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
		
		armyLabel.setText(playerName + "'s rag tag army");
		armyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		armyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		add(armyLabel,constraints);

		constraints.gridwidth = 1;
		constraints.gridy++;
		targetArmyLabel.setText(generateTargetLabelString());
		targetArmyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		targetArmyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		add(targetArmyLabel,constraints);
		
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx++;
		constraints.weightx = 0;
		add(targetArmyButton,constraints);

		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx--;
		constraints.weightx = 1;
		constraints.gridy++;
		constraints.gridwidth = 1;

		hitsToApply.setText(HITS_TAKEN_STRING + 0);
		hitsToApply.setHorizontalAlignment(SwingConstants.CENTER);
		hitsToApply.setHorizontalTextPosition(SwingConstants.CENTER);
		add(hitsToApply,constraints);

		constraints.gridy++;
		constraints.gridwidth = 1;
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
					// TODO Send apply hits command to server
				}});
			add(button,constraints);
			
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
					
					Roll r = new Roll(roller.getDiceCount(), tile, RollReason.ATTACK_WITH_CREATURE, playerID);
					//TODO Send roll to server
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
			
			army.put(new JButtonTilePair(button,tile), roller);

			constraints.gridx--;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.gridy++;
		}
		constraints.weighty = 0;
		constraints.gridwidth = 1;
		add(retreatButton,constraints);
		
		constraints.gridx++;
		add(fightOnButton,constraints);
	}
	
	public void setRollResults(Roll result)
	{
		ITileProperties tile = result.getRollTarget();
		for(Entry<JButtonTilePair,DiceRoller> entry : army.entrySet())
		{
			if(entry.getKey().tile.equals(tile))
			{
				DiceRoller roller = entry.getValue();
				//to deal with server sending instant results, wait for animation to finish first
				while(roller.isRolling())
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						Logger.getStandardLogger().warn("Ignoring thread interrupt: ", e);
					}
				}
				
				roller.setResult(Constants.convertToDice(result.getFinalTotal(), result.getDiceCount()));
			}
		}
	}
	
	public void hideInputControls()
	{
		targetArmyButton.setVisible(false);
		retreatButton.setVisible(false);
		fightOnButton.setVisible(false);
		for(DiceRoller roller : army.values())
		{
			roller.setVisible(false);
		}
	}
	
	public void setHitsToApply(int hitCount)
	{
		hitsToApply.setText(HITS_TAKEN_STRING + hitCount);
	}
	
	public void setTargetPlayerName(String targetPlayerName)
	{
		this.targetPlayerName = targetPlayerName;
		targetArmyLabel.setText(generateTargetLabelString());
	}
	
	public void removeThing(ITileProperties thing)
	{
		Iterator<Entry<JButtonTilePair,DiceRoller>> it = army.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<JButtonTilePair,DiceRoller> entry = it.next();
			if(entry.getKey().tile.equals(thing))
			{
				remove(entry.getKey().button);
				remove(entry.getValue());
				it.remove();
			}
		}
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
	
	public int getPlayerID()
	{
		return playerID;
	}
	
	public String getPlayerName()
	{
		return playerName;
	}
	
	public String getTargetPlayerName()
	{
		return targetPlayerName;
	}
	
	private String generateTargetLabelString()
	{
		return "Targetting " + targetPlayerName + "'s rag tag army";
	}
	
	private static class JButtonTilePair
	{
		private final JButton button;
		private final ITileProperties tile;
		
		private JButtonTilePair(JButton button, ITileProperties tile)
		{
			this.button = button;
			this.tile = tile;
		}
	}
}

package client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import server.event.commands.ApplyHitsCommand;
import server.event.commands.RollDiceCommand;
import server.logic.game.Player;
import common.Constants;
import common.Constants.RollReason;
import common.event.notifications.CombatHits;
import common.event.notifications.DieRoll;
import common.event.notifications.HexStatesChanged;
import common.game.HexState;
import common.game.TileProperties;

public class CombatPanel extends JPanel
{
	private static final long serialVersionUID = -8151724738245642539L;
	private static final String HITS_TO_APPLY_TEXT = "Hits to apply: ";
	
	private HexState hs;
	private JLabel hitsToApply;
	private int hitsToApplyNum;
	private final Player p;
	private final HashMap<TileProperties,JLabel> rolls;

	public CombatPanel(HexState hs, Player p)
	{
		this.hs = hs;

		this.p = p;
		hitsToApplyNum = 0;
		setLayout(new GridBagLayout());
		hitsToApply = new JLabel(HITS_TO_APPLY_TEXT + hitsToApplyNum);
		rolls = new HashMap<TileProperties,JLabel>();
		init();
	}
	
	private void init()
	{
		this.removeAll();
		rolls.clear();
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		
		updateHitsToApplyLabel();
		add(hitsToApply,constraints);
		constraints.gridy++;
		
		for(TileProperties thing : hs.getFightingThingsInHex())
		{
			if(p.ownsThingOnBoard(thing))
			{
				add(generateCreatureRollPanel(thing),constraints);
				constraints.gridy++;
			}
		}
		
		constraints.gridx++;
		constraints.gridy = 0;

		for(TileProperties thing : hs.getFightingThingsInHex())
		{
			if(!p.ownsThingOnBoard(thing))
			{
				if(thing.isCreature())
				{
					add(new JLabel(new ImageIcon(Constants.IMAGES.get(thing.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT))),constraints);
				}
				else
				{
					for(TileProperties b : Constants.BUILDING.values())
					{
						if(b.getName().equals(thing.getName()))
						{
							add(new JLabel(new ImageIcon(Constants.IMAGES.get(b.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT))),constraints);
						}
					}
				}
				constraints.gridy++;
			}
		}
	}
	
	private void updateHitsToApplyLabel()
	{
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run()
			{
				hitsToApply.setText(HITS_TO_APPLY_TEXT + hitsToApplyNum);
			}});
	}
	
	private JPanel generateCreatureRollPanel(final TileProperties thing)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		ImageIcon image = null;
		if(thing.isCreature())
		{
			image = new ImageIcon(Constants.IMAGES.get(thing.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
		}
		else
		{
			for(TileProperties b : Constants.BUILDING.values())
			{
				if(b.getName().equals(thing.getName()))
				{
					image = new ImageIcon(Constants.IMAGES.get(b.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT));
				}
			}
		}
		JButton creatureButton = new JButton(image);

		creatureButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				new ApplyHitsCommand(1, thing).postCommand(p.getID());
				hitsToApplyNum--;
				updateHitsToApplyLabel();
			}});
		
		panel.add(creatureButton,constraints);
		constraints.gridx++;
		
		JButton rollButton = new JButton("Roll");

		rollButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new RollDiceCommand(RollReason.ATTACK_WITH_CREATURE,thing).postCommand(p.getID());
			}});

		panel.add(rollButton,constraints);
		constraints.gridx++;
		
		JLabel rollValue = new JLabel("");
		panel.add(rollValue,constraints);
		rolls.put(thing, rollValue);
		
		return panel;
	}
	
	@Subscribe
	public void recieveRollNotification(DieRoll roll)
	{
		final DieRoll r = roll;
		if(roll.getID() == p.getID())
		{
			final JLabel valueLabel = rolls.get(roll.getDieRoll().getRollTarget());
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run()
				{
					int rollNum = r.getDieRoll().getRolls().get(0);
					valueLabel.setText(valueLabel.getText().equals("")? "" + rollNum : valueLabel.getText() + ", " + r.getDieRoll().getRolls().get(1));
				}});
		}
	}

	@Subscribe
	public void recieveHitsNotification(final CombatHits hitsNotification)
	{
		if(hitsNotification.getPlayerReceivingHitID() == p.getID())
		{
			hitsToApplyNum += hitsNotification.getNumberOfHits();
			updateHitsToApplyLabel();
		}

		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run()
			{
				for(JLabel rollLabel : rolls.values())
				{
					rollLabel.setText("");
				}
			}});
	}

	@Subscribe
	public void recieveHexChangedNotification(final HexStatesChanged hexChangeNotification)
	{
		hs = hexChangeNotification.getArray()[0];
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run()
			{
				init();
				invalidate();
			}});
	}
}

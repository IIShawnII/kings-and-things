package client.gui.components;

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

import server.event.internal.ApplyHitsCommand;
import server.event.internal.RollDiceCommand;
import common.Constants;
import common.Constants.RollReason;
import common.event.AbstractUpdateReceiver;
import common.event.network.CombatHits;
import common.event.network.DieRoll;
import common.event.network.HexStatesChanged;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;

public class CombatPanel extends JPanel
{
	private static final long serialVersionUID = -8151724738245642539L;
	private static final String HITS_TO_APPLY_TEXT = "Hits to apply: ";
	
	private HexState hs;
	private JLabel hitsToApply;
	private int hitsToApplyNum;
	private final Player p;
	private final HashMap<ITileProperties,JLabel> rolls;

	public CombatPanel(HexState hs, Player p)
	{
		this.hs = hs;

		this.p = p;
		hitsToApplyNum = 0;
		setLayout(new GridBagLayout());
		hitsToApply = new JLabel(HITS_TO_APPLY_TEXT + hitsToApplyNum);
		rolls = new HashMap<ITileProperties,JLabel>();
		init();
		new DieRollReceiver();
		new HexChangedReceiver();
		new HitsReceiver();
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
		
		for(ITileProperties thing : hs.getFightingThingsInHex())
		{
			if(p.ownsThingOnBoard(thing))
			{
				add(generateCreatureRollPanel(thing),constraints);
				constraints.gridy++;
			}
		}
		
		constraints.gridx++;
		constraints.gridy = 0;

		for(ITileProperties thing : hs.getFightingThingsInHex())
		{
			if(!p.ownsThingOnBoard(thing))
			{
				if(thing.isCreature())
				{
					add(new JLabel(new ImageIcon(Constants.IMAGES.get(thing.hashCode()).getScaledInstance(Constants.TILE_SIZE.width, Constants.TILE_SIZE.height, Image.SCALE_DEFAULT))),constraints);
				}
				else
				{
					for(ITileProperties b : Constants.BUILDING.values())
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
	
	private JPanel generateCreatureRollPanel(final ITileProperties thing)
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
			for(ITileProperties b : Constants.BUILDING.values())
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
				new ApplyHitsCommand(1, thing).postInternalEvent(p.getID());
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
				new RollDiceCommand(RollReason.ATTACK_WITH_CREATURE,thing).postInternalEvent(p.getID());
			}});

		panel.add(rollButton,constraints);
		constraints.gridx++;
		
		JLabel rollValue = new JLabel("");
		panel.add(rollValue,constraints);
		rolls.put(thing, rollValue);
		
		return panel;
	}
	
	private class DieRollReceiver extends AbstractUpdateReceiver<DieRoll>{

		protected DieRollReceiver() {
			super( INTERNAL, -1, CombatPanel.this);
		}

		@Override
		protected void handlePrivate( DieRoll update) {
			final DieRoll r = update;
			final JLabel valueLabel = rolls.get(update.getDieRoll().getRollTarget());
			SwingUtilities.invokeLater( new Runnable(){
				@Override
				public void run(){
					int rollNum = r.getDieRoll().getBaseRolls().get(0);
					valueLabel.setText(valueLabel.getText().equals("")? "" + rollNum : valueLabel.getText() + ", " + r.getDieRoll().getBaseRolls().get(1));
				}
			});
		}

		@Override
		protected boolean verifyPrivate( DieRoll update) {
			return update.isValidID( p.getID());
		}
	}
	
	private class HitsReceiver extends AbstractUpdateReceiver<CombatHits>{

		protected HitsReceiver() {
			super( INTERNAL, -1, CombatPanel.this);
		}

		@Override
		protected void handlePublic( CombatHits update) {
			if(update.getPlayerReceivingHitID() == p.getID()){
				hitsToApplyNum += update.getNumberOfHits();
				updateHitsToApplyLabel();
			}
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					for(JLabel rollLabel : rolls.values()){
						rollLabel.setText("");
					}
				}
			});
		}
	}
	
	private class HexChangedReceiver extends AbstractUpdateReceiver<HexStatesChanged>{

		protected HexChangedReceiver() {
			super( INTERNAL, -1, CombatPanel.this);
		}

		@Override
		protected void handlePublic( HexStatesChanged update) {
			hs = update.getArray()[0];
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					init();
					invalidate();
				}
			});
		}
	}
}

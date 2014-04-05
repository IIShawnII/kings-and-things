package client.gui.components.combat;

import static common.Constants.PUBLIC;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import common.event.AbstractUpdateReceiver;
import common.event.network.CombatHits;
import common.event.network.PlayerTargetChanged;
import common.game.ITileProperties;

public abstract class AbstractCombatArmyPanel extends JPanel
{
	private static final long serialVersionUID = 468496960335415903L;
	private static final String HITS_TAKEN_STRING = "Damage taken: ";
	
	private final String playerName;
	private String targetPlayerName;
	private final HashMap<ITileProperties,JButton> army;
	private final JLabel targetArmyLabel;
	private final JLabel armyLabel;
	private final JLabel hitsToApply;
	private final int playerID;
	
	public AbstractCombatArmyPanel(String playerName, int playerID, String targetPlayerName)
	{
		this.playerName = playerName;
		this.targetPlayerName = targetPlayerName;
		this.army = new HashMap<>();
		this.playerID = playerID;
		
		armyLabel = new JLabel(playerName + "'s rag tag army");
		armyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		armyLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		targetArmyLabel = new JLabel(generateTargetLabelString());
		targetArmyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		targetArmyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		
		hitsToApply = new JLabel(HITS_TAKEN_STRING + 0);
		hitsToApply.setHorizontalAlignment(SwingConstants.CENTER);
		hitsToApply.setHorizontalTextPosition(SwingConstants.CENTER);
		new HitsReceiver();
		new TargetChangedReceiver();
	}
	
	public void init(Collection<ITileProperties> things)
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
		
		add(generateArmyHeaderPanel(),constraints);
		
		constraints.gridy++;
		constraints.gridwidth = 1;
		constraints.weighty = 1;
		
		add(generateMainArmyPanel(things),constraints);
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
		remove(army.remove(thing));
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
	
	protected JLabel getArmyLabel()
	{
		return armyLabel;
	}

	protected JLabel getTargetArmyLabel()
	{
		return targetArmyLabel;
	}
	
	protected JLabel getHitsToApplyLabel()
	{
		return hitsToApply;
	}
	
	protected void addArmyMapping(JButton button, ITileProperties thing)
	{
		army.put(thing, button);
	}
	
	protected abstract JPanel generateArmyHeaderPanel();
	protected abstract JPanel generateMainArmyPanel(Collection<ITileProperties> things);
	
	private String generateTargetLabelString()
	{
		return "Targetting " + targetPlayerName + "'s rag tag army";
	}

	private class HitsReceiver extends AbstractUpdateReceiver<CombatHits>{

		protected HitsReceiver() {
			super( INTERNAL, PUBLIC, AbstractCombatArmyPanel.this);
		}

		@Override
		protected void handlePublic(final CombatHits update) {
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					AbstractCombatArmyPanel.this.setHitsToApply(update.getNumberOfHits());
				}
			});
		}
	}
	
	private class TargetChangedReceiver extends AbstractUpdateReceiver<PlayerTargetChanged>{

		protected TargetChangedReceiver() {
			super( INTERNAL, PUBLIC, AbstractCombatArmyPanel.this);
		}

		@Override
		protected void handlePublic(final PlayerTargetChanged update) {
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run(){
					AbstractCombatArmyPanel.this.setTargetPlayerName(update.getPlayersTarget().getName());
				}
			});
		}
		
		protected boolean verifyPublic( PlayerTargetChanged update){
			return update.isPublic() && update.getTargettingPlayer().getID() == playerID;
		}
	}
}

package client.gui.components.combat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;

import client.gui.components.TileButton;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.UpdatePackage;
import common.game.ITileProperties;

public class InactiveCombatArmyPanel extends AbstractCombatArmyPanel
{
	private static final long serialVersionUID = -4966411286325876812L;

	public InactiveCombatArmyPanel(String playerName, int playerID, String targetPlayerName)
	{
		super(playerName, playerID, targetPlayerName);
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
		
		constraints.gridy++;
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
			JButton button = new TileButton(tile);
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					new UpdatePackage(UpdateInstruction.CallBluff, UpdateKey.ThingArray, tile, "Inactive combat army panel for Player: " + getPlayerName()).postNetworkEvent(getPlayerID());
				}});
			mainArmyPanel.add(button,constraints);
			
			addArmyMapping(button,tile);
			constraints.gridy++;
		}
		
		return mainArmyPanel;
	}
}

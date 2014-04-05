package client.gui.components.combat;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import client.gui.components.HexContentsPanel;
import common.Constants;
import common.Constants.Direction;
import common.game.HexState;

public class RetreatPanel extends JPanel
{
	private static final long serialVersionUID = -601964560680389251L;

	public RetreatPanel(Collection<HexState> validOptions, HexState combatHex)
	{
		init(validOptions, combatHex);
	}
	
	private void init(Collection<HexState> validOptions, HexState combatHex)
	{
		JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		
		for(HexState hs : validOptions)
		{
			Direction retreatDirection = Constants.Direction.getFromAdjacentPoints(combatHex.getLocation(), hs.getLocation());
			JLabel directionLabel = new JLabel(new ImageIcon(retreatDirection.getImage()));
			directionLabel.setToolTipText("Direction");
			directionLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			contentsPanel.add(directionLabel,constraints);
			
			constraints.gridy++;
			constraints.weighty = 1;
			contentsPanel.add(new HexContentsPanel(hs),constraints);
			
			constraints.gridy++;
			constraints.weighty = 0;
			
			JButton pickMe = new JButton(new ImageIcon(Constants.PICK_ME_KITTEN));
			pickMe.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					// TODO send retreat command to server, close dialog
				}});
			contentsPanel.add(pickMe, constraints);
			constraints.gridy-=2;
			constraints.gridx++;
		}
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(contentsPanel);
		add(scrollPane);
	}
}

package client.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import client.gui.Board;
import client.gui.components.combat.AbstractCombatArmyPanel;
import client.gui.components.combat.ActiveCombatArmyPanel;
import client.gui.components.combat.InactiveCombatArmyPanel;
import client.gui.components.combat.RetreatPanel;

import com.google.common.eventbus.Subscribe;

import common.Constants;
import common.Constants.CombatPhase;
import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.CurrentPhase;
import common.event.network.HexStatesChanged;
import common.game.HexState;
import common.game.Player;

public class CombatPanel extends JPanel
{
	private static final long serialVersionUID = -8151724738245642539L;
	
	private HexState hs;
	private final ActiveCombatArmyPanel playerPanel;
	private final ArrayList<InactiveCombatArmyPanel> otherArmies;
	private final JScrollPane scrollPane;
	private final JLabel combatPhaseLabel;
	private final ArrayList<Player> allPlayersInCombat;
	private final ArrayList<Integer> playerOrderList;
	private final Player defendingPlayer;
	private final HashSet<HexState> adjacentPlayerOwnedHexes;
	private CombatPhase currentPhase;

	public CombatPanel(HexState hs, Collection<HexState> adjacentPlayerOwnedHexes, Player p, Collection<Player> otherPlayers, CombatPhase currentPhase, Player defendingPlayer, Collection<Integer> playerOrder)
	{
		this.hs = hs;
		this.currentPhase = currentPhase;
		this.adjacentPlayerOwnedHexes = new HashSet<>(adjacentPlayerOwnedHexes.size());
		for(HexState adjacentHs : adjacentPlayerOwnedHexes)
		{
			this.adjacentPlayerOwnedHexes.add(adjacentHs);
		}
		playerOrderList = new ArrayList<>(playerOrder.size());
		for(Integer i : playerOrder)
		{
			playerOrderList.add(i);
		}
		allPlayersInCombat = new ArrayList<>();
		allPlayersInCombat.add(p);
		this.defendingPlayer = defendingPlayer;
		
		setLayout(new GridBagLayout());
		playerPanel = new ActiveCombatArmyPanel(p.getName(), p.getID(), "No one");
		playerPanel.init(hs.getFightingThingsInHexOwnedByPlayer(p));
		
		otherArmies = new ArrayList<>(otherPlayers.size());
		for(Player otherPlayer : otherPlayers)
		{
			allPlayersInCombat.add(otherPlayer);
			InactiveCombatArmyPanel otherPanel = new InactiveCombatArmyPanel(otherPlayer.getName(),otherPlayer.getID(),"No one");
			otherPanel.init(hs.getFightingThingsInHexOwnedByPlayer(otherPlayer));
			otherArmies.add(otherPanel);
		}
		scrollPane = new JScrollPane();
		combatPhaseLabel = new JLabel();
	}
	
	public void init()
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
		constraints.weighty = 1;
		
		contentsPanel.add(playerPanel,constraints);
		constraints.gridx++;

		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		for(AbstractCombatArmyPanel panel : otherArmies)
		{
			contentsPanel.add(panel,constraints);
			constraints.gridx++;
		}
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weighty = 0;
		updateCombatPhaseLabel();
		combatPhaseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		combatPhaseLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		combatPhaseLabel.setFont(Board.STATUS_INDICATOR_FONT);
		contentsPanel.add(combatPhaseLabel,constraints);
		
		setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(contentsPanel);
		add(scrollPane,constraints);
		
		playerPanel.addTargetSelectActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String[] targetNames = new String[otherArmies.size()];
				for(int i=0; i<targetNames.length; i++)
				{
					targetNames[i] = otherArmies.get(i).getPlayerName();
				}
				String currentTarget = playerPanel.getTargetPlayerName();
				String targetName = (String) JOptionPane.showInputDialog(playerPanel, "Select the player you would like to target", "Change Target", JOptionPane.PLAIN_MESSAGE, null, targetNames, currentTarget.equals("No one")? targetNames[0] : currentTarget);
				int targetID = Constants.PUBLIC;
				for(InactiveCombatArmyPanel panel : otherArmies)
				{
					if(panel.getPlayerName().equals(targetName))
					{
						targetID = panel.getPlayerID();
					}
				}
				new UpdatePackage(UpdateInstruction.TargetPlayer, UpdateKey.Player, targetID, "Combat Panel for: " + playerPanel.getPlayerName()).postNetworkEvent(playerPanel.getPlayerID());
			}});
		playerPanel.addFightOnButtonListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				// TODO send skip command to server, update phase label upon getting results
			}});
		playerPanel.addRetreatButtonListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JDialog retreatDialog = new JDialog();
				retreatDialog.add(new RetreatPanel(adjacentPlayerOwnedHexes,hs));
				retreatDialog.setVisible(true);
			}});

		EventDispatch.registerOnInternalEvents(this);
	}
	
	private String getPlayerNameByAttackerNumber(int num)
	{
		int defenderIndex = playerOrderList.indexOf(defendingPlayer.getID());
		int offset = defenderIndex - num;
		int attackerID = playerOrderList.get(offset<0? playerOrderList.size() - offset: offset);
		for(Player p : allPlayersInCombat)
		{
			if(p.getID() == attackerID)
			{
				return p.getName();
			}
		}
		
		throw new IllegalStateException("Unable to find player with ID: " + attackerID);
	}
	
	private void setCombatPhase(CombatPhase phase)
	{
		currentPhase = phase;
		updateCombatPhaseLabel();
	}
	
	private void updateCombatPhaseLabel()
	{
		String phaseText = "";
		switch(currentPhase)
		{
			case APPLY_RANGED_HITS:
			case APPLY_MELEE_HITS:
			case APPLY_MAGIC_HITS:
			{
				phaseText = "Apply Damage";
				break;
			}
			case ATTACKER_ONE_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(1) + " Retreat";
				break;
			}
			case ATTACKER_THREE_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(3) + " Retreat";
				break;
			}
			case ATTACKER_TWO_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(2) + " Retreat";
				break;
			}
			case DEFENDER_RETREAT:
			{
				phaseText = getPlayerNameByAttackerNumber(0) + " Retreat";
				break;
			}
			case DETERMINE_DAMAGE:
			{
				phaseText = "Determine Damage To Hex";
				break;
			}
			case DETERMINE_DEFENDERS:
			{
				phaseText = "Determine Defenders";
				break;
			}
			case MAGIC_ATTACK:
			{
				phaseText = "Magic Attack";
				break;
			}
			case MELEE_ATTACK:
			{
				phaseText = "Melee Attack";
				break;
			}
			case NO_COMBAT:
			{
				phaseText = "No Combat";
				break;
			}
			case PLACE_THINGS:
			{
				phaseText = "Place Things In Hex";
				break;
			}
			case RANGED_ATTACK:
			{
				phaseText = "Ranged Attack";
				break;
			}
			case SELECT_TARGET_PLAYER:
			{
				phaseText = "Select Target Player";
				break;
			}
		}
		combatPhaseLabel.setText(phaseText);
	}
	
	private void combatHexChanged(HexState hex)
	{
		hs = hex;
		playerPanel.removeThingsNotInList(hex.getFightingThingsInHex());
		for(AbstractCombatArmyPanel p : otherArmies)
		{
			p.removeThingsNotInList(hex.getFightingThingsInHex());
		}
	}
	
	@Subscribe
	public void recieveHexChanged(final HexStatesChanged evt)
	{
		Runnable logic = new Runnable(){
			@Override
			public void run(){
				combatHexChanged(evt.getArray()[0]);
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
	public void recieveCombatPhaseChanged(final CurrentPhase<CombatPhase> evt)
	{
		Runnable logic = new Runnable(){
			@Override
			public void run(){
				setCombatPhase(evt.getPhase());
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

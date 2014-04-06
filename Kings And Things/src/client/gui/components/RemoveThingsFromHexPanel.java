package client.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.google.common.eventbus.Subscribe;

import common.Constants.UpdateInstruction;
import common.Constants.UpdateKey;
import common.event.EventDispatch;
import common.event.UpdatePackage;
import common.event.network.HexNeedsThingsRemoved;
import common.event.network.HexStatesChanged;
import common.game.HexState;
import common.game.ITileProperties;
import common.game.Player;

public class RemoveThingsFromHexPanel extends JPanel
{
	private static final long serialVersionUID = 5623429760419172554L;
	private final HashMap<ITileProperties,JButton> thingsInHex;
	private final Player p;
	private final JLabel thingsToRemoveLabel;
	private final JFrame parent;
	private final ITileProperties hex;
	
	public RemoveThingsFromHexPanel(Player player, JFrame parent, ITileProperties hex)
	{
		p = player;
		this.parent = parent;
		this.hex = hex;
		parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		thingsInHex = new HashMap<>();
		
		thingsToRemoveLabel = new JLabel();
		thingsToRemoveLabel.setHorizontalAlignment(SwingConstants.CENTER);
		thingsToRemoveLabel.setHorizontalTextPosition(SwingConstants.CENTER);
	}
	
	public void init(Collection<ITileProperties> thingsInHex, int removeCount)
	{
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		
		updateRemoveCountLabel(removeCount);
		add(thingsToRemoveLabel,constraints);

		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridwidth = 1;
		constraints.gridy++;
		
		for(final ITileProperties thing : thingsInHex)
		{
			JButton thingButton = new TileButton(thing);
			thingButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					UpdatePackage msg = new UpdatePackage(UpdateInstruction.RemoveThingsFromHex, UpdateKey.ThingArray, new ITileProperties[]{thing}, "RemoveThingsFromHex panel for player " + p);
					msg.putData(UpdateKey.Hex, hex);
					msg.postNetworkEvent(p.getID());
				}});
			this.thingsInHex.put(thing, thingButton);
			add(thingButton,constraints);
			
			constraints.gridx++;
		}
		EventDispatch.registerOnInternalEvents(this);
	}
	
	private void updateRemoveCountLabel(int removeCount)
	{
		thingsToRemoveLabel.setText("Select " + removeCount + " things to remove from this hex");
	}
	
	private void removeStuffNotInList(Collection<ITileProperties> things)
	{
		Iterator<Entry<ITileProperties,JButton>> it = thingsInHex.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<ITileProperties,JButton> entry = it.next();
			if(!things.contains(entry.getKey()))
			{
				remove(entry.getValue());
				it.remove();
			}
		}
		if(thingsInHex.size()==0)
		{
			close();
		}
	}
	
	private void close()
	{
		parent.dispose();
		EventDispatch.unregisterFromInternalEvents(this);
	}
	
	@Subscribe
	public void receiveHexRemovalCountChanged(final HexNeedsThingsRemoved evt)
	{
		if(!evt.isFirstNotificationForThisHex() && evt.getHex().getHex().equals(hex))
		{
			Runnable logic = new Runnable(){
				@Override
				public void run() {
					int newCount = evt.getNumToRemove();
					if(newCount == 0)
					{
						close();
					}
					else
					{
						updateRemoveCountLabel(evt.getNumToRemove());
					}
				}};
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
	
	@Subscribe
	public void recieveHexStateChanged(HexStatesChanged evt)
	{
		for(final HexState hs : evt.getArray())
		{
			if(hs.getHex().equals(hex))
			{
				Runnable logic = new Runnable(){
					@Override
					public void run() {
						removeStuffNotInList(hs.getThingsInHexOwnedByPlayer(p));
					}};
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
}

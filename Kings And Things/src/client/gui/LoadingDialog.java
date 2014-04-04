package client.gui;

import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.DefaultListModel;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;

import common.event.AbstractUpdateReceiver;
import common.event.UpdatePackage;
import common.game.PlayerInfo;
import common.Constants;
import common.Constants.Category;
import common.Constants.UpdateKey;
import common.Constants.UpdateInstruction;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog implements Runnable{

	private InputControl control;
	private String title;
	private Runnable task;
	private JPanel jpProgress;
	private DefaultListModel< PlayerInfo> listModel;
	private JTextField jtfIP, jtfPort, jtfName;
	private JButton jbConnect, jbReady;
	private JProgressBar jpbHex, jpbCup, jpbBuilding;
	private JProgressBar jpbGold, jpbSpecial, jpbState;
	private boolean isConnected = false, progress, forceClose = true;
	private UpdateReceiver receiver;
	
	public LoadingDialog( Runnable task, String title, boolean modal, boolean progress, GraphicsConfiguration gc) {
		super( (Frame)null, title, modal, gc);
		this.task = task;
		this.title = title;
		this.progress = progress;
		control = new InputControl();
		receiver = new UpdateReceiver();
	}

	@Override
	public void run() {
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE);
		addWindowListener( new InputControl());
		setContentPane( createGUI());
		pack();
		Dimension size = new Dimension( getWidth(), getHeight());
		setMinimumSize( size);
		setLocationRelativeTo( null);
		Thread thread = new Thread( task, title);
		thread.setDaemon( true);
		thread.start();
		setVisible( true);
	}
	
	private JPanel createGUI(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);

		JLabel label = new JLabel( "User Name:");
		constraints.gridy = 0;
		jpMain.add( label, constraints);
		
		jbReady = new JButton( "UnReady");
		jbReady.setEnabled( false);
		jbReady.addActionListener( control);
		constraints.gridy = 1;
		jpMain.add( jbReady, constraints);
		
		jbConnect = new JButton( "Connect");
		jbConnect.setEnabled( false);
		jbConnect.addActionListener( control);
		constraints.gridy = 6;
		jpMain.add( jbConnect, constraints);
		
		label = new JLabel( "IP:");
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 1;
		jpMain.add( label, constraints);
		
		label = new JLabel( "Port:");
		constraints.gridx = 3;
		jpMain.add( label, constraints);
		
		jtfIP = new JTextField( Constants.SERVER_IP, Constants.IP_COLUMN_COUNT);
		constraints.gridx = 2;
		constraints.weightx = .6;
		jpMain.add( jtfIP, constraints);
		
		jtfPort = new JTextField( Constants.SERVER_PORT+"", Constants.PORT_COLUMN_COUNT);
		constraints.gridx = 4;
		constraints.weightx = .4;
		jpMain.add( jtfPort, constraints);
		
		jtfName = new JTextField("-demo Xaphan Leviathan Abaddon Lilith");
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 1;
		jpMain.add( jtfName, constraints);
		
		if( progress){
			jpProgress = createLoadingPanel();
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.gridx = 0;
			constraints.gridy = 7;
			constraints.weightx = 1;
			jpMain.add( jpProgress, constraints);
		}

		listModel = new DefaultListModel<>();
		JList<PlayerInfo> jlPlayers = new JList<>( listModel);
		JScrollPane jsp = new JScrollPane( jlPlayers);
		jsp.setPreferredSize( Constants.CONSOLE_SIZE);
		jsp.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		constraints.gridheight = 5;
		constraints.weighty = 1;
		constraints.gridx = 1;
		constraints.gridy = 1;
		jpMain.add( jsp, constraints);
		
		return jpMain;
	}
	
	private JPanel createLoadingPanel(){
		JPanel jpMain = new JPanel( new GridBagLayout());
		jpMain.setPreferredSize( Constants.PROGRESS_SIZE);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 5, 5, 5, 5);
		
		JLabel label = new JLabel("Building");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 0;
		constraints.gridx = 0;
		jpMain.add( label, constraints);

		
		jpbBuilding = new JProgressBar( JProgressBar.HORIZONTAL, 0, 6);
		jpbBuilding.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbBuilding, constraints);
		
		label = new JLabel("Cup");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 1;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbCup = new JProgressBar( JProgressBar.HORIZONTAL, 0, 158);
		jpbCup.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbCup, constraints);
		
		label = new JLabel("Gold");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 2;
		constraints.gridx = 0;
		jpMain.add( label, constraints);

		jpbGold = new JProgressBar( JProgressBar.HORIZONTAL, 0, 6);
		jpbGold.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbGold, constraints);
		
		label = new JLabel("Hex");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 3;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbHex = new JProgressBar( JProgressBar.HORIZONTAL, 0, 8);
		jpbHex.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbHex, constraints);
		
		label = new JLabel("Special");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 4;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbSpecial = new JProgressBar( JProgressBar.HORIZONTAL, 0, 22);
		jpbSpecial.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbSpecial, constraints);
		
		label = new JLabel("State");
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.gridy = 5;
		constraints.gridx = 0;
		jpMain.add( label, constraints);
		
		jpbState = new JProgressBar( JProgressBar.HORIZONTAL, 0, 4);
		jpbState.setStringPainted( true);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1;
		constraints.gridx = 1;
		jpMain.add( jpbState, constraints);
		
		return jpMain;
	}
	
	public void removeProgress(){
		remove( jpProgress);
		jpProgress = null;
		Dimension size = getSize();
		size.height -= Constants.PROGRESS_SIZE.height;
		setMinimumSize( size);
		setPreferredSize( size);
		setSize( size);
		revalidate();
	}
	
	public void close(){
		setVisible( false);
		task = null;
		dispose();
		receiver.unregisterFromEventBus();
	}
	
	public boolean isForceClosed(){
		return forceClose;
	}
	
	private class InputControl extends WindowAdapter implements ActionListener {
		
		private UpdatePackage update = new UpdatePackage("Dialog.Control", this);
		
		@Override
		public void actionPerformed( ActionEvent e) {
			Object source = e.getSource();
			update.clear();
			if( source==jbConnect){
				if( !isConnected){
					update.addInstruction( UpdateInstruction.Connect);
					update.putData( UpdateKey.Name, jtfName.getText().trim());
					update.putData( UpdateKey.IP, jtfIP.getText().trim());
					update.putData( UpdateKey.Port, Integer.parseInt( jtfPort.getText().trim()));
				}else{
					update.addInstruction( UpdateInstruction.Disconnect);
				}
				update.postInternalEvent(Constants.LOGIC);
			}else if( isConnected && source==jbReady){
				update.addInstruction( UpdateInstruction.State);
				update.postInternalEvent(Constants.LOGIC);
			}
		}
		
		@Override
		public void windowClosing(WindowEvent e){
			close();
		}
	}
	
	private class UpdateReceiver extends AbstractUpdateReceiver<UpdatePackage>{

		protected UpdateReceiver() {
			super( INTERNAL, Constants.PUBLIC, LoadingDialog.this);
		}

		@Override
		protected void handlePrivate( UpdatePackage update) {
			updateProgress( update);
		}

		@Override
		protected void handlePublic( UpdatePackage update) {
			updateDialog( update);
		}
		
		@Override
		protected boolean verifyPrivate( UpdatePackage update){
			return update.isValidID( Constants.PROGRESS);
		}
	}

	private void updateDialog( UpdatePackage update){
		switch(update.peekFirstInstruction()){
			case Connect:
				isConnected = true;
				jbConnect.setText( "Disconnect");
				jtfName.setEnabled( false);
				jtfIP.setEnabled( false);
				jtfPort.setEnabled( false);
				break;
			case Disconnect:
				isConnected = false;
				jbConnect.setText( "Connect");
				if( update.getData( UpdateKey.Message)!=null){
					JOptionPane.showMessageDialog( this, update.getData( UpdateKey.Message), "Connection", JOptionPane.ERROR_MESSAGE);
				}
				listModel.removeAllElements();
				break;
			case Start:
				forceClose = false;
				listModel = null;
				close();
				break;
			case State:
				jbReady.setText( (String)update.getData( UpdateKey.Message));
				break;
			case UpdatePlayers:
				if( listModel!=null){
					listModel.removeAllElements();
					for( PlayerInfo player: (PlayerInfo[])update.getData( UpdateKey.Players)){
						listModel.addElement( player);
					}
				}
				break;
			default:
				return;
		}
		jbReady.setEnabled( isConnected);
	}
	
	private void updateProgress( UpdatePackage load) {
		if( !progress){
			return;
		}
		if(load.peekFirstInstruction()==UpdateInstruction.Category){
			Category category = (Category)load.getData( UpdateKey.Category);
			switch( category){
				case Building:
					jpbBuilding.setValue( jpbBuilding.getValue()+1);
					break;
				case Cup:
					jpbCup.setValue( jpbCup.getValue()+1);
					break;
				case Gold:
					jpbGold.setValue( jpbGold.getValue()+1);
					break;
				case Hex:
					jpbHex.setValue( jpbHex.getValue()+1);
					break;
				case Special:
					jpbSpecial.setValue( jpbSpecial.getValue()+1);
					break;
				case State:
					jpbState.setValue( jpbState.getValue()+1);
					break;
				case END:
					remove( jpProgress);
					Dimension size = getSize();
					size.height -= Constants.PROGRESS_SIZE.height+10;
					setMinimumSize( size);
					setSize( size);
					revalidate();
					jbConnect.setEnabled( true);
				default:
					break;
			}
		}
	}
}

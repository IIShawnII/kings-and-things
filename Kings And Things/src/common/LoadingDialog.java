package common;

import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.BorderFactory;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GraphicsConfiguration;

import static mycode.engine.collision.Constants.LOADING_WIDTH;
import static mycode.engine.collision.Constants.LOADING_HEIGHT;
import static mycode.engine.collision.Constants.CONTROLLER_COUNT;
import static mycode.engine.collision.Constants.MAX_MOVING_OBJECTS;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog implements Runnable{

	private String title;
	private Runnable task;
	private JProgressBar jpbObjects, jpbControllers, jpbStart;
	
	public LoadingDialog( Runnable task, String title, boolean modal, GraphicsConfiguration gc) {
		super( (Frame)null, title, modal, gc);
		this.task = task;
		this.title = title;
	}

	@Override
	public void run() {
		setUndecorated( true);
		setPreferredSize( new Dimension( LOADING_WIDTH, LOADING_HEIGHT));
		setContentPane( createGUI());
		pack();
		setLocationRelativeTo( null);
		Thread thread = new Thread( task, title);
		thread.setDaemon( true);
		thread.start();
		setVisible( true);
	}
	
	private JPanel createGUI(){
		JPanel jpMain = new JPanel( new GridLayout( 3, 1, 10, 10));
		jpMain.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder(Color.BLACK), title));
		jpbObjects = new JProgressBar( JProgressBar.HORIZONTAL, 0, MAX_MOVING_OBJECTS);
		jpbObjects.setStringPainted( true);
		jpbObjects.setString( "Objects Created: 0/"+MAX_MOVING_OBJECTS);
		jpbControllers = new JProgressBar( JProgressBar.HORIZONTAL, 0, CONTROLLER_COUNT);
		jpbControllers.setStringPainted( true);
		jpbControllers.setString( "Controllers Created: 0/"+CONTROLLER_COUNT);
		jpbStart = new JProgressBar( JProgressBar.HORIZONTAL, 0, CONTROLLER_COUNT);
		jpbStart.setStringPainted( true);
		jpbStart.setString( "Controllers Started: 0/"+CONTROLLER_COUNT);
		jpMain.add( jpbObjects);
		jpMain.add( jpbControllers);
		jpMain.add( jpbStart);
		return jpMain;
	}
	
	public void updateObjects( int objects){
		jpbObjects.setString( "Objects Creating: "+objects+"/"+MAX_MOVING_OBJECTS);
		jpbObjects.setValue( objects);
	}
	
	public void updateControllers( int controllers, boolean starting){
		if( !starting){
			jpbControllers.setString( "Controllers Creating: "+controllers+"/"+CONTROLLER_COUNT);
			jpbControllers.setValue( controllers);
		}else{
			jpbStart.setString( "Controllers Starting: "+controllers+"/"+CONTROLLER_COUNT);
			jpbStart.setValue( controllers);
		}
	}
	
	public void close(){
		setVisible( false);
		task = null;
		dispose();
	}
}

package client.gui.die;

import javax.swing.Timer;
import javax.swing.JPanel;

import common.Constants;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static common.Constants.IMAGE_DICE;
import static common.Constants.DICE_SIZE;
import static common.Constants.MAX_ROLLS;

@SuppressWarnings("serial")
public class Die extends JPanel implements ActionListener{
	
	//clockwise dice, possible faces for 1 and 6, 2 and 5 and finally 3 and 4
	private static final int[][] FACES ={{2,3,4,5},{1,3,4,6},{1,2,5,6}}; 

	private Timer timer;
	private Parent parent;
	private int roll, target, faceValue;

	public Die( Parent parent) {
		super( true);
		this.parent = parent;
	}
	
	public Die init(){
		faceValue = 1;
		setFace();
		setOpaque( false);
		timer = new Timer( 125, this);
		setPreferredSize( new Dimension( DICE_SIZE, DICE_SIZE));
		return this;
	}
	
	public void setResult( int face){
		target = face;
	}
	
	public void roll(){
		if( !timer.isRunning()){
			roll = 0;
			timer.start();
		}
	}
	
	private void setFace(){
		int index = 0;
		switch( faceValue){
			case 1: case 6:
				index = 0; break;
			case 2: case 5:
				index = 1; break;
			case 3: case 4:
				index = 2; break;
			default:
				throw new IllegalStateException( "ERROR - fave Value can only be 1-6, received: " + faceValue);
		}
		faceValue = FACES[index][Constants.random( 3)];
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setFace();
		roll++;
		if( roll>=MAX_ROLLS && faceValue==target){
			timer.stop();
			parent.doneRolling();
		}
		repaint();
	}

	@Override
	public void paintComponent( Graphics g) {
		super.paintComponent( g);
		g.drawImage( IMAGE_DICE[faceValue], 0, 0, getWidth(), getHeight(), null);
	}
}
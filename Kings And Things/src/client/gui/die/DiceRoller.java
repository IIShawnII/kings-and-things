package client.gui.die;

import java.util.Random;
import java.util.ArrayList;

import javax.swing.JPanel;

import client.gui.util.SizeChangeAnimation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import static common.Constants.DICE_SIZE;
import static common.Constants.IMAGE_DICE;
import static common.Constants.IMAGE_GREEN;

@SuppressWarnings("serial")
public class DiceRoller extends JPanel implements Parent{

	private ArrayList<Die> dice;
	private SizeChangeAnimation change;
	private Random rand = new Random();
	private int dieCount, rollingCount;

	public DiceRoller() {
		super( true);
	}

	public DiceRoller init() {
		setOpaque( false);
		setBackground( Color.GREEN.darker().darker());
		change = new SizeChangeAnimation( IMAGE_DICE[0], DICE_SIZE, this);
		setPreferredSize( new Dimension( DICE_SIZE,DICE_SIZE));
		addMouseListener( new MouseListener());
		dice = new ArrayList< Die>();
		setLayout( new FlowLayout());
		setResult( 2, 6, 6);
		return this;
	}
	
	public void setResult( int count, int...results){
		if( count>dieCount){
			for( int i=0; i<count-dieCount;i++){
				Die die= new Die( this).init();
				dice.add( die);
				add(die);
			}
		} else if( count<dieCount){
			for( int i=0; i<dieCount-count;i++){
				remove( dice.remove(0));
			}
		}
		dieCount = count;
		count = 0;
		for( Die die: dice){
			die.setResult( results[ count++]);
		}
		revalidate();
		repaint();
	}
	
	public void roll(){
		for( Die die: dice){
			die.roll();
			rollingCount++;
		}
	}

	public boolean isRolling(){
		return rollingCount>=1;
	}
	
	@Override
	public void doneRolling() {
		rollingCount--;
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent( g);
		g.drawImage( IMAGE_GREEN, 0, 0, getWidth(), getHeight(), null);
	}
	
	@Override
	public void paint( Graphics g){
		if( change==null || !change.paint( g)){
			super.paint( g);
		}
	}
	
	private class MouseListener extends MouseAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e){
			if( e.getButton()==MouseEvent.BUTTON1){
				if( change.isExpanded() && !isRolling()){
					setResult( 4, rand.nextInt( 6)+1, rand.nextInt( 6)+1, rand.nextInt( 6)+1, rand.nextInt( 6)+1);
					roll();
				}else{
					change.expandTo( DICE_SIZE*5, (int)(DICE_SIZE*1.2));
				}
			}else if( e.getButton()==MouseEvent.BUTTON3){
				//TODO add debug
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e){
			change.shrinkToOriginal();
		}
	}
}
package client.gui.die;

import java.util.List;
import java.util.ArrayList;

import javax.swing.JPanel;

import client.gui.util.SizeChangeAnimation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.FlowLayout;

import static common.Constants.DICE_SIZE;
import static common.Constants.IMAGE_DICE;
import static common.Constants.IMAGE_GREEN;

@SuppressWarnings("serial")
public class DiceRoller extends JPanel implements Parent{

	private ArrayList<Die> dice;
	private SizeChangeAnimation change;
	private int dieCount, rollingCount;

	public DiceRoller() {
		super( true);
	}

	public DiceRoller init() {
		setOpaque( false);
		setBackground( Color.GREEN.darker().darker());
		change = new SizeChangeAnimation( IMAGE_DICE[0], DICE_SIZE, this);
		setPreferredSize( new Dimension( DICE_SIZE,DICE_SIZE));
		dice = new ArrayList< Die>();
		setLayout( new FlowLayout());
		setResult( 2, null);
		return this;
	}
	
	public void setResult( int count, List<Integer> results){
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
			die.setResult( results==null? 6: results.get( count));
			count++;
		}
		revalidate();
		repaint();
	}
	
	public void expand(){
		change.expandTo( DICE_SIZE*5, (int)(DICE_SIZE*1.2));
	}
	
	public void shrink(){
		change.shrinkToOriginal();
	}
	
	public void roll(){
		for( Die die: dice){
			die.roll();
			rollingCount++;
		}
	}

	public boolean canRoll(){
		return change.isExpanded() && !isRolling();
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
}
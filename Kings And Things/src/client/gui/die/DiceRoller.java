package client.gui.die;

import javax.swing.JPanel;

import com.google.common.primitives.Ints;

import client.gui.util.SizeChangeAnimation;

import java.util.List;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.FlowLayout;

import static common.Constants.DICE_SIZE;
import static common.Constants.IMAGE_DICE;
import static common.Constants.IMAGE_GREEN;

@SuppressWarnings("serial")
public class DiceRoller extends JPanel implements Parent{

	private Die[] dice;
	private SizeChangeAnimation change;
	private int dieCount, rollingCount, results[];

	public DiceRoller() {
		super( true);
	}

	public DiceRoller init() {
		setOpaque( false);
		setBackground( Color.GREEN.darker().darker());
		change = new SizeChangeAnimation( IMAGE_DICE[0], DICE_SIZE, this);
		setPreferredSize( new Dimension( DICE_SIZE,DICE_SIZE));
		dice = new Die[]{ new Die( this).init(), new Die( this).init()};
		results = new int[]{ 6, 6};
		setLayout( new FlowLayout());
		setDiceCount( 2);
		return this;
	}
	
	public List<Integer> getResults(){
		return Ints.asList( results);
	}
	
	public void setResult( List< Integer> results){
		if( results.size()==dieCount){
			for( int i=0; i<dieCount; i++){
				this.results[i] = results.get( i);
				dice[i].setResult( this.results[i]);
			}
		}else if(results.size()>dieCount){
			throw new IllegalStateException( "ERROR - more results than available Dice");
		}
	}
	
	public void expand(){
		change.expandTo( DICE_SIZE*3, (int)(DICE_SIZE*1.2));
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

	public void setDiceCount( int count) {
		if( count>dieCount){
			for( int i=dieCount; i<dice.length;i++){
				add(dice[i]);
			}
		} else if( count<dieCount){
			for( int i=count; i<dice.length;i++){
				remove( dice[i]);
			}
		}
		dieCount = count;
		revalidate();
		repaint();
	}
}
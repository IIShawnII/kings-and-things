package client.gui.die;

import java.util.Random;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

@SuppressWarnings("serial")
public class DiceRoller extends JPanel implements Parent{

	private ArrayList<Die> dice;
	private Random rand = new Random();
	private int dieCount, rollingCount;

	public DiceRoller() {
		super( true);
	}

	private DiceRoller init() {
		setBackground( Color.RED);
		addMouseListener( new MouseListener());
		dice = new ArrayList< Die>();
		setLayout( new FlowLayout());
		setResult( 3, rand.nextInt( 6)+1, rand.nextInt( 6)+1, rand.nextInt( 6)+1);
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
	
	private class MouseListener extends MouseAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e){
			if( isRolling()){
				return;
			}
			if( e.getButton()==MouseEvent.BUTTON1){
				setResult( 4, rand.nextInt( 6)+1, rand.nextInt( 6)+1, rand.nextInt( 6)+1, rand.nextInt( 6)+1);
			}else if( e.getButton()==MouseEvent.BUTTON3){
				setResult( 2, rand.nextInt( 6)+1, rand.nextInt( 6)+1);
			}
			roll();
		}
	}
	
	public static void main( String[] args){
		try {
			UIManager.setLookAndFeel( UIManager. getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println( e);
		}
		final DiceRoller roller = new DiceRoller().init();
		final JFrame frame = new JFrame( "Die Roller");
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane( roller);
		frame.pack();
		frame.setMinimumSize( new Dimension( frame.getWidth()*3, frame.getHeight()*3));
		frame.setLocationRelativeTo( null);
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				frame.setVisible( true);
				roller.roll();
			}
		});
	}
}
package client.gui;

import static common.Constants.BOARD_SIZE;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import common.event.EventDispatch;

public class MultiBoardManager {

	private JPanel container;
	private Board[] boards;
	private int current = -1;
	private GridBagConstraints constraints;
	
	public MultiBoardManager( JPanel container, GridBagConstraints constraints){
		this.container = container;
		this.constraints = constraints;
	}
	
	public void creatBoards( final int count){
		boards = new Board[count];
		for( int i=0; i<count; i++){
			boards[ i] = new Board( null, true);
			EventDispatch.registerForCommandEvents( boards[ i]);
			boards[ i].setPreferredSize( BOARD_SIZE);
			boards[ i].setSize( BOARD_SIZE);
			boards[ i].init( count);
		}
	}
	
	public void showBoard( int index){
		if( current>=0){
			boards[index].setActive( false);
			container.remove( boards[current]);
		}
		current = index;
		boards[index].setActive( true);
		container.add( boards[index], constraints);
		container.revalidate();
	}
}

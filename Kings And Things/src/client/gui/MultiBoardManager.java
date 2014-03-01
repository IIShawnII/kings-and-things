package client.gui;

import static common.Constants.BOARD_SIZE;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import common.event.EventDispatch;

public class MultiBoardManager {

	private JPanel container;
	private Board[] boards;
	private Board current;
	private final int MAX_SIZE;
	private GridBagConstraints constraints;
	
	public MultiBoardManager( JPanel container, GridBagConstraints constraints, int boardCount){
		MAX_SIZE = boardCount;
		boards = new Board[ MAX_SIZE];
		this.container = container;
		this.constraints = constraints;
		current = null;
	}
	
	public void creatBoards( ){
		for( int i=0; i<MAX_SIZE; i++){
			boards[ i] = new Board( null, true);
			EventDispatch.registerForCommandEvents( boards[ i]);
			boards[ i].setPreferredSize( BOARD_SIZE);
			boards[ i].setSize( BOARD_SIZE);
			boards[ i].init( MAX_SIZE);
			container.add( boards[ i], constraints);
		}
	}
	
	public void showBoard( int index){
		if( current!=null){
			container.remove( current);
		}
		container.add( boards[index]);
		container.revalidate();
	}
}

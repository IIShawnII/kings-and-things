package client.gui;

import static common.Constants.BOARD_SIZE;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import common.game.PlayerInfo;

public class MultiBoardManager {

	private Board[] boards;
	private JPanel container;
	private GridBagConstraints constraints;
	private int current = -1;
	private boolean created = false;
	
	public MultiBoardManager( JPanel container, GridBagConstraints constraints){
		setProperties( container, constraints);
	}
	
	public MultiBoardManager(){}
	
	public void creatBoards( final int count, final PlayerInfo[] players){
		if( count<=1 || count>=5){
			throw new IllegalArgumentException( "Error - Count must be between 2 and 4");
		}
		if(created){
			return;
		}
		boards = new Board[count];
		for( int i=0; i<count; i++){
			boards[ i] = new Board( null);
			boards[ i].setPreferredSize( BOARD_SIZE);
			boards[ i].setSize( BOARD_SIZE);
			boards[ i].init( count);
			boards[ i].setCurrentPlayer( players[i]);
		}
		created = true;
	}
	
	public void setProperties( JPanel container, GridBagConstraints constraints){
		if( constraints==null || container==null){
			throw new IllegalArgumentException( "Error - Neither of container or constraints can be null");
		}
		this.container = container;
		this.constraints = constraints;
	}
	
	public void showBoard( int index){
		if(current==index){
			return;
		}
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

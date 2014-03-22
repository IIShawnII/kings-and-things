package client.gui.components;

import static common.Constants.BOARD_SIZE;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import client.gui.Board;
import common.game.PlayerInfo;

public class MultiBoardManager {

	private Board[] boards;
	private JPanel container;
	private Board currentBoard = null;
	private GridBagConstraints constraints;
	private int currentID = -1;
	private boolean created = false;
	
	public MultiBoardManager( JPanel container, GridBagConstraints constraints){
		setProperties( container, constraints);
	}
	
	public void creatBoards(final PlayerInfo[] players){
		if(created){
			return;
		}
		boards = new Board[players.length];
		for( int i=0; i<boards.length; i++){
			boards[ i] = new Board( null);
			boards[ i].setPreferredSize( BOARD_SIZE);
			boards[ i].setSize( BOARD_SIZE);
			boards[ i].init( boards.length);
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
	
	public void show( final int ID){
		if( ID==-1){
			//only for initial initiation
			setNewBoard( ID, boards[0]);
		}
		if(currentID==ID){
			return;
		}
		for( Board board:boards){
			if( board.matchPlayer( ID)){
				if(	currentBoard!=null){
					container.remove( currentBoard);
					currentBoard.setActive( false);
				}
				setNewBoard( ID, board);
			}
		}
	}
	
	private void setNewBoard( final int ID, Board board){
		currentID = ID;
		currentBoard = board;
		currentBoard.setActive( true);
		container.add( currentBoard, constraints);
		container.revalidate();
		container.repaint();
	}
}

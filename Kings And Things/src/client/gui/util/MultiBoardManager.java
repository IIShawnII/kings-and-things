package client.gui.util;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import client.gui.Board;
import common.Constants;
import common.game.PlayerInfo;

public class MultiBoardManager {

	private Board[] boards;
	private JPanel container;
	private Board currentBoard = null;
	private GridBagConstraints constraints;
	private int currentID = -1;
	private boolean created = false;
	private final boolean demo;
	
	public MultiBoardManager( JPanel container, GridBagConstraints constraints, boolean demo){
		this.demo = demo;
		setProperties( container, constraints);
	}
	
	public void creatBoards(final PlayerInfo[] players){
		if(created){
			return;
		}
		boards = new Board[players.length];
		for( int i=0; i<boards.length; i++){
			boards[ i] = new Board( demo, players[i]);
			boards[ i].setPreferredSize( Constants.BOARD_SIZE);
			boards[ i].setSize( Constants.BOARD_SIZE);
			boards[ i].init( boards.length);
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
	
	public void show( final PlayerInfo player){
		show( player.getID());
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
				break;
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

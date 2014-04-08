package client.gui.util.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import common.Constants;
import client.gui.tiles.Tile;

/**
 * animation task to work with timer, used for animating 
 * tile movement from starting position to its destination
 */
public class MoveAnimation implements ActionListener{
	
	private CanvasParent parent;
	private Tile tile;
	private Point end;
	private Timer timer;
	private double slope, intercept;
	private int xTemp=-1, yTemp;
	private Tile[] list;
	private int index = -1;
	private Dimension size;
	
	public MoveAnimation( CanvasParent parent){
		this.parent = parent;
	}
	
	private void setTile( Tile tile){
		this.tile = tile;
		this.end = tile.getDestination();
		xTemp = tile.getX();
		yTemp = tile.getY();
		slope = (end.y-yTemp)/(double)(end.x-xTemp);
		intercept = yTemp-slope*xTemp;
		size = tile.getSize();
	}
	
	public void start( Tile...tiles){
		list = tiles;
		index = 0;
		xTemp = -1;
		parent.phaseStarted();
		timer = new Timer( Constants.ANIMATION_DELAY, this);
        timer.setInitialDelay( 0);
        timer.start();
	}

	@Override
	public void actionPerformed( ActionEvent e) {
		//animation is done
		if( !parent.isActive() || xTemp==-1){
			//list is done
			if( !parent.isActive() || index>=list.length){
				timer.stop();
				parent.phaseDone();
				return;
			}
			//get next index in list
			if( list[index]!=null && list[index].canAnimate()){
				setTile((tile = list[index]));
				index++;
			}else{
				index++;
				return;
			}
		}
		yTemp = (int)(slope*xTemp+intercept);
		tile.setLocation( xTemp, yTemp);
		xTemp+=Constants.MOVE_DISTANCE;
		//hex has passed its final location
		if( xTemp>=end.x-size.width/2){
			xTemp=-1;
			tile.setLocation( end.x-size.width/2, end.y-size.height/2);
			tile.setLockArea( parent.getLock( tile));
			parent.placeTileOnHex( tile);
		}
		parent.repaintCanvas();
	}
}
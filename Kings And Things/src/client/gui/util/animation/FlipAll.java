package client.gui.util.animation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import common.Constants;
import client.gui.tiles.Hex;
import client.gui.tiles.Tile;

public class FlipAll implements ActionListener{

	private CanvasParent parent;
	private Timer timer;
	private Component[] list;
	private int index = 0;
	
	public FlipAll( Component[] components , CanvasParent parent){
		list = components;
		this.parent = parent;
		index = 0;
	}
	
	public void start(){
		parent.phaseStarted();
		timer = new Timer( Constants.ANIMATION_DELAY, this);
        timer.setInitialDelay( 0);
        timer.start();
	}

	@Override
	public void actionPerformed( ActionEvent e) {
		if(index>=list.length){
			timer.stop();
			parent.phaseDone();
		}else{
			if( list[index] instanceof Hex){
				((Tile) list[index]).flip();
				parent.repaintCanvas();
			}
			index++;
		}
	}
}
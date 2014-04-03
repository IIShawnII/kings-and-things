package client.gui.util.undo;

import common.game.ITileProperties;

import client.gui.util.LockManager.Lock;

public class BasicOperation implements OperationInterface{

	private Lock lock;
	private ITileProperties tile;
	
	public BasicOperation( ITileProperties tile, Lock lock){
		this.tile = tile;
		this.lock = lock;
	}
	
	public Lock getLock(){
		return lock;
	}
	
	public ITileProperties getTile(){
		return tile;
	}
	
	@Override
	public void clear(){
		lock = null;
		tile = null;
	}
}
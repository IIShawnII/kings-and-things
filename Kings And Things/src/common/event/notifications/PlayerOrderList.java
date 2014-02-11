package common.event.notifications;

import java.util.List;

import common.event.AbstractNetwrokEvent;

public class PlayerOrderList extends AbstractNetwrokEvent {
	
	private static final long serialVersionUID = -7178623266087384627L;
	
	private List<Integer> order;
	
	public PlayerOrderList( List<Integer> list){
		order = list;
	}
	
	public int[] getList(){
		int[] array = new int[order.size()];
		for( int i=0; i<array.length;i++){
			array[i] = order.get( i);
		}
		return array;
	}
	
	@Override
	public String toString(){
		return "Network/PlayerOrderList: " + order;
	}
}

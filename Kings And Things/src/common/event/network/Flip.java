package common.event.network;

import common.event.AbstractNetwrokEvent;

public class Flip extends AbstractNetwrokEvent {

	private static final long serialVersionUID = 5236081833514074273L;
	
	private boolean flipAll = true;
	
	public boolean flipAll(){
		return flipAll;
	}
	
	@Override
	public String toString(){
		return "Network/Flip: Flip All";
	}
}

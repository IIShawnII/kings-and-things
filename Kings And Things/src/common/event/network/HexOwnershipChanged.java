package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;

public class HexOwnershipChanged extends AbstractNetwrokEvent {
	private static final long serialVersionUID = -8427885692049391183L;
	
	private final HexState hex;
	
	public HexOwnershipChanged(HexState hex){
		this.hex = hex;
	}
	
	public HexState getChangedHex(){
		return hex;
	}
	
	@Override
	public String toString(){
		return "Network/HexOwner: hex: " + hex;
	}
}

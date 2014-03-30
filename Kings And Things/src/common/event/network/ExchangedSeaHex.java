package common.event.network;

import common.event.AbstractNetwrokEvent;
import common.game.HexState;

public class ExchangedSeaHex extends AbstractNetwrokEvent {

	private static final long serialVersionUID = -320725755166258477L;
	
	private HexState state;
	
	public ExchangedSeaHex( HexState state){
		this.state = state;
	}

	public HexState getSate(){
		return state;
	}
	
	@Override
	public String toString(){
		return "Network/ExchangeSeaHex: Hex: " + state;
	}
}

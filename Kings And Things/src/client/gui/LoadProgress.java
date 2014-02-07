package client.gui;

import common.Constants.Category;
import common.event.AbstractEvent;

public class LoadProgress extends AbstractEvent {

	private Category category = null;
	
	public LoadProgress( Category category){
		this.category = category;
	}
	
	public Category getCategory(){
		return category;
	}
}

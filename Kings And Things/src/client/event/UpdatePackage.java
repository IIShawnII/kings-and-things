package client.event;

import java.util.HashMap;
import java.util.LinkedList;

import common.event.AbstractEvent;
import common.Constants.UpdateKey;
import common.Constants.UpdateInstruction;

/**
 * a complete package to hold all information needed to
 * be sent as local events or network events.
 * use putData and addInstruction to populate the package
 */
public class UpdatePackage extends AbstractEvent {

	private LinkedList< UpdateInstruction> instructions;
	private HashMap< UpdateKey, Object> data;
	private String source;
	
	public UpdatePackage( String source){
		this.source = source;
		data = new HashMap<>();
		instructions = new LinkedList<>();
	}
	
	public UpdatePackage( UpdateInstruction instruction, String source){
		this( source);
		addInstruction( instruction);
	}
	
	public void putData( UpdateKey key, Object value){
		data.put( key, value);
	}
	
	public void addInstruction( UpdateInstruction instruction){
		instructions.add( instruction);
	}
	
	public UpdateInstruction getFirstInstruction(){
		return instructions.removeFirst();
	}

	public Object getData( Object key){
		return data.get( key);
	}

	public boolean hasInstructions(){
		return instructions.size()>=1;
	}

	public boolean hasData(){
		return data.size()>=1;
	}
	
	public void clearDate(){
		data.clear();
	}
	
	public void clearInstruction(){
		instructions.clear();
	}
	
	public void clear(){
		clearDate();
		clearInstruction();
	}

	public boolean isModified() {
		return hasData()||hasInstructions();
	}
	
	@Override
	public String toString(){
		return "Network/UpdatePackage:\t\nSource: " + source + ",\n\tDate: " + data.keySet() + ",\n\tInstructions count: " + instructions;
	}
}

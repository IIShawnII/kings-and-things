package common.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import common.Constants.UpdateKey;
import common.Constants.UpdateInstruction;

/**
 * a complete package to hold all information needed to
 * be sent as local events or network events.
 * use putData and addInstruction to populate the package
 */
public class UpdatePackage extends AbstractEvent implements Serializable {

	private static final long serialVersionUID = -1888776611771542522L;
	
	private LinkedList< UpdateInstruction> instructions;
	private HashMap< UpdateKey, Object> data;
	private String source;
	
	public UpdatePackage( String source, final Object OWNER){
		super(OWNER);
		this.source = source;
		data = new HashMap<UpdateKey, Object>();
		instructions = new LinkedList<UpdateInstruction>();
	}
	
	public UpdatePackage( UpdateInstruction instruction, String source, final Object OWNER){
		this( source, OWNER);
		addInstruction( instruction);
	}
	
	/**
	 * used for network only
	 */
	public UpdatePackage( UpdateInstruction instruction, String source){
		this( source, null);
		addInstruction( instruction);
	}
	
	/**
	 * used for network only
	 */
	public UpdatePackage( UpdateInstruction instruction, UpdateKey key, Object data, String source){
		this( source, null);
		addInstruction( instruction);
		putData( key, data);
	}
	
	public void putData( UpdateKey key, Object value){
		data.put( key, value);
	}
	
	public void addInstruction( UpdateInstruction instruction){
		instructions.add( instruction);
	}
	
	public UpdateInstruction[] getInstructions(){
		return instructions.toArray( new UpdateInstruction[0]);
	}
	
	public UpdateInstruction peekFirstInstruction(){
		return instructions.peekFirst();
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
	
	public void clearData(){
		data.clear();
	}
	
	public void clearInstruction(){
		instructions.clear();
	}
	
	public void clear(){
		clearData();
		clearInstruction();
	}

	public boolean isModified() {
		return hasData()||hasInstructions();
	}
	
	public void setSource( String source){
		this.source = source;
	}
	
	@Override
	public String toString(){
		return "Network/UpdatePackage:\n\tSource: " + source + "\n\tInstructions: " + instructions + "\n\tData: " + data.keySet();
	}
}

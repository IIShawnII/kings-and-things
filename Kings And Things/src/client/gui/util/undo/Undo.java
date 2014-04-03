package client.gui.util.undo;

public class Undo<T extends OperationInterface>{
		
	private T start, end;
	
	protected Undo(){
		start = null;
		end = null;
	}
	
	protected void add( T operation){
		if( start==null){
			start = operation;
		}else if( end==null){
			end = operation;
		}else{
			throw new IllegalStateException("start and end Operations are assigned already");
		}
	}
	
	public T getStart(){
		return start;
	}
	
	public T getEnd(){
		return end;
	}
	
	protected boolean isComplete(){
		return start!=null && end!=null;
	}
	
	protected void clear(){
		if( start!=null){
			start.clear();
		}
		start = null;
		if( end!=null){
			end.clear();
		}
		end = null;
	}
}
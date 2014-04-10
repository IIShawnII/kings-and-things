package common;

import java.util.ArrayList;
import java.util.List;

public class TotalDiceTree {
    private Node root = null;
    private int maxDepth, min, max;

    public void generate( int total, int depth, int min, int max){
    	root = new Node( total);
    	this.maxDepth = depth;
    	this.min = min;
    	this.max = max;
    	root.createChildren();
    }
    
    public List<Integer> getRandomCombination(){
    	if( root==null || !root.hasChildren()){
    		return null;
    	}
    	List<Integer> list = new ArrayList< Integer>( maxDepth);
    	Node node = root;
    	for( int i=0;i<maxDepth;i++){
    		node = node.getRandomChild();
    		list.add( node.getRoll());
    	}
    	return list;
    }
    
    @Override
    public String toString(){
    	return root.toString();
    }
    
    protected class Node {
        private int total = 0, roll = 0, depth;
        private List<Node> children = null;
        
        public Node( int total){
        	this( 0, total, 0);
        }
        
        private Node( int roll, int total, int depth){
        	this.roll = roll;
        	this.total = total;
        	this.depth = depth;
        	children = new ArrayList<Node>( max);
        }
        
        public boolean hasChildren(){
        	return children.size()>0;
        }
        
        public Node getRandomChild(){
        	return children.get( Constants.random( 0, children.size()-1));
        }
        
        public int getRoll(){
        	return roll;
        }

		public boolean createChildren() {
			if( total==0 && depth==maxDepth){
				return true;
			}
			Node node = null;
			for( int i=min; i<=max; i++){
				node = new Node( i, total-i, depth+1);
				if( node.validate()){
					if (node.createChildren()){
						children.add(node);
					}
				}
			}
			return hasChildren();
		}
	    
	    private boolean validate(){
	    	if(total<max && depth<(maxDepth-1)){
	    		return false;
	    	}
	    	if(total>max && depth==(maxDepth-1)){
	    		return false;
	    	}
	    	if( total!=0 && depth==maxDepth){
	    		return false;
	    	}
	    	return true;
	    }
	    
	    private String print( String prefix){
	    	String str = "";
	    	if( depth==maxDepth){
	    		return prefix + toString();
	    	}
	    	for( Node node : children){
	    		if(depth==0){
	    			str += prefix + node.print( prefix + "--");
	    		}else{
	    			str += prefix + roll + "\n" + node.print( prefix + "--") + "\n";
	    		}
	    	}
	    	return str;
	    }
	    
	    @Override
	    public String toString() {
	    	if( depth==0){
		        return total + "\n" + print("");
	        }else{
	        	return Integer.toString( roll);
	        }
	    }
    }
}

package Metrics;

import java.util.List;
import java.util.Vector;


public class Sequence extends Vector<String>{

	/**
	 * 
	 */
	public Sequence(){
		super();
	}
	
	public Sequence(List l){
		super(l);
	}
	
	private static final long serialVersionUID = -1443846443442875387L;
	
	public Sequence subsequence(int start, int end){
		
		return (new Sequence(this.subList(start, end)));
	}
	
}

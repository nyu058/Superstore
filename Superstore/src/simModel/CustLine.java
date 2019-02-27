package simModel;

import java.util.ArrayList;

/*
 * Queue set, each member represents a queue at a counter
 */
public class CustLine {
	// Implement the queue using an ArrayList object
	protected ArrayList<Customer> custLine = new ArrayList<Customer>(); 
	
	// getters/setters and standard procedures
	protected int getN() { 
		return(custLine.size()); 
	}
	
	
	protected void spInsertQue(Customer cust) { 
		custLine.add(cust);
	}
	
	protected Customer spRemoveQue() { 
		Customer cust = null;
		if(custLine.size() != 0) cust = custLine.remove(0); // delete and return the first element
		return(cust);
	}
}

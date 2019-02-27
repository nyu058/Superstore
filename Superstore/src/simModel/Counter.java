package simModel;

/*
 * Resource Consumer Set, each set member represents a counter
 */
public class Counter {
	
	protected boolean uOpen; // indicate if the counter is open or not
	protected Customer customer; // costumer currently engaged in checkout at that counter
	protected boolean baggerPresent;
	
	protected enum counterStates {
		SCANNING_READY, // this state indicates that the counter isn't busy
		SCANNING,
		PAYMENT_READY,
		PAYMENT,
		BAGGING_READY,
		BAGGING;
	}; //enumeration
	
	protected counterStates state; 

}

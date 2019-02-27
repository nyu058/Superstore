package simModel;

/*
 * Class, each instance represents a customer
 */
public class Customer {
	
	protected int nItems; // number of items the customer wants to buy
	protected enum payMethods{
		CASH,
		CREDIT_CARD,
		CHECK_WITH_CARD,
		CHECK_NO_CARD;
	};
	
	protected payMethods payMethod; // Payment method the customer wants to use
	protected double startWait; // time at which the customer started to wait
	
}

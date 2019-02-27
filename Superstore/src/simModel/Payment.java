package simModel;

import simulationModelling.ConditionalActivity;

/*
 * Implements the conditional activity Payment, used when the payment method is NOT
 * check without card
 */
public class Payment extends ConditionalActivity{
	
	static SMSuperstore model;  // for referencing the model
	int id; // counter considered here
	
	/*
	 * This UDP return the id (if it exists) of a counter ready to begin payment when no supervisor is required.
	 * The preconditions are defined in the detailed level CM
	 */
	protected static int nextPayment() {
		for(int i=Constants.C1; i<=Constants.C20; i++) {
			if(model.rcCounters[i].state == Counter.counterStates.PAYMENT_READY 
					&& model.rcCounters[i].customer.payMethod != Customer.payMethods.CHECK_NO_CARD) {
				return i;
			}
		}
		return Constants.NONE;
	}
	
	/*
	 * @param the model
	 * @return true if the precondition, tested by the UDP.nextPayment, is true, false otherwise
	 */
	protected static boolean precondition(){
		boolean returnValue = false;
	    if( (nextPayment	() != Constants.NONE)) returnValue = true;
		return(returnValue);
	}

	/*
	 * Starting event SCS
	 */
	public void startingEvent() {
		this.id = nextPayment();
		model.rcCounters[id].state=Counter.counterStates.PAYMENT;
	}

	/*
	 * Duration determined by the RVP.uPayTime and dependent on the payment method
	 */
	protected double duration() {
		return (model.rvp.uPayTime(model.rcCounters[id].customer.payMethod));
	}

	/*
	 * Terminating event SCS
	 */
	protected void terminatingEvent() {
		if(model.rcCounters[id].baggerPresent) {
			model.rgBaggers.nAvail +=1;
			model.rcCounters[id].baggerPresent=false;
			model.rcCounters[id].customer=null;
			model.rcCounters[id].state = Counter.counterStates.SCANNING_READY;
		} else {
			model.rcCounters[id].state = Counter.counterStates.BAGGING_READY;
		}
	}

}

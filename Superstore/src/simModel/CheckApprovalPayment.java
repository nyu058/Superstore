package simModel;

import simulationModelling.ConditionalActivity;

/*
 * Implements the conditional activity used for payment with check but no card, which
 * requires the Supervisor's approval 
 */

public class CheckApprovalPayment extends ConditionalActivity{
	static SMSuperstore model;  // for referencing the model
	int id; // Counter considered here

	
	/*
	 * @param the model
	 * @return true if the precondition is true, false otherwise
	 */
	protected static boolean precondition(){
		boolean returnValue = false;
	    if( (!model.rSupervisor.isBusy && model.qApproveLine.getN() > 0)) returnValue = true;
		return(returnValue);
	}

	/*
	 * Starting event SCS
	 */
	public void startingEvent() {
		this.id = model.qApproveLine.spRemoveQue();
		model.rcCounters[id].state=Counter.counterStates.PAYMENT;
		model.rSupervisor.isBusy=true;
	}

	/*
	 * Duration determined by the RVP.uApprovalTime
	 */
	protected double duration() {
		return (model.rvp.uApprovalTime());
	}

	/*
	 * Terminating event SCS
	 */
	protected void terminatingEvent() {
		model.rSupervisor.isBusy = false;
		if(model.rcCounters[id].baggerPresent) {
			model.rgBaggers.nAvail +=1;
			model.rcCounters[id].baggerPresent=false;
			model.rcCounters[id].state = Counter.counterStates.SCANNING_READY;
			model.rcCounters[id].customer=null;
		} else {
			model.rcCounters[id].state = Counter.counterStates.BAGGING_READY;
		}
	}

}

package simModel;

import simulationModelling.ScheduledAction;

/*
 * Implements the scheduled action Initialise, executed once at t=0
 */
class Initialise extends ScheduledAction {
	static SMSuperstore model;

	double [] ts = { 0.0, -1.0 }; // -1.0 ends scheduling
	int tsix = 0;  // set index to first entry.
	
	protected double timeSequence() 
	{
		return ts[tsix++];  // only invoked at t=0
	}

	protected void actionEvent() {
		// System Initialisation
        for(int id=Constants.C1; id<=Constants.C20; id++) {
        	//creation of customers' queues and the counters
        	model.qCustLines[id] = new CustLine();
        	model.rcCounters[id] = new Counter();
        	model.rcCounters[id].state=Counter.counterStates.SCANNING_READY; //all counters not busy
        	model.rcCounters[id].baggerPresent = false;
        }
        model.dvp.openCloseCounters(); // open the right number of counter for the first period
        model.rgBaggers.nAvail=model.baggerSchedule[0]; // set the number of baggers to the one indicated in the schedule
        model.rSupervisor.isBusy = false;
        
	}
	

}

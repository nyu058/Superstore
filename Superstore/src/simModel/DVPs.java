package simModel;

class DVPs {
	
	static SMSuperstore model;  // for accessing the clock
	/*
	 * Set the first nCash counters to open and the others to closed, where nCash is the 
	 * number of cashiers in the schedule for each period
	 */
	protected void openCloseCounters() {
		int period = (int) (model.getClock() + 10)/30 ;  // the +10 here ensure the correct period is computed, with regards to the ApplySchedule time sequence.
		int nCash = model.cashierSchedule[period];
		for (int id=Constants.C1; id<=Constants.C20; id++) {
			if (id<nCash) {
				model.rcCounters[id].uOpen = true;
			} else {
				model.rcCounters[id].uOpen = false;
			}
		}
	}
}

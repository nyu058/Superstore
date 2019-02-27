package simModel;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import simulationModelling.ConditionalActivity;

/*
 * Implements the conditional activity Bagging
 */
public class Bagging extends ConditionalActivity{
	
	static SMSuperstore model;  // for referencing the model
	int id; // counter considered in the activity
	
	/*
	 * random variate procedure for the bagging time
	 */
	
	private static final double MEAN_BAG_TIME = 1.25/60;
	private static final double SD_BAG_TIME = 0.75/60;
	private static Normal BaggingTime;
	
	protected static double uBaggingTime(int nItems) {
		double bagTime=0;
		for(int i = 0; i<nItems; i++)
		{
			bagTime+=Math.max(0.0, BaggingTime.nextDouble());
		}
		return bagTime;
	}
	
	/*
	 * This UDP return the id (if it exists) of a counter ready to begin bagging if not already done by a bagger.
	 * The preconditions are defined in the detailed level CM
	 */
	protected static int nextBagging() {
		for(int i=Constants.C1; i<=Constants.C20; i++) {
			if(model.rcCounters[i].state == Counter.counterStates.BAGGING_READY 
					&& model.rcCounters[i].baggerPresent == false) {
				return i;
			}
		}
		return Constants.NONE;
	}
	
	
	// initializes rvps and udps associated with the class
	static void init() {
		BaggingTime = new Normal(MEAN_BAG_TIME, SD_BAG_TIME, new MersenneTwister(model.sd.seedBagTime));
	}
	/*
	 * @param the model
	 * @return true if the precondition, tested by the UDP.nextBagging, is true, false otherwise
	 */
	protected static boolean precondition(){
		boolean returnValue = false;
	    if( (nextBagging() != Constants.NONE)) returnValue = true;
		return(returnValue);
	}

	/*
	 * Starting event SCS
	 */
	public void startingEvent() {
		this.id = nextBagging();
		model.rcCounters[id].state=Counter.counterStates.BAGGING;
	}

	/*
	 * Duration determined by the RVP.uBaggingTime and dependent on the number of items
	 */
	protected double duration() {
		return (uBaggingTime(model.rcCounters[id].customer.nItems));
	}

	/*
	 * Terminating event SCS
	 */
	protected void terminatingEvent() {
		model.rcCounters[id].state = Counter.counterStates.SCANNING_READY;
		model.rcCounters[id].customer=null;

	}

}

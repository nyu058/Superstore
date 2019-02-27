package simModel;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import simulationModelling.ConditionalActivity;

/*
 * This class represents the conditional activity scanning.
 */

public class Scanning extends ConditionalActivity {
	
	static SMSuperstore model;  // for referencing the model
	int id; // this activity is parameterized since it's the same for all the counters
	
	/*
	 * Random variate procedure for scanning time
	 */
	private static final double MEAN_SCAN_TIME = 3/60;
	private static final double SD_SCAN_TIME = 0.75/60;
	private static final double MEAN_PRICE_CHECK = 2.2;
	private static final double SD_PRICE_CHECK = 1;
	private static final double PROB_PRICE_CHECK=0.13;
	private static Uniform priceCheck;
	private static Normal ScanTime;
	private static Normal PriceCheckTime;
	
	protected static double uScanTime(int nItems) {
		double scanTime =  0.0;
		for (int i=0; i<nItems; i++) {
			scanTime += Math.max(0.0, ScanTime.nextDouble()); //we had issues where the normal distribution returned negative times for scanning.
		}
		if(priceCheck.nextDouble()<=PROB_PRICE_CHECK) 
			return scanTime+PriceCheckTime.nextDouble();
		else
			return scanTime;
	}
	
	/*
	 * This UDP return the id (if it exists) of a counter ready to begin scanning.
	 * The preconditions are defined in the detailed level CM
	 */
	protected static int nextScanning() {
		for(int i=Constants.C1; i<=Constants.C20; i++) {
			if(model.rcCounters[i].state == Counter.counterStates.SCANNING_READY && model.qCustLines[i].getN() >0) {
				return i;
			}
		}
		return Constants.NONE;
	}
	
	//initializes the rvps and udp associated with this class
	static void init() {
		ScanTime = new Normal(MEAN_SCAN_TIME, SD_SCAN_TIME, new MersenneTwister(model.sd.seedScanTime));
		PriceCheckTime = new Normal(MEAN_PRICE_CHECK, SD_PRICE_CHECK, new MersenneTwister(model.sd.seedPriceCheckTime));
		priceCheck = new Uniform(0,1, new MersenneTwister(model.sd.seedPriceCheck));
	}
	
	/*
	 * @param the model
	 * @return true if the precondition of this activity, defined by UDP.nextScanning(), is true, false otherwise.
	 */
	protected static boolean precondition(){
		boolean returnValue = false;
	    if( (nextScanning() != Constants.NONE)) returnValue = true;
		return(returnValue);
	}

	/*
	 * Starting event SCS
	 */
	public void startingEvent() {
		Output output = model.output;
		this.id = nextScanning();
		model.rcCounters[id].customer = model.qCustLines[id].spRemoveQue();
		model.rcCounters[id].state = Counter.counterStates.SCANNING;
		if(model.getClock()-model.rcCounters[id].customer.startWait > 15) {
			output.numLongWait[(int) model.getClock()/30] +=1;
		}
		output.numCustomers[(int) model.getClock()/30]+=1;
		output.propLongWait[(int) model.getClock()/30] = (double) (output.numLongWait[(int) model.getClock()/30])/ (double) (output.numCustomers[(int) model.getClock()/30]);
		if (model.rgBaggers.nAvail > 0) {
			model.rgBaggers.nAvail -=1;
			model.rcCounters[id].baggerPresent = true;
		}
	}

	/*
	 * Duration of the activity defined by the number of items to scan, and the need or not of a price check.
	 */
	protected double duration() {
		return (uScanTime(model.rcCounters[id].customer.nItems));
	}

	/*
	 * Terminating event SCS
	 */
	protected void terminatingEvent() {
		if(model.rcCounters[id].customer.payMethod == Customer.payMethods.CHECK_NO_CARD) {
			model.qApproveLine.spInsertQue(id);
		}
		model.rcCounters[id].state = Counter.counterStates.PAYMENT_READY;
	}

}

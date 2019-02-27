package simModel;
import cern.jet.random.Exponential;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import simulationModelling.*;

/*
 * Implements the scheduled action Arrivals,  the entity stream representing the arriving customers
 */
public class Arrivals extends ScheduledAction {

	static SMSuperstore model;
	
	
	/* Random Variate Procedure for Arrivals */
	private static Exponential [] interArrCust = new Exponential[16] ;  // Exponential distribution for interarrival times
	private static final double MEAN_INTER_ARR[]= {38.0/60.0, 36.0/60.0, 30.0/60.0, 24.0/60.0, 
	                                   22.0/60.0, 24.0/60.0, 22.0/60.0, 33.0/60.0, 
	                                   34.0/60.0, 38.0/60.0 ,29.0/60.0, 24.0/60.0, 
	                                   23.0/60.0, 38.0/60.0, 51.0/60.0, 1.0};
	
	protected static double duCArr()  // for getting next value of duInput
	{
	    double nxtInterArr;

        nxtInterArr = interArrCust[(int)(model.getClock())/30].nextDouble();
        //System.out.println("Next customer in: "+nxtInterArr);
	    // Note that interarrival time is added to current
	    // clock value to get the next arrival time.
	    return(nxtInterArr+model.getClock());
	}
	
	
	/* Random Variate procedures for number of items */
	private static final double MEAN_ITEM_A=27;
	private static final double MEAN_ITEM_B=108;
	private static final double SD_ITEM_A=8.33;
	private static final double SD_ITEM_B=19;
	private static final double PROB_CAT_A=0.233;
	private static Normal NItemA;
	private static Normal NItemB;
	private static Uniform ItemCat;
	
	protected int nItems() 
	{
		if(ItemCat.nextDouble()<=PROB_CAT_A)
			return (int)NItemA.nextDouble();
		else
			return (int)NItemB.nextDouble();
	
	}
	
	/*
	 * Random variate procedure for payment method
	 */
	private static final double PROB_CASH_INF20=0.45;
	private static final double PROB_CASH_SUP20=0.20;
	private static final double PROB_CREDIT_CARD_INF20=0.25;
	private static final double PROB_CREDIT_CARD_SUP20=0.35;
	private static final double PROB_CHECK_WITH_CARD=0.73;
	private static Uniform payMethodCat;
	private static Uniform checkWithCard;
	
	
	protected static Customer.payMethods payMethod(int nItems) {
		double rand = payMethodCat.nextDouble();
		if(nItems <= 20) {
			if(rand<=PROB_CASH_INF20) {
				return Customer.payMethods.CASH;
			} else if (rand <= PROB_CASH_INF20+PROB_CREDIT_CARD_INF20) {
				return Customer.payMethods.CREDIT_CARD;
			} else {
				double rand2 = checkWithCard.nextDouble();
				if (rand2 <= PROB_CHECK_WITH_CARD) {
					return Customer.payMethods.CHECK_WITH_CARD;
				} else {
					return Customer.payMethods.CHECK_NO_CARD;
				}
			}
		} else {
			if(rand<=PROB_CASH_SUP20) {
				return Customer.payMethods.CASH;
			} else if (rand <= PROB_CASH_SUP20+PROB_CREDIT_CARD_SUP20) {
				return Customer.payMethods.CREDIT_CARD;
			} else {
				double rand2 = checkWithCard.nextDouble();
				if (rand2 <= PROB_CHECK_WITH_CARD) {
					return Customer.payMethods.CHECK_WITH_CARD;
				} else {
					return Customer.payMethods.CHECK_NO_CARD;
				}
			}
		}
	}
	
	
	/*
	 * This UDP returns the id of the queue in which to put the newly arriving customer.
	 * It's essentially a min function with special conditions. 
	 */
	protected static int ChooseQueue() {
		int mini = model.qCustLines[0].getN()+(model.rcCounters[0].customer==null?0:1);  //the ternary operator accounts for the customer in the counter. 
		int id = Constants.C1;
		for (int i=Constants.C1; i<=Constants.C20; i++) {
			if(model.rcCounters[i].uOpen && model.qCustLines[i].getN() + (model.rcCounters[i].customer==null?0:1) < mini){
				id = i;
				mini = model.qCustLines[i].getN() + (model.rcCounters[i].customer==null?0:1);
			}
		}
		return id;
	}
	
	//initializes the rvp and udp associated with this class
	static void init() {
		for (int i=0; i<16;i++) {
			interArrCust[i] = new Exponential(1.0/MEAN_INTER_ARR[i],
				                       	new MersenneTwister(model.sd.seedArrival[i]));
		}
		
		NItemA = new Normal(MEAN_ITEM_A,SD_ITEM_A,new MersenneTwister(model.sd.seedItemA));
		NItemB = new Normal(MEAN_ITEM_B,SD_ITEM_B,new MersenneTwister(model.sd.seedItemB));
		ItemCat = new Uniform(0,1,new MersenneTwister(model.sd.seedItemCat));
		
		payMethodCat = new Uniform(0,1, new MersenneTwister(model.sd.seedPayMethodCat));
		checkWithCard = new Uniform(0,1, new MersenneTwister(model.sd.seedCheckWithCard));
	}
	
	/*
	 * @return the next time value at which a customer will arrive
	 */
	@Override
	protected double timeSequence() {
		return duCArr();
	}

	/*
	 * Create a new customer arriving in the store, and generate all needed attributes
	 */
	@Override
	protected void actionEvent() {
		int id = ChooseQueue(); 
		Customer cust = new Customer();
		cust.nItems = nItems();
		cust.payMethod = payMethod(cust.nItems);
		cust.startWait = model.getClock();
		model.qCustLines[id].spInsertQue(cust); // add the customer to the chosen queue
		//System.out.println("one client arrived queue num: " + id + " queue length: "+ model.qCustLines[id].n );
	}

}

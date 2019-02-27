package simModel;


import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;

/*
 * This class defines the Random Variate Procedures (RVPs) used in the model, that are shared accros multiple activities. 
 * The RVPs will be implemented as methods returning the next number in the pseudo-random sequence.
 */

class RVPs 
{
	static SMSuperstore model; // for accessing the clock
    // Data Models - i.e. random veriate generators for distributions
	// are created using Colt classes, define 
	// reference variables here and create the objects in the
	// constructor with seeds


	// Constructor
	protected RVPs() 
	{ 
		// Set up distribution functions
		
		Cash = new Normal(MEAN_CASH, SD_CASH, new MersenneTwister(model.sd.seedCash));
		CreditCard = new Normal(MEAN_CREDIT_CARD, SD_CREDIT_CARD, new MersenneTwister(model.sd.seedCreditCard));
		Check = new Normal(MEAN_CHECK, SD_CHECK, new MersenneTwister(model.sd.seedCheck));
		
		ApprovalTime = new Normal(MEAN_APP_TIME, SD_APP_TIME, new MersenneTwister(model.sd.seedAppTime));
		
	}
	
	
	/*
	 * Random variate procedure for payment item
	 */
	
	private final double MEAN_CASH = 0.95;
	private final double SD_CASH = 0.17;
	private final double MEAN_CREDIT_CARD = 1.24;
	private final double SD_CREDIT_CARD = 0.21;
	private final double MEAN_CHECK = 1.45;
	private final double SD_CHECK = 0.35;
	private Normal Cash;
	private Normal CreditCard;
	private Normal Check;
	
	
	protected double uPayTime(Customer.payMethods payMethod) {
		double payTime = Constants.NONE;
		switch (payMethod){
			case CASH:
				payTime = Cash.nextDouble();
				break;
			case CREDIT_CARD:
				payTime = CreditCard.nextDouble();
				break;
			case CHECK_WITH_CARD:
				payTime = Check.nextDouble();
				break;
			case CHECK_NO_CARD:
				payTime = Check.nextDouble();
				break;			
		}
		return payTime;
	}
	
	/*
	 * random variate procedure for the approval time.
	 */
	
	private final double MEAN_APP_TIME = 0.95;
	private final double SD_APP_TIME = 0.15;
	private Normal ApprovalTime;
	
	protected double uApprovalTime() {
		return ApprovalTime.nextDouble()+uPayTime(Customer.payMethods.CHECK_NO_CARD);
	}
	
	
	
}

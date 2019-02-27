package simModel;

class Output {
	static SMSuperstore model;

    // SSOVs
	// Number of customers served in each period
	protected int[] numCustomers = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	// Number of customers who waited more than 15 minutes for each period
	protected int[] numLongWait = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	// Proportion of customers who waited 15 minutes or more, i.e. numLongWait/numCustomers
	protected double[] propLongWait = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

}

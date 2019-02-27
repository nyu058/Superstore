package simModel;

import simulationModelling.AOSimulationModel;
import simulationModelling.Behaviour;

// The Simulation model Class
public class SMSuperstore extends AOSimulationModel
{
	// Constants available from Constants class
	
	/* Parameters */
    protected int[] cashierSchedule;
    protected int[] baggerSchedule;
        

	/*-------------Entity Data Structures-------------------*/
    
	protected CustLine [] qCustLines = new CustLine[20];
	protected Counter [] rcCounters = new Counter[20];
	protected Baggers rgBaggers = new Baggers();
	protected Supervisor rSupervisor = new Supervisor();
	protected ApproveLine qApproveLine = new ApproveLine();
		
	// References to RVP and DVP objects
	protected RVPs rvp;  // Reference to rvp object - object created in constructor
	protected DVPs dvp = new DVPs();  // Reference to dvp object

	Seeds sd;
	// Output object
	protected Output output = new Output();
	
	// Output values - define the public methods that return values
	// required for experimentation.
	// SSOVs
	public int[] getNumCostumers () {return output.numCustomers;};  
	public int[] getNumLongWait () {return output.numLongWait;};
	public double[] getPropLongWait () {return output.propLongWait;};

	public void InitialiseClass() {
		//Actions / ACtivities
		Initialise.model=this;
		ApplySchedule.model=this;
		Arrivals.model=this;
		Bagging.model=this;
		CheckApprovalPayment.model=this;
		Payment.model=this;
		Scanning.model=this;
		//RVP, DVP, Output
		DVPs.model=this;
		RVPs.model=this;
		Output.model=this;
		rvp = new RVPs();  //rvp needs to be instanciated here, so that model.sd is initialized
		ApplySchedule.init();
        Arrivals.init();
        Scanning.init();
        Bagging.init();
		
		
	}
	// Constructor
	public SMSuperstore(double t0time, double tftime,int [] cashierSchedule, int[] baggerSchedule , Seeds sd, boolean logFlag)
	{
		
		// Turn trancing on if traceFlag is true
		this.logFlag = logFlag;
		this.sd = sd;
		//Initialise static attributes model
		InitialiseClass();
		// Initialise parameters here
		this.cashierSchedule = cashierSchedule;
		this.baggerSchedule = baggerSchedule;
		// Create RVP object with given seed
		
		// rgCounter and qCustLine objects created in Initialise Action
		
		// Initialise the simulation model
		initAOSimulModel(t0time,tftime);  
		stopTime = tftime;

		// Schedule the first arrivals and employee scheduling
		Initialise init = new Initialise();
		scheduleAction(init);  // Should always be first one scheduled.
		// Schedule other scheduled actions and activities here
		Arrivals arr = new Arrivals();
		scheduleAction(arr);
		ApplySchedule applySched = new ApplySchedule();
		scheduleAction(applySched);
		
	}

	/************  Implementation of Data Modules***********/	
	/*
	 * Testing preconditions
	 */
	protected void testPreconditions(Behaviour behObj)
	{
		reschedule (behObj);
		while (scanPreconditions() == true) /* repeat */;
	}
	
	// Single scan of all preconditions
	// Returns true if at least one precondition was true.
	private boolean scanPreconditions()
	{
		boolean statusChanged = false;

		// Conditional Activities
		if (Scanning.precondition() == true)
		{
			Scanning act = new Scanning(); // Generate instance
			act.startingEvent();
			scheduleActivity(act);
			statusChanged = true;
		}
		
		if (Payment.precondition() == true)
		{
			Payment act = new Payment(); // Generate instance																// instance
			act.startingEvent();
			scheduleActivity(act);
			statusChanged = true;
		}
		
		if (CheckApprovalPayment.precondition() == true)
		{
			CheckApprovalPayment act = new CheckApprovalPayment(); // Generate instance																// instance
			act.startingEvent();
			scheduleActivity(act);
			statusChanged = true;
		}
		if (Bagging.precondition() == true)
		{
			Bagging act = new Bagging(); // Generate instance																// instance
			act.startingEvent();
			scheduleActivity(act);
			statusChanged = true;
		}
		return (statusChanged);
	}
	
	protected double stopTime; // end of observation interval

	public boolean implicitStopCondition() // termination explicit
	{
		boolean retVal = false;
		if (getClock() >= stopTime)
			retVal = true;
		return (retVal);
	}
	
	public void eventOccured()
	{			
		if(logFlag) printDebug();
	}
	
	// for Debugging
	boolean logFlag = true;
	protected void printDebug()
	{
		System.out.println("Clock = " + getClock());
		for(int id=Constants.C1; id<=Constants.C20; id++) {
			if(rcCounters[id].uOpen || rcCounters[id].customer!=null) {
				System.out.print("id: " + id + "; n queue: " + qCustLines[id].getN() + "; open: " + (rcCounters[id].uOpen?"True":"False") + "; state: " + rcCounters[id].state + "; Bag.: " + rcCounters[id].baggerPresent);
				if (rcCounters[id].customer != null)
				{	
					System.out.println("; Cust.: True; paymethod: "+ rcCounters[id].customer.payMethod);
				}
				else
				{
					System.out.println("; Cust.: False");
				}
			}
		}
		System.out.println("Supervisor busy: "+rSupervisor.isBusy+"; QApproveLength: "+qApproveLine.getN());
		System.out.println("n bagger avail: " + rgBaggers.nAvail);
		System.out.println("cash sched: " + cashierSchedule[(int) getClock()/30]);
		System.out.println("bag sched: " + baggerSchedule[(int) getClock()/30]);
		showSBL();
		System.out.println(">-----------------------------------------------------------------<");		
	}

}



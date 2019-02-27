

import simModel.*;
import cern.jet.random.engine.*;
import outputAnalysis.ConfidenceInterval;
import java.util.Arrays;
import java.util.ArrayList;

public class Experimentation {

	static final int NUMRUNS = 1000; //number of runs per experiment, should be 1500 for a confidence level of .95, or 1000 for a confidence level of .90
	static final double confidence = 0.90; //confidence level for the CI. we are sure to confidence*100% that our estimated values lie within the CI.
	static final double THRESHOLD = 0.10; // maximum proportion of customer waiting more than 15 min allowed
	static final double startTime=0.0, endTime=480; //simulation start and end times.
	static RandomSeedGenerator rsg = new RandomSeedGenerator(); //random seed generator

	/*
	 *  verifies that for each period the proportion of customers waiting more than 15 min is below CEIL
	 */
	static public boolean waitOK(double [] propLongWait, double ceil) {
		for(int i = 0; i<16; i++) {
			if(propLongWait[i]>ceil)
				return false;
		}
		return true;
	}

	/*
	 * This function executes an experiment with given schedule, by running the simulation 1000 times, and returning the CI for each period.
	 */
	static public ConfidenceInterval[] runSchedule(int [] cashierSchedule, int []baggerSchedule,Seeds[] seeds, boolean verbose) {
		SMSuperstore model; // instantiate model
		ConfidenceInterval [] intervals = new ConfidenceInterval[16];
		double [][] values = new double[16][NUMRUNS]; 

		for(int i = 0; i<NUMRUNS; i++) {
			model = new SMSuperstore(startTime, endTime,cashierSchedule,baggerSchedule,seeds[i],false);
			model.runSimulation();
			double [] results = model.getPropLongWait();
			for(int j = 0; j<16; j++) {
				values[j][i] = results[j];
			}
		}

		for(int j =0; j< 16; j++) {
			intervals[j]=new ConfidenceInterval(values[j],confidence);
		}

		// debugging print enable by parameter verbose
		if(verbose) {
			System.out.printf("-------------------------------------------------------------------------------------\n");
			System.out.printf("Comparison    Point estimate(ybar(n))  s(n)     zeta   CI Min   CI Max |zeta/THRESHOLD|\n");
			System.out.printf("-------------------------------------------------------------------------------------\n");
			for(int j=0; j<16; j++) {
				System.out.printf("Period: %2d %13.3f %18.3f %8.3f %8.3f %8.3f %14.3f\n",j,
						intervals[j].getPointEstimate(), intervals[j].getVariance(), intervals[j].getZeta(), 
						intervals[j].getCfMin(), intervals[j].getCfMax(),
						Math.abs(intervals[j].getZeta()/THRESHOLD));
			}
		}
		return intervals;
	}


	public static void main(String[] args) {
		//cashiers schedule
		int [] cashierSchedule= {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}; 
		//bagger schedule, initially set to 20 per period to eliminate bagger bottleneck.
		int [] baggerSchedule= {20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20}; 
		//proportion of customer that wait more than 15 minutes at each period.
		double[] propLongWait = new double[16]; 
		//number of employee delta for each period, only useful for printing/debuging purposes.
		double [] delta = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; 
		//used to compute the period at which the newly added employee will start and how many periods he will work.
		int shiftStart, shiftDuration; 
		int run=0; //number of simulation runs
		//seeds for the 1000 iteration of each run. All the runs will use the same 1000 seeds to reduce variance.
		Seeds [] seeds = new Seeds[NUMRUNS]; 
		/*
		 * Uncomment this block to get different seeds between two runs of the schedule optimization.
		 * 
		int randomSeed=(int) System.currentTimeMillis()%10;
 		for(int i=0; i<randomSeed; i++) {
			for(int j=0; j<NUMRUNS;j++) {
				rsg.nextSeed();
			}
		}
		 */
		
		for(int i=0; i<NUMRUNS;i++) {//generate the 1000 seeds.
			seeds[i]=new Seeds(rsg);
		}

		ConfidenceInterval [] intervals = new ConfidenceInterval[16]; //16 confidence interval, one for each period.
		System.out.println("Initiating Cashier schedule optimization...");
		intervals=runSchedule(cashierSchedule, baggerSchedule, seeds, true); //get base line number.
		for(int i=0; i<16; i++) {
			//use CfMax instead of point estimate so that the actual value is 90% certain to be below CEIL
			propLongWait[i]=intervals[i].getCfMax();
		}

		//This is the cashier schedule loop.
		while(!waitOK(propLongWait,THRESHOLD)) { //have we met our objective?
			run +=1; // new experiment going on
			System.out.printf("\nRun: %d \n", run);
			System.out.printf("Current long wait proportion: %s\n", Arrays.toString(propLongWait));
			shiftStart = -1; // initialized to -1 since it will be increased in the do..while
			do{
				shiftStart+= 1;
			}while(shiftStart < 11 && propLongWait[shiftStart]<THRESHOLD) ; //find first period where customers wait too much, and use it as start of shift for next cashier.
			// Cannot start a shift after 11 otherwise the cashier won't be able to work long enough
			if(shiftStart>0) {
				shiftStart -=1; //The accumulation of customers in the previous period affects the results of the current period, 
				//so make the employee start one period earlier.
			}
			shiftDuration=5; // initialized to 5 since it is going to be incremented to 6 in the do..while. Start with shift duration equal to 6 so that employee works at least 3 hours.
			do {
				shiftDuration +=1;
			}while(shiftDuration < 10 && shiftStart+shiftDuration < 16 && propLongWait[shiftStart+shiftDuration-1]>=THRESHOLD);//Shift duration should be less or equal to 5 hours, and end of shift should be at most the last period.
			for(int i=0; i<16; i++) { //initialization 
				delta[i]=0;
			}
			for(int i=0; i<shiftDuration; i++) {
				delta[shiftStart+i]+=1; //add one employee between shift start and shift start+duration
			}
			System.out.printf("Proposed modification: %s \n", Arrays.toString(delta));
			for(int i=0; i<16; i++) {
				cashierSchedule[i]+=delta[i]; //apply schedule modification
			}
			System.out.printf("New Cashier schedule: %s \n", Arrays.toString(cashierSchedule));
			intervals = runSchedule(cashierSchedule,baggerSchedule, seeds, false); //run new experiment with new schedule
			for(int i=0; i<16; i++) {
				propLongWait[i]=intervals[i].getCfMax(); //update propLongWait
			}

		}
		System.out.printf("\n\n\nOptimal Cashier Schedule: %s\n", Arrays.toString(cashierSchedule));
		System.out.printf("Proportion of long wait times: %s\n", Arrays.toString(propLongWait));
		System.out.println("Cashier schedule optimization done.\nBagger schedule optimization begining...");
		for(int i=0; i<16; i++) {
			baggerSchedule[i]=0 ; // reset bagger schedule to begin optimization
		}

		run +=1;
		intervals=runSchedule(cashierSchedule, baggerSchedule, seeds, false); // get baseline results.
		
		for(int i=0; i<16; i++) {
			propLongWait[i]=intervals[i].getCfMax();
		}
		
		ArrayList<Integer> backtrackingStart = new ArrayList<Integer>(); // Arraylist used to keep track of the baggers shift start as we add them, so that we can undo our modifications if need be.
		ArrayList<Integer> backtrackingDuration = new ArrayList<Integer>();// Arraylist used to keep track of the baggers shift duration as we add them, so that we can undo our modifications if need be.
		
		
		//bagger main loop, see comments of cashier loop for more details as they are almost the same.
		while(!waitOK(propLongWait,THRESHOLD)) { //have we met our objective?
			run +=1; // new experiment done here
			System.out.printf("\nRun: %d \n", run);
			System.out.printf("Current long wait proportion: %s\n", Arrays.toString(propLongWait));
			shiftStart = -1; // 
			do{
				shiftStart+= 1;
			}while(shiftStart < 11 && propLongWait[shiftStart]<THRESHOLD) ; // find bagger shift start according to schedule constraints
			if(shiftStart>0) {
				shiftStart -= 1; //previous period as a lot of impact on current period, so start a period earlier.
			}
			shiftDuration=5;
			do {
				shiftDuration +=1;
			}while(shiftDuration < 10 && shiftStart+shiftDuration < 16 && propLongWait[shiftStart+shiftDuration-1]>=THRESHOLD); //find shift duration according to schedule constraints.

			backtrackingStart.add(shiftStart); //add shift start to employee history
			backtrackingDuration.add(shiftDuration);// add shift duration employee history

			for(int i=0; i<16; i++) {
				delta[i]=0;
			}
			for(int i=0; i<shiftDuration; i++) {
				delta[shiftStart+i]+=1; //add employee to schedule
			}

			for(int i=0; i<16; i++) {
				baggerSchedule[i]+=delta[i]; //make modifications effective
			}

			// verify that there is no schedule problems
			int problemPeriod=0; // holds the index of the problematic period.
			do {
				problemPeriod++;
			}while(problemPeriod<16 && baggerSchedule[problemPeriod]<=cashierSchedule[problemPeriod]); // scan each period, and verify that there aren't more baggers than cashiers

			/* 
			 * In this block we will apply a backtracking algorithm to fix cases where our simple algorithm tries to add more baggers
			 * than there are cashiers in a given period. This can happen because baggers have less influence on the queue times
			 * compared to cashiers, and so interdependence between periods is more important when optimizing the bagger's schedule.
			 * The basic working principle of this algorithm is as follows:
			 *   * find the first period where there are more baggers than cashiers
			 *   * find the first period P1 where the number of baggers is equal to the number of baggers 
			 *   * using the history of the employees, remove all employees with shifts starting at and after P1
			 *   * add a new employee starting at period P1-1, and continue the optimization.
			 */
			if (problemPeriod<16) {
				
				for(int i=0; i<16; i++) { // clear delta so we can use it to see the backtracking modifications
					delta[i]=0;
				}
				System.out.printf("Proposed bagger schedule: %s \n", Arrays.toString(baggerSchedule));
				System.out.printf("Current cashier schedule: %s \n", Arrays.toString(cashierSchedule));
				System.out.printf("original  problmeStart: %d\n", problemPeriod);
				while(problemPeriod>0 && cashierSchedule[problemPeriod-1]==baggerSchedule[problemPeriod-1]) { //find first period where there is room for a new employee
					problemPeriod--;
				}
				System.out.printf("updated problmeStart: %d\n", problemPeriod);
				System.out.println("Shift start history before backtracking: " + Arrays.toString(backtrackingStart.toArray()));
				System.out.println("Shift duration history before backtracking: " + Arrays.toString(backtrackingDuration.toArray()));
				int beginning = -1; //this variable will hold the index of the first "problematic" employee in the history of employees 
				for(int i=0; i<backtrackingStart.size(); i++) { //scan history of employees
					if(backtrackingStart.get(i)>=problemPeriod) { //employee that started later than the problematic period
						if(beginning==-1) beginning=i; //if it's the first of such employee, update beginning 
						for(int j=0; j<backtrackingDuration.get(i); j++) { //remove the employee from the schedule.
							baggerSchedule[problemPeriod+j] --;
							delta[problemPeriod+j]--;
						}
					}
				}//at this stage we have removed all the employees starting later than the problematic period
				
				
				int newDuration = backtrackingDuration.get(beginning); //new employee gets same duration than the employee it's replacing
				for(int i=0; i<newDuration; i++) {
					baggerSchedule[problemPeriod-1+i] ++;//add new employee to schedule, starting at period before problematic period.
					delta[problemPeriod-1+i]++;
				}

				int nbToRemove = backtrackingStart.size()-beginning; // number of employees removed during back tracking phase
				for(int i=nbToRemove; i>0; i--) {
					backtrackingStart.remove(beginning+i-1);//clear history
					backtrackingDuration.remove(beginning+i-1);
				}
				backtrackingStart.add(problemPeriod-1);//add new employee to history
				backtrackingDuration.add(newDuration);
				
				// at this stage the backtracking is over, the optimization process can continue normally
				System.out.printf("backtracking modification to the schedule: %s\n", Arrays.toString(delta));
				System.out.println("Shift start history after backtracking: " + backtrackingStart);
				System.out.println("Shift duration history after backtracking: " + backtrackingDuration);
			}
			System.out.printf("Proposed modification: %s \n", Arrays.toString(delta));
			System.out.printf("Cashier schedule: %s\n", Arrays.toString(cashierSchedule));
			System.out.printf("New bagger schedule: %s \n", Arrays.toString(baggerSchedule));
			intervals = runSchedule(cashierSchedule,baggerSchedule, seeds, false); //run new experiment with updated schedule.
			for(int i=0; i<16; i++) {
				propLongWait[i]=intervals[i].getCfMax();
			}
		}
		System.out.printf("\n\n\nOptimal bagger Schedule: %s\n", Arrays.toString(baggerSchedule));
		System.out.printf("Proportion of long wait times: %s\n", Arrays.toString(propLongWait));

		System.out.println("\n\nFinal schedules: ");
		System.out.printf("Cashier schedule: %s\n", Arrays.toString(cashierSchedule));
		System.out.printf("Bagger schedule:  %s\n", Arrays.toString(baggerSchedule));
		System.out.println("Detailed stats on the confidence interval for the final schedule: ");
		runSchedule(cashierSchedule,baggerSchedule,seeds,true);

	}

}

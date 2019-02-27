// File: Experiment.java
// Description:

import simModel.*;
import cern.jet.random.engine.*;

// Main Method: Experiments
// 
class Experiment
{
	 
	/*
	 * transform a list and String to be able to print it with the common methods like println
	 */
	public static String toString(double[] list) {
		String result = "[";
		for(int i=0; i<list.length-1; i++) {
			result += ((Double) list[i]).toString() +", ";	
		}
		result += ((Double) list[list.length-1]).toString() +"]";
		return result;
		
	}
	public static void main(String[] args){
       int i, NUMRUNS = 1; 
       double startTime=0.0, endTime=480.0;
       Seeds[] sds = new Seeds[NUMRUNS];
       SMSuperstore mname;  // Simulation object
       
       int [] cashierSchedule = {2,1,20,20,20,20,20,20,20,20,20,20,20,20,20,20};
       int [] baggerSchedule = {2,1,20,20,20,20,20,20,20,20,20,20,20,20,20,20};

       // Lets get a set of uncorrelated seeds
       RandomSeedGenerator rsg = new RandomSeedGenerator();
       for(i=0 ; i<NUMRUNS ; i++) sds[i] = new Seeds(rsg);
       
       // Loop for NUMRUN simulation runs for each case
;
       for(i=0 ; i < NUMRUNS ; i++)
       {
          mname = new SMSuperstore(startTime,endTime,cashierSchedule,baggerSchedule,sds[i], true);
          mname.runSimulation();
          // See examples for hints on collecting output
          // and developping code for analysis
          System.out.println(toString(mname.getPropLongWait()));
       }
   }
}

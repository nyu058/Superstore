
import simModel.*;
import cern.jet.random.engine.*;
import outputAnalysis.ConfidenceInterval;
import java.util.Arrays;

public class ExperimentConfidence {
	
	static final double THRESHOLD = 0.10;
	
	public static String toString(double[] list) {
		String result = "[";
		for(int i=0; i<list.length-1; i++) {
			result += String.format("%.2f", list[i]) +", ";	
		}
		result += String.format("%.2f", list[list.length-1]) +"]";
		return result;
	}
	
	public static void main(String[] args) {
		SMSuperstore store;
		int NUMRUNS = 1500;
		int [] runs = {800,1000,1100,1200,1300,1500};
		double confidence = 0.95;
		int [] cashierSchedule= {3, 4, 5, 6, 6, 7, 7, 7, 7, 7, 9, 7, 6, 5, 5, 5};
		int [] baggerSchedule= {3, 3, 4, 6, 6, 6, 6, 6, 6, 5, 7, 6, 5, 5, 5, 5};
		
		double startTime=0.0, endTime=480;
		
		ConfidenceInterval [][] intervals = new ConfidenceInterval[6][16];
		double [][] values = new double[16][NUMRUNS];
		RandomSeedGenerator rsg = new RandomSeedGenerator();
		
		for(int i = 0; i<NUMRUNS; i++) {
			store = new SMSuperstore(startTime, endTime,cashierSchedule,baggerSchedule,new Seeds(rsg),false);
			store.runSimulation();
			double [] results = store.getPropLongWait();
			for(int j = 0; j<16; j++) {
				values[j][i] = results[j];
			}
		}
		
		for(int i = 0; i<6;i++) {
			for(int j =0; j< 16; j++) {
				intervals[i][j]=new ConfidenceInterval(Arrays.copyOfRange(values[j], 0, runs[i]),confidence);
			}
		}
		
		System.out.printf("-------------------------------------------------------------------------------------\n");
		System.out.printf("Comparison    Point estimate(ybar(n))  s(n)     zeta   CI Min   CI Max |zeta/THRESHOLD|\n");
		System.out.printf("-------------------------------------------------------------------------------------\n");
		for(int i=0; i<6; i++) {
			System.out.printf("Number of runs: %3d\n",runs[i]);
			for(int j=0; j<16; j++) {
				System.out.printf("Period: %2d %13.3f %18.3f %8.3f %8.3f %8.3f %14.3f\n",j,
		       	         intervals[i][j].getPointEstimate(), intervals[i][j].getVariance(), intervals[i][j].getZeta(), 
		       	      intervals[i][j].getCfMin(), intervals[i][j].getCfMax(),
		    	         Math.abs(intervals[i][j].getZeta()/THRESHOLD));
			}
		}

	}

}

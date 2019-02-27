package simModel;
import simulationModelling.*;

/*
 * Implements the scheduled action ApplySchedule
 */
public class ApplySchedule extends ScheduledAction{

	static SMSuperstore model;
	
	
	// time sequence contains two time more elments than number of period: 
	// one for the period transition and one 10 min earlier to close counters
	private static final double [] scheduleChange = {20, 30, 50, 60, 80, 90, 110, 120, 140, 150, 170, 180, 200,
				  210, 230, 240, 260, 270, 290, 300, 320, 330, 350, 360, 380, 
				  390, 410, 420, 440, 450};
	private static int current_schedule;
	
	
	// used to initialize the current schedule.
	protected static void init() {
		current_schedule=-1;
	}
	
	/*
	 * @return The next value of time at which the schedules will be updated
	 */
	@Override
	protected double timeSequence() {
		if(model.getClock()<450) {
			current_schedule +=1; //move to next schedule index
			return scheduleChange[current_schedule];
		}
		return model.stopTime+1; //do not reschedule this action after last period change
	}
	
	/*
	 * Updates the schedules by using the DVP.openCloseCounters() to set the right number of open
	 * counters, and the formula below for the baggers
	 */
	@Override
	protected void actionEvent() {
		
		//upcoming period
		int next_period=(int)(model.getClock() + 10)/30;  // the +10 ensure the correct upcoming period with regard to the action time sequence
		
		//close counters ten minutes before period end
		if((int)model.getClock()%30 !=0 && model.cashierSchedule[next_period] < model.cashierSchedule[next_period-1] ) {
			model.dvp.openCloseCounters();
		}
		
		//update bagger schedule on time
		if((int)model.getClock()%30 == 0) {
		
			
			/*
			 * model.rgBaggers.navail only represents the number of idle baggers, so
			 * simply replacing it's value would not account for the baggers still busy, 
			 * effectively yielding more baggers than specified for this period. Instead,
			 * we only add the difference of number of cashiers to ensure the proper number 
			 * of baggers are working the next shift. This may yield a negative number of
			 * available baggers, to be interpreted as the number of baggers working 'over time'
			 */
			model.rgBaggers.nAvail += model.baggerSchedule[(int)(model.getClock() + 10)/30]-
					model.baggerSchedule[(int)(model.getClock() + 10)/30-1];
			
			//open counters on time
			if(model.cashierSchedule[next_period] > model.cashierSchedule[next_period-1]) {
				model.dvp.openCloseCounters();
			}
		
		}
		
	}
	
}

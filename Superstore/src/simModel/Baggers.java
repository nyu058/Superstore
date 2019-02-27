package simModel;


/*
 * Resource Group representing the baggers
 */
public class Baggers {

	/* nAvail value is decremented each time a bagger is assigned to a counter 
	* and incremented when a bagger is freed from a counter. 
	* At schedule change, the number may become negative to show that some baggers 
	* are working overtime.
	*/
	protected int nAvail; // The number of baggers available
	
}

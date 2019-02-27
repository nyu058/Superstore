package simModel;

import cern.jet.random.engine.RandomSeedGenerator;

/*
 * This class defines random seeds for all the random variate procedures defined in the class RVPs.
 * Each RVP gets it's own* seed.
 */

public class Seeds 
{
	int [] seedArrival = new int[16];   //seeds for all the arrivals.
	int seedItemA;   // comment 2
	int seedItemB;   // comment 3
	int seedItemCat;   // comment 4
	
	int seedPayMethodCat;
	int seedCheckWithCard;
	
	int seedScanTime;
	int seedPriceCheckTime;
	int seedPriceCheck;
	
	int seedCash;
	int seedCreditCard;
	int seedCheck;
	
	int seedAppTime;
	
	int seedBagTime;

	public Seeds(RandomSeedGenerator rsg)
	{
		for(int i = 0; i<16; i++) {
			seedArrival[i]=rsg.nextSeed();
		}
		seedItemA=rsg.nextSeed();
		seedItemB=rsg.nextSeed();
		seedItemCat=rsg.nextSeed();
		seedPayMethodCat=rsg.nextSeed();
		seedCheckWithCard=rsg.nextSeed();
		seedScanTime=rsg.nextSeed();
		seedPriceCheckTime=rsg.nextSeed();
		seedPriceCheck=rsg.nextSeed();
		seedCash=rsg.nextSeed();
		seedCreditCard=rsg.nextSeed();
		seedCheck=rsg.nextSeed();
		seedAppTime=rsg.nextSeed();
		seedBagTime=rsg.nextSeed();
	}
}

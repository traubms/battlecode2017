package battlecode2017;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode2017.AbstractBot;

/**
 * Needs RobotPlayer to work, and all of the other classes define the logic, or what happens
 * when it runs. You never need to look at this file. 
 */
public class RobotPlayer {

	// Run at the start of each unit's existence.
	public static void run(RobotController rc) throws GameActionException
	{
		AbstractBot myLogic;
		
		switch (rc.getType()) {
	        case ARCHON:
	            myLogic = new Archon(rc);
	            break;
	        case GARDENER:
	        	myLogic = new Gardener(rc);
	            break;
	        case SOLDIER:
	        	myLogic = new Soldier(rc);
	            break;
	        case LUMBERJACK:
	        	myLogic = new Lumberjack(rc);
	            break;
	        case SCOUT:
	        	myLogic = new Scout(rc);
	            break;
	        case TANK:
	        	myLogic = new Tank(rc);
	            break;
	        default:
	        	throw new GameActionException(null, "Bad RobotType");
	    }

		
		/*
		 * Run unit logic for the duration of the game.
		 */
		int round;
		while(true)
		{
			round = rc.getRoundNum();
			try{
				myLogic.donateBulletsToWin();
				myLogic.incrementalDonate();
				myLogic.run();
			} catch (Exception e) {
                e.printStackTrace();
            }
			if(round == rc.getRoundNum())
				Clock.yield();
		}
	}
	
}


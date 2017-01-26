package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Scout extends AbstractBot {

	public Scout(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	
	public void run() throws GameActionException {
        trees.update();
        bots.update();
        dodge();
        shake();
        MapLocation goal = nearestEnemyBotOrTreeOrBulletTree();
        if (goal == null)
            wander();
        else
        	moveTowardsOrWander(goal);
        attack();
    }
}

package battlecode2017;

import battlecode.common.GameActionException;
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
        moveToArchon();
        //moveTowardsOrWander(nearestEnemyBotOrTreeOrBulletTree());
        attack();
    }
}

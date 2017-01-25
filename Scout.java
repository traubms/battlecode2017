package battlecode2017;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;

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
		moveTowardsOrWander(nearestEnemyBotOrTreeOrBulletTree());
		attack();
	}

}

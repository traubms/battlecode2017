package battlecode2017;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import java.util.List;

public class Soldier extends AbstractBot {

	public Soldier(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

	public void run() throws GameActionException {
		trees.update();
		bots.update();
		shake();
		dodge();
		attack();
		if (radio.forwardMarch())
			potentialMove(enemyArchLoc);
		else
			moveTowardsOrWander(this.nearestEnemyBotOrTree());
	}
}

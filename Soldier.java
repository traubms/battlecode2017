package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Soldier extends AbstractBot {

	public Soldier(RobotController rc) {
		super(rc);
	}

	public void run() throws GameActionException {
		trees.update();
		bots.update();
		shake();
		dodge();
		attack();
		radio.reportEnemies(bots);
		if (radio.forwardMarch()){
			swarm = true;
		if (swarm)
			followMarchingOrders();
		} else {
			moveTowardsOrWander(this.nearestEnemyBotOrTree());
		}
	}
}

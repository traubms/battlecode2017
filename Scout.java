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

		if (rc.senseNearbyBullets().length > 0) { //try dodging bullets as a first priority
			dodge();
		}

		ArrayList<TreeInfo> bulletTrees = trees.getBulletTrees();
		if (bulletTrees.size() > 0){ //if no dodge was attempted, then seek out neutral bullet trees
			if(!shake()){
				this.tryMove(rc.getLocation().directionTo(bulletTrees.get(0).location));
			}
		} else {
			wander();
		}
	}
}

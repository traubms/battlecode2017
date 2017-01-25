package battlecode2017;

import battlecode.common.*;

public class Scout extends AbstractBot {

	public Scout(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void run() throws GameActionException {
		trees.update();
		MapLocation myLocation = rc.getLocation();

		TreeInfo closest_neutral_tree = trees.getClosestTree(Team.NEUTRAL); //get closest neutral TreeInfo object
		if (closest_neutral_tree.containedBullets > 0) { //if said tree has bullets, then move towards it
			if (rc.canInteractWithTree(closest_neutral_tree.getLocation()) && rc.canShake()) {
				rc.shake(closest_neutral_tree.getLocation());
			} else {
				Direction dir = myLocation.directionTo(closest_neutral_tree.getLocation());
				if (rc.canMove(dir)) {
					rc.move(dir);
				}
			}
		}
	}
}

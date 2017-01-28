package battlecode2017;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.MapLocation;
import battlecode.common.TreeInfo;
import battlecode.common.RobotInfo;

import java.util.List;

public class Tank extends AbstractBot {

	public Tank(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

	public void run() throws GameActionException {
		trees.update();
		bots.update();
		dodge();
		attack();
		moveTowardsOrWander(this.nearestEnemyBotOrTree());
	}

	public boolean move(Direction dir) throws GameActionException {
		//regular moving
		if (super.move(dir))
			return true;
		else { //try to bash tree
			boolean canBash = false;
			//not perfect but halfway decent
			TreeInfo nearestTree = nearestEnemyTreeOrNeutralTree();

			//if there's no tree nearby, give up
			if (nearestTree==null) {
				return false;
			} else { // see if its close enough to ram
				float degBetween = dir.degreesBetween(rc.getLocation().directionTo(nearestTree.location));
				float distTo = rc.getLocation().distanceTo(nearestTree.location);
				float collideDist = rc.getType().strideRadius + rc.getType().bodyRadius + nearestTree.radius;

				// if will collide with tree, try to smash
				if (distTo < collideDist && degBetween < (float) 45) {
					RobotInfo nearestBot = bots.getClosestbot(team);
					RobotInfo nearestEBot = bots.getClosestbot(team.opponent());
					canBash = true;

					// If any robot in way, don't try to move
					if (nearestBot != null) {
						float degBetween2 = dir.degreesBetween(rc.getLocation().directionTo(nearestBot.location));
						float distTo2 = rc.getLocation().distanceTo(nearestBot.location);
						if (distTo2 < distTo && degBetween2 < (float) 45) canBash = false;
					}
					if (nearestEBot != null) {
						float degBetween3 = dir.degreesBetween(rc.getLocation().directionTo(nearestEBot.location));
						float distTo3 = rc.getLocation().distanceTo(nearestEBot.location);
						if (distTo3 < distTo && degBetween3 < (float) 45) canBash = false;
					}
					//If there's a neutral or enemy tree right in
					if (canBash)
						rc.move(dir);
					return canBash;

				} else { // not close enough
					return false;
				}
			}
		}
	}
}


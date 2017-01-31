package battlecode2017;

import battlecode.common.*;

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
		if (radio.forwardMarch())
			swarm = true;
		if (swarm)
			followMarchingOrders();
		else
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
	/**
	 * Attacks by shooting either a single, triad, or pentad shot.
	 * Only called by soldiers, scouts, and tanks.
	 *
	 * @throws GameActionException
	 */

	public boolean attack() throws GameActionException {

		// choose target
		RobotInfo closestEnemy = bots.getClosestbot(team.opponent());
		TreeInfo closestEnemyTree = trees.getClosestTree(team.opponent());
		BodyInfo target;
		if (closestEnemy!=null) {
			target = closestEnemy;
		} else if (closestEnemyTree != null) {
			target = closestEnemyTree;
		} else {
			target = null;
		}

		//means no shot taken, defend, else move not directly in path of bullets
		if(target == null) {
			RobotInfo inDanger = bots.getWeakestbot(team);
			if (inDanger!=null)
				tryMove(rc.getLocation().directionTo(inDanger.location));
			return false;
		} else {
			Direction directionToTarget = rc.getLocation().directionTo(target.getLocation());
			//back up if swarming and enemy is getting close (and not getting close to a friendly archon), else move towards
			if (inEnemyTerritory() && target.isRobot() && !(closestEnemy.getType().equals(RobotType.ARCHON)
					|| closestEnemy.getType().equals(RobotType.GARDENER))
					&& rc.getLocation().distanceTo(target.getLocation()) < (6 + rc.getType().bodyRadius)) {
				tryMove(directionToTarget.rotateLeftDegrees(180));
			} else tryMove(directionToTarget);

			fireShot(target); // SHOOT
			return true;
		}
	}

	public Direction fireShot(BodyInfo target) throws GameActionException {
		MapLocation myLocation = rc.getLocation(), loc = target.getLocation();
		Direction directionToShoot = myLocation.directionTo(loc);
		float distToEnemy = rc.getLocation().distanceTo(loc);


		//setting appropriate ranges based on type of enemy to reduce wasted bullets and friendly fire
		//tweak the hardcoded numbers as appropriate
		float pentadRange = (float) 5 + rc.getType().bodyRadius;
		float triadRange = (float) 6 + rc.getType().bodyRadius;
		float singleRange = (float) 8 + rc.getType().bodyRadius;

		//if swarming the enemy, just go ahead and blast away.
		if (true) { //messy change
			pentadRange = pentadRange + 5;
			triadRange = triadRange + 4;
			singleRange = singleRange + 3;
		}

		if (target.isRobot()) {
			if (((RobotInfo) target).getType().equals(RobotType.ARCHON) || ((RobotInfo) target).getType().equals(RobotType.TANK)) {
				pentadRange = pentadRange + (float) 1;
				triadRange = triadRange + (float) 1.3;
				singleRange = singleRange + (float) 2.5;
			}
		}

		TreeInfo nearestBadTree = trees.getClosestTree(team.opponent());
		TreeInfo nearestNTree = trees.getClosestTree(Team.NEUTRAL);
		//TreeInfo nearestGoodTree = trees.getClosestTree(team);

		// consider neutral tree to be enemy tree
		if (nearestNTree != null) {
			if (nearestBadTree == null) {
				nearestBadTree = nearestNTree;
			} else if (rc.getLocation().distanceTo(nearestBadTree.location) > rc.getLocation().distanceTo(nearestNTree.location)) {
				nearestBadTree = nearestNTree;
			}
		}

		float directionDifference = (float) 500;
		if (nearestBadTree != null) {
			directionDifference = directionToShoot.degreesBetween(myLocation.directionTo(nearestBadTree.location));
		}

		// decide if surrounding trees affect logic
		boolean dontWorryAboutTrees;
		if (target.isTree())
			dontWorryAboutTrees = true;
		else if (nearestBadTree == null)
			dontWorryAboutTrees = false;
		else
			dontWorryAboutTrees = myLocation.distanceTo(nearestBadTree.location) < (float) 2.5 && directionDifference * directionDifference < (float) 50 * 50;

		boolean single = rc.canFireSingleShot();
		boolean triad = rc.canFireTriadShot();
		boolean pentad = rc.canFirePentadShot();

		//hopefully further prevent friendly fire
		List<RobotInfo> FBots = bots.getBots(team);
		int count = 0;
		float distanceToFriendly;
		float degreesBetweenFriendly;
		for (RobotInfo fb : FBots) {
			count++;
			distanceToFriendly = myLocation.distanceTo(fb.location);
			degreesBetweenFriendly = directionToShoot.degreesBetween(myLocation.directionTo(fb.location));
			if (distanceToFriendly < distToEnemy && degreesBetweenFriendly * degreesBetweenFriendly < 50 * 50) {
				single = false;
				triad = false;
				pentad = false;
				break;
			}
			if (count >= 6) break;
		}
		return null;
	}
}


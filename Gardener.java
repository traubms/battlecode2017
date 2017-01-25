package battlecode2017;

import battlecode.common.*;

import java.util.Map;

public class Gardener extends AbstractBot {

	public Gardener(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void run() throws GameActionException {
		plantTreesAndBuildSoldiers();
	}
	/** Checks if tree can be planted and plants there */
    public void plantTree(Direction dir) throws GameActionException {
    	if (rc.canPlantTree(dir)){
            rc.plantTree(dir);
    	}
    }


	public boolean waterLocation(MapLocation loc) throws GameActionException{
		if (rc.canWater(loc)) {
			rc.water(loc);
			return true;
		} else
			return false;
	}

	/**build functions for various robot types*/
	public boolean build(Direction d, RobotType type) throws GameActionException{
		if (rc.canBuildRobot(type, d)){
			rc.buildRobot(type, d);
			return true;
		} else {
			return false;
		}
	}
	
	/**Determines whether or not can plant a tree at a given location */
	public boolean canPlantLoc(MapLocation plantSite) {
		if ((rc.getLocation().distanceTo(plantSite) - (1 + GameConstants.GENERAL_SPAWN_OFFSET)) <= EPSILON
				&& rc.canPlantTree(rc.getLocation().directionTo(plantSite)))  {
			return true;
		} else {return false;}
	}

	/**Moves to position to plant*/
	public void moveToPlant(MapLocation plantSite) {
        // TODO
    }

	/**
	 * Used by a gardener to build trees and robots.
	 *
	 * @throws GameActionException
	 */
	public void plantTreesAndBuildSoldiers() throws GameActionException {

		// sense the archon
		bots.update();
		Direction directionToMove = null;
		for (RobotInfo robotInfo : bots.getBots(team)) {
			if (robotInfo.getType().equals(RobotType.ARCHON)) {
				Direction directionToArchon = rc.getLocation().directionTo(robotInfo.location);
				directionToMove = directionToArchon.opposite();
				if (rc.canMove(directionToMove))
					rc.move(directionToMove, 2f);
			}
		}

		Direction directionToBuild = directionToMove;
		if (rc.canBuildRobot(RobotType.SOLDIER, directionToBuild)) {
			rc.buildRobot(RobotType.SOLDIER, directionToBuild);
		}

		if (rc.canPlantTree(directionToBuild)) {
			rc.plantTree(directionToBuild);
		}

		float radiansToBuild = directionToBuild.getAngleDegrees() + ((float) Math.PI / 3);
		float step = (float) Math.PI / 3;
		for (float radians = radiansToBuild; radians < 2 * (float) Math.PI + radiansToBuild; radians = radians + step) {
			Direction directionToPlant = new Direction(radians);
			if (rc.canPlantTree(directionToPlant)) {
				rc.plantTree(directionToPlant);
			}
		}
	}


}

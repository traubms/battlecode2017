package battlecode2017;

import battlecode.common.*;

import java.util.Map;

public class Gardener extends AbstractBot {

	public Gardener(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void run() {
		trees.update();
	}
	/** Checks if tree can be planted and plants there */
    public void plantTree(Direction dir) throws GameActionException {
    	if (rc.canPlantTree(dir)){
            rc.plantTree(dir);
    	}
    }

	public void waterClosestTree() throws GameActionException {
		TreeInfo[] trees = rc.senseNearbyTrees();
		if (trees.length > 0){
			waterLocation(trees[0].location);
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


}

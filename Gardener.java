package battlecode2017;

import battlecode.common.*;

import java.util.Map;

public class Gardener extends AbstractBot {

	public Gardener(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void run() {
		System.out.println("gardener");
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
	/**
	 * Waters tree with lowest health
	 * @throws GameActionException
	 */
	public boolean waterTrees() throws GameActionException {
		TreeInfo[] trees = rc.senseNearbyTrees(GameConstants.INTERACTION_DIST_FROM_EDGE, rc.getTeam());
		TreeInfo tree, needs_most = null;
		double health, min_health = 1000000;
		for(int i = 0; i < trees.length; i++){
			tree = trees[i];
			health = tree.getHealth();
			if (health < min_health){
				min_health = health;
				needs_most = tree;
			}
		}
		if (needs_most != null){
			return waterLocation(needs_most.location);
		} else
			return false;
	}

	/**build functions for various robot types*/
	public boolean buildTank(Direction d) throws GameActionException{
		if (rc.canBuildRobot(RobotType.TANK, d)){
			rc.buildRobot(RobotType.TANK, d);
			return true;
		} else {return false;}
	}

	public boolean buildScout(Direction d) throws GameActionException{
		if (rc.canBuildRobot(RobotType.SCOUT, d)){
			rc.buildRobot(RobotType.SCOUT, d);
			return true;
		} else {return false;}
	}

	public boolean buildSoldier(Direction d) throws GameActionException{
		if (rc.canBuildRobot(RobotType.SOLDIER, d)){
			rc.buildRobot(RobotType.SOLDIER, d);
			return true;
		} else {return false;}
	}

	public boolean buildLumberjack(Direction d) throws GameActionException{
		if (rc.canBuildRobot(RobotType.LUMBERJACK, d)){
			rc.buildRobot(RobotType.LUMBERJACK, d);
			return true;
		} else {return false;}
	}
	/**Determines whether or not can plant a tree at a given location */
	public boolean canPlantLoc(MapLocation plantSite) {
		if (rc.getLocation().distanceTo(plantSite) == (1 + GameConstants.GENERAL_SPAWN_OFFSET)
				&& rc.canPlantTree(rc.getLocation().directionTo(plantSite)))  {
			return true;
		} else {return false;}
	}
	/** function for building trees, does not assume sites are in any order
	 * @param plantSites, an array of map locations at which to build trees
	 * @return newPlantSites, an array of map locations of which locations still need to be planted*/

	public MapLocation[] buildTrees(MapLocation[] plantSites) {
		//find nearest site
		double minDist = 1000000000;
		MapLocation nearest = plantSites[0];
		for (MapLocation ps : plantSites) {
			double distTo = rc.getLocation().distanceTo(ps);
			if (distTo < minDist) {
				minDist = distTo;
				nearest = ps;
			}
		}
		if (canPlantLoc(nearest)) {
			// TODO
		}
		return null;
	}
}

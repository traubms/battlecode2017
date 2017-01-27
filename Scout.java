package battlecode2017;

import battlecode.common.*;

import java.util.List;



public class Scout extends AbstractBot {

	public Scout(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	
	public void run() throws GameActionException {
        trees.update();
        bots.update();
        //if (rc.getRoundNum() < 150) moveTo(rc.getInitialArchonLocations(team.opponent())[0]);
        dodge();
        shake();
        hideOnTree();
        MapLocation goal = nearestEnemyBotOrTreeOrBulletTree();
        if (goal == null)
            wander();
        else
        	moveTowardsOrWander(goal);
        attack();
    }

    /** Gets scouts to hide on enemy trees if they see gardeners and enemy trees, doesn't include attacking
     * @return whether or not the scout moved (returns false if couldn't sense gardener and enemy tree as well)
     * @throws GameActionException
     */

    public boolean hideOnTree() throws GameActionException {
        //Find closest enemy gardener
	    List<RobotInfo> EBots = bots.getBots(team.opponent());
	    RobotInfo closestGardener = null;
	    for (RobotInfo ri : EBots) {
	        if (ri.getType().equals(RobotType.GARDENER)) {
	            closestGardener = ri;
	            break;
            }
        }
        //If found enemy gardener, find enemy tree closest to said gardener
        if (closestGardener!=null) {
	        List<TreeInfo> nearTrees = trees.getTrees(team.opponent());
	        TreeInfo closest2gardener = null;
	        for (TreeInfo ti : nearTrees) {
	            //long if statement is "if we haven't found a tree yet or this tree is closer to the gardener than the previous closest"
	            if (closest2gardener == null
                        || closestGardener.location.distanceTo(ti.location)
                        < closestGardener.location.distanceTo(closest2gardener.location)) {
	                closest2gardener = ti;
                }
            }
            //If found appropriate gardener and tree, make a location slightly offset from the center of the tree
            //in the direction of the gardener and try to move there.
            if (closest2gardener != null) {
	            MapLocation slightlyOff =
                        closest2gardener.location.add(closest2gardener.location.directionTo(closestGardener.location), (float) 0.02);
	            return moveTo(slightlyOff);
            }
        }
        return false;
    }
}

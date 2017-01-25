package battlecode2017;

import battlecode.common.*;

import java.util.Map;

public class Gardener extends AbstractBot {

	public Gardener(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

	public void run() throws GameActionException {
	    trees.update();
	    int bc = Clock.getBytecodeNum();
	    followBuildCommands();
	    System.out.println(Clock.getBytecodeNum() - bc);
		plantTreesAndBuildSoldiers();
	}

	/** Checks if tree can be planted in a direction and plants there */
    public void plantTree(Direction dir) throws GameActionException {
    	if (rc.canPlantTree(dir)){
            rc.plantTree(dir);
    	}
    }
    
    public void followBuildCommands() throws GameActionException{
    	Map<Codes, Integer> orders = radio.checkBuildOrders();
    	for(Codes code: orders.keySet()){
    		if(orders.get(code) > 0){
    			if (build(code.getRobotType())){
    				radio.reportBuild(code);
    				break;
    			}
    		}
    	}
    }
    public boolean build(RobotType robotType) throws GameActionException{
	    Direction tryBuildDir = BotUtils.randomDirection();
		for (int i = 0; i < 8; i++) {
			tryBuildDir = tryBuildDir.rotateLeftDegrees((float) (360. / 8));
			if (build(tryBuildDir, robotType)){ // if build successful, break and return true
				return true;
			}
		}
		return false; // build not successful, so return false
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

	//TODO Make build robot logic

    //TODO Make planting trees logic

    //TODO Make better movement logic

	public void waterAndMove() throws GameActionException {
	    while (rc.canWater()) {
	        waterWeakest();
        }
        tryMove(rc.getLocation().directionTo(trees.getWeakestTree(team).location));
	    // may be a problem if I CAN'T water and move in the same turn
        // TODO insert your move function here to replace the simple one above
    }

    /**decides whether or not to water the weakest tree within interacting distance
     *
     * @return boolean indicating whether or not a tree was watered
     * @throws GameActionException
     */
	public boolean waterWeakest() throws GameActionException {
	    TreeInfo weakest = trees.getWeakestTreeWithinInteract(team);
        if (weakest.health <= TREE_WATERING_THRESHOLD && rc.canWater(weakest.location)) {
            rc.water(weakest.location);
            return true;
        }
        else {
            return false;
        }
    }


	/**a function for planting a tree at a particular map location*/
	public void plantTree(MapLocation plantSite) throws GameActionException{
	    if (canPlantLoc(plantSite)) {
	        rc.plantTree(rc.getLocation().directionTo(plantSite));
        }
     else { moveToPlant(plantSite);} //can implement return from moveToPlant to determine whether or not moving there is possible at all
    }

	/**Determines whether or not can plant a tree at a given location */
	public boolean canPlantLoc(MapLocation plantSite) throws GameActionException {
		if ((rc.getLocation().distanceTo(plantSite) - (1 + GameConstants.GENERAL_SPAWN_OFFSET+rc.getType().bodyRadius)) <= EPSILON
				&& rc.canPlantTree(rc.getLocation().directionTo(plantSite)))  {
			return true;
		} else {return false;}
	}

	/**Moves to position to plant
     * @param plantSite : location that the tree should be (the center of the tree)
     * @return true if we made progress or are there, else return false
     * @throws GameActionException
     * */
	public boolean moveToPlant(MapLocation plantSite) throws GameActionException{
        if (((rc.getLocation().distanceTo(plantSite) - (1 + rc.getType().bodyRadius+GameConstants.GENERAL_SPAWN_OFFSET)) > EPSILON)) {
            if (rc.getLocation().distanceTo(plantSite) > (rc.getType().bodyRadius+(float)1+rc.getType().strideRadius+GameConstants.GENERAL_SPAWN_OFFSET)) {
                tryMove(rc.getLocation().directionTo(plantSite));
            } else {
                MapLocation[] intPoints = BotUtils.findCircleIntersections(rc.getLocation(), plantSite, rc.getType().bodyRadius, (float) 1);
                MapLocation nearPoint = plantSite.add(plantSite.directionTo(rc.getLocation()), (float) 1 + GameConstants.GENERAL_SPAWN_OFFSET + rc.getType().strideRadius);
                boolean hasMoved = false;
                if (rc.canMove(nearPoint)) {
                    rc.move(nearPoint);
                    hasMoved = true;
                } else {

                    for (MapLocation iP : intPoints) {
                        MapLocation To = iP.add(iP.directionTo(rc.getLocation()), rc.getType().bodyRadius);
                        if (rc.canMove(To)) {
                            rc.move(To);
                            hasMoved = true;
                            break;
                        }
                    }
                }
                if (!hasMoved) {
                    tryMove(rc.getLocation().directionTo(plantSite), 30, 3);
                }
            }
            if (!((rc.getLocation().distanceTo(plantSite) - (1 + rc.getType().bodyRadius+GameConstants.GENERAL_SPAWN_OFFSET)) > EPSILON)) {
                return false;
            } else { return true; }
        }
        return true;
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

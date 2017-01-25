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
	    dodge();
	    waterAndMove();
	    followBuildCommands();
	}

	/** Checks if tree can be planted in a direction and plants there 
	 * @return */
    public boolean plantTree(Direction dir) throws GameActionException {
    	if (rc.canPlantTree(dir)){
            rc.plantTree(dir);
            return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean plantTree() throws GameActionException{
	    Direction tryPlantDir = BotUtils.randomDirection();
		for (int i = 0; i < 8; i++) {
			tryPlantDir = tryPlantDir.rotateLeftDegrees((float) (360. / 8));
			if (this.plantTree(tryPlantDir)){ // if build successful, break and return true
				return true;
			}
		}
		return false; // build not successful, so return false
    }
    
    public void followBuildCommands() throws GameActionException{
    	Map<Codes, Integer> orders = radio.checkBuildOrders();
    	for(Codes code: orders.keySet()){
    		if(orders.get(code) > 0){
    			if (build(code)){
    				radio.reportBuild(code);
    				break;
    			}
    		}
    	}
    }
    
    public boolean build(Codes code) throws GameActionException{
    	if (code == Codes.TREE){
    		return plantTree();
    	} else {
    		return build(code.getRobotType());
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
    
    /**build functions for various robot types*/
	public boolean build(Direction d, RobotType type) throws GameActionException{
		if (rc.canBuildRobot(type, d)){
			rc.buildRobot(type, d);
			return true;
		} else {
			return false;
		}
	}

	public boolean waterLocation(MapLocation loc) throws GameActionException{
		if (rc.canWater(loc)) {
			rc.water(loc);
			return true;
		} else
			return false;
	}

	//TODO Make build robot logic
	public boolean buildLogic() throws GameActionException {
	    //check queue

        //see if can build and want to build

        //build

        //remove from queue

        //add to count of living bots of that type
        return true;
    }

    //TODO Make planting trees logic
    public boolean plantLogic() throws GameActionException {
	    //check trees around

        //see if its reasonable to build a tree

        //if so, build in the 4 / 6 hexagonal spots, leaving an exit route
        return true;
    }

    //TODO Make better movement logic
    public boolean moveLogic() throws GameActionException {
	    //check if there are enemies, maybe just scouts, within a certain distance or on trees

        //if so, run away, notify soldier

	    //check to see what I should be doing (e.g. finding a place to plant trees)

        //try to sense such a location for building trees that is most convenient

        //go there

        return true;
    }

	public void waterAndMove() throws GameActionException {
		TreeInfo weakest = trees.getWeakestTree(team);
	    if(trees.getTreesWithinInteract(team).size() > 0){
	    	waterWeakest();
	    } else if (weakest != null){
	    	tryMove(rc.getLocation().directionTo(weakest.location));
	    } else {
	    	wander();
	    }
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
        if (weakest != null && weakest.health <= TREE_WATERING_THRESHOLD && rc.canWater(weakest.location)) {
            rc.water(weakest.location);
            return true;
        } else {
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

	public MapLocation[] possiblePositionsForPlanting(MapLocation myLoc, MapLocation plantSite) throws GameActionException{
		MapLocation[] intPoints = BotUtils.findCircleIntersections(rc.getLocation(), plantSite, rc.getType().bodyRadius, (float) 1);
        MapLocation nearPoint = plantSite.add(plantSite.directionTo(rc.getLocation()), (float) 1 + GameConstants.GENERAL_SPAWN_OFFSET + rc.getType().strideRadius);
        MapLocation[] result = new MapLocation[1 + intPoints.length];
        result[0] = nearPoint;
        MapLocation iP;
        for(int i = 0; i < intPoints.length; i++){
        	iP = intPoints[i];
        	result[i+1] = iP.add(iP.directionTo(rc.getLocation()), rc.getType().bodyRadius);
        }
        return result;
        
	}
	
	/**Moves to position to plant
     * @param plantSite : location that the tree should be (the center of the tree)
     * @return true if we moved
     * @throws GameActionException
     * */
	public boolean moveToPlant(MapLocation plantSite) throws GameActionException{
		// include bot radius and plant offset to find goal distance from plantSite
		float goalDist = 1 + rc.getType().bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET;
        
		// return True if within epsilon
		if ((rc.getLocation().distanceTo(plantSite) - goalDist <= EPSILON)) 
        	return true;
		
		// if outside stride, take big step toward goal, else move into position
		boolean hasMoved = false;
        if (rc.getLocation().distanceTo(plantSite) > goalDist + rc.getType().strideRadius) {
            hasMoved = tryMove(rc.getLocation().directionTo(plantSite));
        } else {
            MapLocation[] intPoints = possiblePositionsForPlanting(rc.getLocation(), plantSite);
        	for (MapLocation iP : intPoints) {
                if (rc.canMove(iP)) {
                    rc.move(iP);
                    hasMoved = true;
                    break;
                }
            }
        }
        // if it doesn't work, move to side
        if (!hasMoved) 
            return tryMove(rc.getLocation().directionTo(plantSite), 30, 3);
        else
        	return hasMoved; // always True
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

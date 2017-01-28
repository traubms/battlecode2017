package battlecode2017;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gardener extends AbstractBot {
	
	MapLocation base;
	Direction dirToBase;
	List<MapLocation> treeTargets;
	List<MapLocation> builtTrees;
	boolean makeTree;
	
	public Gardener(RobotController rc) {
		super(rc);
		bots.update();
		for(RobotInfo bot : bots.getBots(team)){
			if (bot.getType() == RobotType.ARCHON){
				this.base = bot.location;
				this.dirToBase = rc.getLocation().directionTo(base);
				break;
			}
		}
		this.treeTargets = calculateTreeTargets();
		this.builtTrees = new ArrayList<MapLocation>();
		this.makeTree = false;
	}

	public void run() throws GameActionException {
	    bots.update();
	    trees.update();
	    checkOnTrees();
	    
	    TreeInfo weakestTree = trees.getWeakestTreeWithinInteract(team);
	    if(weakestTree != null && weakestTree.health < GameConstants.BULLET_TREE_MAX_HEALTH / 2){
	    	this.moveTo(weakestTree.location);
	    	this.waterWeakest();
	    } else {
	    	followBuildCommands();
	    }
	    
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
    
    public boolean plantTreeAwayFromBase() throws GameActionException{
	    Direction tryPlantDir = dirToBase;
		for (int i = 0; i < 5; i++) {
			if (i != 2){ // not in direction away from base either
				tryPlantDir = tryPlantDir.rotateLeftDegrees((float) (360. / 6));
				if (this.plantTree(tryPlantDir)){ // if build successful, break and return true
					return true;
				}
			}
		}
		return false; // build not successful, so return false
    }
    
    public boolean plantTreeInFormation() throws GameActionException{
    	MapLocation nextLoc = this.treeTargets.get(0);
    	System.out.println(nextLoc);
    	if(plantTree(nextLoc)){
    		builtTrees.add(treeTargets.remove(0));
    		makeTree = false;
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean wanderAlongAisle() throws GameActionException{
    	Direction dir;
    	if (Math.random() > 5)
    		dir = dirToBase;
    	else
    		dir = dirToBase.opposite();
    	
    	if(!this.tryMove(dir)){
    		return this.tryMove(dir.opposite());
    	} else{
    		return true;
    	}
    }
    
    public void followBuildCommands() throws GameActionException{
    	Map<Codes, Integer> orders = radio.checkBuildOrders();
    	Codes[] botOrder = {Codes.TANK, Codes.LUMBERJACK, Codes.SOLIDER, Codes.SCOUT, Codes.TREE};
    	for(Codes code: botOrder){
    		if(orders.containsKey(code) && orders.get(code) > 0){
    			if (build(code)){
    				radio.reportBuild(code);
    				break;
    			}
    		}
    	}
    }
    
    public boolean build(Codes code) throws GameActionException{
    	if (code == Codes.TREE){
    		this.makeTree = true;
    		return this.plantTreeInFormation();
    	} else {
    		return build(code.getRobotType());
    	}
    }
    
    public boolean build(RobotType robotType) throws GameActionException{
	    Direction tryBuildDir = this.dirToBase.opposite();
	    return build(tryBuildDir, robotType);
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
        RobotInfo nearestEnemy = bots.getClosestbot(team.opponent());
        boolean flee = (nearestEnemy!=null);
        //if so, run away, notify soldier
        if (flee) {
            Direction fleeTo = nearestEnemy.location.directionTo(rc.getLocation());
            boolean moved = tryMove(fleeTo);
            if (moved) return true; //work in progress
        }
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
        if (weakest != null){
        	while(rc.canWater(weakest.location)) 
        		rc.water(weakest.location);
            return true;
        } else {
            return false;
        }
    }


	/**a function for planting a tree at a particular map location
	 * Return True if plant successful.
	 * 
	 * */
	public boolean plantTree(MapLocation plantSite) throws GameActionException{
		Direction dirToPlant = rc.getLocation().directionTo(plantSite);
	    if (canPlantLoc(plantSite)) {
	        rc.plantTree(dirToPlant);
	        return true;
        }
	    else { //can implement return from moveToPlant to determine whether or not moving there is possible at all
	    	System.out.println("moving to plant...");
	    	if(!moveToPlant(plantSite)){ // try to move around a little
	    		wander();
	    	}
			return false;
	    } 
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
		MapLocation[] result;
		if (intPoints == null){
			result = new MapLocation[1];
        } else {
        	result = new MapLocation[1 + intPoints.length];
	        MapLocation iP;
	        for(int i = 0; i < intPoints.length; i++){
	        	iP = intPoints[i];
	        	result[i+1] = iP.add(iP.directionTo(rc.getLocation()), rc.getType().bodyRadius);
	        }
	        return result;
        }
		result[0] = nearPoint;
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

		Direction directionToBuild = directionToEnemyArchon(rc.getLocation());
		if (rc.canBuildRobot(RobotType.SOLDIER, directionToBuild)) {
				rc.buildRobot(RobotType.SOLDIER, directionToBuild);
			}

		if (rc.canPlantTree(directionToBuild)) {
			rc.plantTree(directionToBuild);
		}

		float radiansToBuild = directionToBuild.getAngleDegrees() + ((float) Math.PI / 3);
		float step = ((float) Math.PI/ 3);
		for (float radians = radiansToBuild; radians < 2 * (float) Math.PI + radiansToBuild; radians = radians + step) {
			Direction directionToPlant = new Direction(radians);
			if (rc.canPlantTree(directionToPlant)) {
				rc.plantTree(directionToPlant);
			}
		}
	}
	
	public List<MapLocation> calculateTreeTargets(int N){
		MapLocation ccw, cw, loc, myLoc = rc.getLocation();
		cw = myLoc.add(dirToBase.rotateRightDegrees(90), 2+GameConstants.GENERAL_SPAWN_OFFSET);
		ccw = myLoc.add(dirToBase.rotateRightDegrees(-90), 2+GameConstants.GENERAL_SPAWN_OFFSET);
		List<MapLocation> result = new ArrayList<MapLocation>(N);
		for(int i = 0; i < N; i++){
			if (i % 2 == 0)
				loc = ccw;
			else
				loc = cw;
			result.add(loc.add(dirToBase,  -2 * (int) (i / 2)));
		}
		return result;
	}
	
	public List<MapLocation> calculateTreeTargets(){
		return calculateTreeTargets(10);
	}
	
	public void checkOnTrees() throws GameActionException{
		int i = 0;
		for(MapLocation loc: builtTrees){
			if(rc.canSenseLocation(loc) && rc.senseTreeAtLocation(loc) == null){
				this.treeTargets.add(0, builtTrees.remove(i));
				System.out.println("Lost a tree! " + loc);
			}
			i++;
		}
		i = 0;
		for(MapLocation loc: this.treeTargets){
			if(rc.canSenseLocation(loc) && rc.senseTreeAtLocation(loc) != null){
				System.out.println("Found a tree! " + loc);
				this.builtTrees.add(0, treeTargets.remove(i));
			}
			i++;
		}
	}

}

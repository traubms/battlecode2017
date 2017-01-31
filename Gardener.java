package battlecode2017;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gardener extends AbstractBot {
	
	MapLocation base;
	Direction preferedDirection;
	Direction dirToBase;
	boolean foundHome;
	Map<Direction, Boolean> canBuild;
	
	public Gardener(RobotController rc) throws GameActionException {
		super(rc);
		bots.update();
		for(RobotInfo bot : bots.getBots(team)){
			if (bot.getType() == RobotType.ARCHON){
				this.base = bot.location;
				this.dirToBase = rc.getLocation().directionTo(base);
				break;
			}
		}
		this.preferedDirection = BotUtils.randomDirection();
		this.foundHome = false;
	}

	public void run() throws GameActionException {
	    bots.update();
	    trees.update();
	    waterWeakest();
	    if(!foundHome){
	    	this.preferedDirection = BotUtils.randomDirection();
		    findHome();
	    }
	    followBuildCommands();
	    radio.reportEnemies(bots);
	    updateEdges();
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
    
    public List<Direction> getBuildDirections() throws GameActionException{
    	MapLocation center = rc.getLocation(), loc;
    	List<Direction> result = new ArrayList<Direction>();
    	Direction dir = this.preferedDirection;
    	for (int i = 0; i < 6; i++) {
    		loc = center.add(dir, 2.1f);
			if (rc.onTheMap(loc, 1) && !rc.isCircleOccupied(loc, 1))
				result.add(dir);
			dir = dir.rotateLeftDegrees((float) (360. / 6));
		}
    	return result;
    }

    public boolean plantTree() throws GameActionException{
    	List<Direction> buildDirs = getBuildDirections();
    	if (buildDirs.size() > 1){
			for (Direction tryPlantDir: buildDirs) {
				if (this.plantTree(tryPlantDir)){ // if build successful, break and return true
					return true;
				}
			}
    	}
		return false; // build not successful, so return false
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
    
    public void findHome() throws GameActionException{
    	int count = 0;
	    float minDist = 10000, minArchDist=1000, dist, strength;
	    float[] gradient = new float[2];
	    gradient = this.gradientFromEdges(gradient);
	    MapLocation myLoc = rc.getLocation();
	    for(RobotInfo bot: bots.getBots(team)){
	    	if (bot.type == RobotType.GARDENER || bot.type == RobotType.ARCHON){
	    		count++;
	    		dist = myLoc.distanceTo(bot.location);
	    		if (bot.type == RobotType.GARDENER && dist < minDist)
	    			minDist = dist;
	    		if (bot.type == RobotType.ARCHON && dist < minDist)
	    			minArchDist = dist;
	    	}
    		if (bot.type == RobotType.ARCHON)
    			strength = .1f;
    		else if (bot.type == RobotType.GARDENER)
    			strength = 1;
    		else
    			strength = .00f;
    		gradient = updateGradient(gradient, myLoc, bot.location, strength);	
	    }
	    float stopDist = 8.5f;
	    if (count == 0 || (minDist > stopDist && minArchDist > stopDist * 3 / 4)){
	    	List<Direction> bd = getBuildDirections();
	    	if (bd.size() < 3 && bd.size() > 0){
	    		this.build(bd.get(0), RobotType.LUMBERJACK);
	    		System.out.println("lumber...");
	    	}else 
	    		foundHome = true;
//	    		while(!build(RobotType.SOLDIER)){
//	    			foundHome = true;
//	    		}
	    } 
	    if (!foundHome){
	    	gradient = updateGradient(gradient, myLoc, base, 1f);
	    	followGradient(gradient);
	    }
    }
    
    public boolean build(Codes code) throws GameActionException{
    	if (code == Codes.TREE){
    		if(this.foundHome)
    			return this.plantTree();
    		else
    			return false;
    	} else {
    		return build(code.getRobotType());
    	}
    }
    
    public boolean build(RobotType robotType) throws GameActionException{
    	List<Direction> buildDirs = getBuildDirections();
		if (buildDirs.size() > 0){
			for (Direction tryBuildDir: buildDirs) {
				if (this.build(tryBuildDir, robotType)){ // if build successful, break and return true
					return true;
				}
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
}

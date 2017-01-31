package battlecode2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import battlecode.common.BodyInfo;
import battlecode.common.BulletInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import battlecode.common.GameConstants;

public abstract class AbstractBot {
	
	protected final float EPSILON = (float) 0.001;
	protected final float TREE_WATERING_THRESHOLD = (float) 45;

	protected RobotController rc;
	protected Team team;
	protected TreeReport trees;
	protected BotReport bots;
	protected Radio radio;
	protected boolean swarm;
	
	public AbstractBot(RobotController rc){
		this.rc = rc;
		this.team = rc.getTeam();
		this.trees = new TreeReport(rc);
		this.bots = new BotReport(rc);
		this.radio = new Radio(rc);
		this.swarm = false;
	}
	
	public abstract void run() throws GameActionException;
	
	
    
    public boolean followMarchingOrders() throws GameActionException{
    	MapLocation loc = radio.swarmLocation();
    	if (rc.getLocation().distanceTo(loc) < 5){
    		radio.reachedSwarmLocation(loc);
    	} if (!potentialMove(loc)) {
    		attackNeutralTrees();
    		return false;
    	} else
    		return true;
    }
    
    /** Move in random direction*/
    public boolean wander() throws GameActionException {
    	return moveAvoidingGardeners();
    }
    
 
    /**basic function for moving to a map location*/
    public boolean moveTo(MapLocation dest) throws GameActionException {
        float distTo = rc.getLocation().distanceTo(dest);
        Direction dirTo = rc.getLocation().directionTo(dest);

        if (distTo > rc.getType().strideRadius) {
            return tryMove(dirTo);
        } else {
            if (!rc.hasMoved() && rc.canMove(dest)) {
                rc.move(dest);
                return true;
            } else {
                return tryMove(dirTo);
                }
            }
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    public boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        move(dir);
        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the right side
            move(dir.rotateRightDegrees(degreeOffset*currentCheck));
            // Try the offset on the left side
            move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    
    public boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,7);
    }
      
    /** most fundamental movement funciton we've made*/
    public boolean move(Direction dir) throws GameActionException {
        if (!rc.hasMoved() && rc.canMove(dir) && !rc.hasAttacked()) {
            rc.move(dir);
            return true;
        } else return false;
    }

    
    public void moveTowardsEnemiesOrTrees() throws GameActionException{
		MapLocation goal = null;
		if (bots.getBotCounts(team.opponent()) > 0){ // move to opponent
			goal = bots.getClosestbot(team.opponent()).location;
		} else if (trees.getTreeCounts(team.opponent()) > 0){ // move to enemy tree
			goal = trees.getClosestTree(team.opponent()).location;
		} else if (trees.getTreeCounts(Team.NEUTRAL) > 0){ // move to neutral tree
			goal = trees.getClosestTree(Team.NEUTRAL).location;
		} 
		
		if (goal == null) {
			wander();
		} else if(!rc.getLocation().isWithinDistance(goal, (float) 2.4)){
			this.tryMove(rc.getLocation().directionTo(goal));
		}
    }
    
    public void moveTowardsOrWander(MapLocation goal, float tol) throws GameActionException{
    	if (goal == null) {
			wander();
		} else if(!rc.getLocation().isWithinDistance(goal, tol)){
			this.moveTo(goal);
		}
    }
    
    public void moveTowardsOrWander(MapLocation goal) throws GameActionException{
    	moveTowardsOrWander(goal, this.EPSILON);
    }
    
    boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        //MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        //MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
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
                    && rc.getLocation().distanceTo(target.getLocation()) < (5 + rc.getType().bodyRadius)) {
                tryMove(directionToTarget.rotateLeftDegrees(180));
            } else tryMove(directionToTarget);

            fireShot(target); // SHOOT
            return true;
        }
    }
    
    public Direction fireShot(BodyInfo target) throws GameActionException{
    	MapLocation myLocation = rc.getLocation(), loc = target.getLocation();
    	Direction directionToShoot = myLocation.directionTo(loc);
    	float distToEnemy = rc.getLocation().distanceTo(loc);
    	

        //setting appropriate ranges based on type of enemy to reduce wasted bullets and friendly fire
        //tweak the hardcoded numbers as appropriate
        float pentadRange = (float) 2 + rc.getType().bodyRadius;
        float triadRange = (float) 3 + rc.getType().bodyRadius;
        float singleRange = (float) 5 + rc.getType().bodyRadius;

        //if swarming the enemy, just go ahead and blast away.
        if (true)  { //messy change
            pentadRange = pentadRange + 5;
            triadRange = triadRange + 4;
            singleRange = singleRange + 3;
        }

        if (target.isRobot()){
	        if (((RobotInfo)target).getType().equals(RobotType.ARCHON) || ((RobotInfo)target).getType().equals(RobotType.TANK)) {
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
            } else if (rc.getLocation().distanceTo(nearestBadTree.location) > rc.getLocation().distanceTo(nearestNTree.location)){
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
        else if (nearestBadTree== null)
        	dontWorryAboutTrees = false;
        else 
        	dontWorryAboutTrees = myLocation.distanceTo(nearestBadTree.location) < (float) 2.5 && directionDifference*directionDifference < (float) 50*50;
        
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
            if (distanceToFriendly < distToEnemy && degreesBetweenFriendly*degreesBetweenFriendly < 50*50) {
                single = false;
                triad = false;
                pentad = false;
                break;
            }
            if (count >= 6) break;
        }



        if (single || triad || pentad){
	        if (pentad && (distToEnemy < pentadRange || dontWorryAboutTrees)) {// pentad close enough
	            rc.firePentadShot(directionToShoot);
	        } else if (triad && (distToEnemy < triadRange|| dontWorryAboutTrees)) {
	            rc.fireTriadShot(directionToShoot);
	        } else if (distToEnemy < singleRange) {
	            rc.fireSingleShot(directionToShoot);
	        } else {
	        	return null;
	        }
        	return directionToShoot;
        } else
        	return null;
    }
    
    public boolean attackNeutralTrees() throws GameActionException{
//    	return false;
    	List<TreeInfo> neutralTrees = trees.getTreesWithinInteract(Team.NEUTRAL);
		if (neutralTrees.size() > 0 && rc.getTeamBullets() > 100) {
			return fireShot(neutralTrees.get(0)) != null;
		} else 
			return false;
    }
    
    
    public float[] initializeGradient(){
    	return new float[2];
    }
    
    public boolean followGradient(float[] gradient) throws GameActionException{
		Direction moveDir = new Direction((float) Math.atan2(gradient[1], gradient[0]));
		if(!tryMove(moveDir)){
			if(!tryMove(moveDir.rotateLeftDegrees(90))){
				if(!tryMove(moveDir.rotateRightDegrees(90)))
					return false;
			}
		}
		return true;
	}
	
    public float[] updateGradient(float[] gradient, MapLocation myLoc, MapLocation loc, float strength){
		float dist = myLoc.distanceTo(loc);
		gradient[0] += strength * (myLoc.x - loc.x) / (dist*dist*dist);
		gradient[1] += strength * (myLoc.y - loc.y) / (dist*dist*dist);
		return gradient;
	}
	
	public boolean moveAvoidingGardeners() throws GameActionException{
		int count = 0;
	    float[] gradient = initializeGradient();
	    MapLocation myLoc = rc.getLocation();
	    for(RobotInfo bot: bots.getBots(team)){
    		if (bot.type == RobotType.ARCHON){
    			gradient = updateGradient(gradient, myLoc, bot.location, 1);	
    			count++;
    		} else if((bot.type == RobotType.GARDENER || bot.type == RobotType.SCOUT) && myLoc.distanceTo(bot.location) < 5){
    			gradient = updateGradient(gradient, myLoc, bot.location, 1);	
    			count++;
    		}
	    }
	    if(count > 0)
	    	return followGradient(gradient);
	    else {
	        Direction dir = BotUtils.randomDirection();
	        return tryMove(dir);
	    }
	}
	
	public boolean avoidEnemies(float range) throws GameActionException {
		int count = 0;
	    float[] gradient = initializeGradient();
	    MapLocation myLoc = rc.getLocation();
	    float dist;
//	    boolean canAttack;
	    for(RobotInfo bot: bots.getBots(team.opponent())){
	    	dist = myLoc.distanceTo(bot.location);
//	    	canAttack = bot.type == RobotType.SCOUT || bot.type == RobotType.SOLDIER || bot.type == RobotType.TANK || bot.type == RobotType.LUMBERJACK;
    		if (dist < range){
    			gradient = updateGradient(gradient, myLoc, bot.location, 1);	
    			count++;
    		}
	    }
	    if(count > 0)
	    	return followGradient(gradient);
	    else 
	    	return false;
	}
	
	public boolean avoidEnemies() throws GameActionException {
		return avoidEnemies(1000);
	}
	
	public boolean potentialMove(MapLocation goal) throws GameActionException{
		MapLocation myLoc = rc.getLocation();
		float[] gradient = initializeGradient();
	    gradient = updateGradient(gradient, myLoc, goal, -10000);
	    for(RobotInfo bot: bots.getBots(team)){
    		if (bot.type == RobotType.ARCHON){
    			gradient = updateGradient(gradient, myLoc, bot.location, 1);	
    		}
	    }
	    return followGradient(gradient);
	}
	
	public void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }
    }

    public boolean inEnemyTerritory() throws GameActionException {
        return rc.getLocation().distanceTo(rc.getInitialArchonLocations(team.opponent())[0])
                < rc.getLocation().distanceTo(rc.getInitialArchonLocations(team)[0]);
    }

    public boolean shake() throws GameActionException {
    	ArrayList<TreeInfo> bullets = trees.getBulletTrees();
    	if (bullets.size() > 0 && rc.canShake(bullets.get(0).ID)){
    		rc.shake(bullets.get(0).ID);
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void donateBulletsToWin() throws GameActionException {
        float round = rc.getRoundNum();
        float numBullets = rc.getTeamBullets();
        float conversion = (7.5f) + ((round * 12.5f) / 3000f);
        if (numBullets / conversion > 1000 - rc.getTeamVictoryPoints()) {
            rc.donate(numBullets);
        }
    }
    
    public void incrementalDonate() throws GameActionException {
        float round = rc.getRoundNum();
        float numBullets = rc.getTeamBullets();
        float conversion =  (7.5f) + ((round * 12.5f) / 3000f);
        float bulletsNeededFor50VP = 50 * conversion + 1;
        if (numBullets > 1.5 * bulletsNeededFor50VP) {
            rc.donate(bulletsNeededFor50VP);
        }
    }
    
    public MapLocation nearestEnemyBotOrTree(){
    	if (bots.getBotCounts(team.opponent()) > 0){ // move to opponent
			return bots.getClosestbot(team.opponent()).location;
		} else if (trees.getTreeCounts(team.opponent()) > 0){ // move to enemy tree
			return trees.getClosestTree(team.opponent()).location;
		} else {
			return null;
		} 
    }

    public TreeInfo nearestEnemyTreeOrNeutralTree() {
        TreeInfo nearestTree = null;
        if (trees.getTreeCounts(Team.NEUTRAL) > 0) {
            nearestTree = trees.getClosestTree(Team.NEUTRAL);
        }
        if (trees.getTreeCounts(team.opponent()) > 0) {
            TreeInfo nearestBTree = trees.getClosestTree(team.opponent());
            if (nearestTree == null || rc.getLocation().distanceTo(nearestBTree.getLocation()) < rc.getLocation().distanceTo(nearestTree.getLocation())) {
                nearestTree = nearestBTree;
            }
        }
        if (nearestTree!=null) return nearestTree;
        else return null;

    }
    
    public MapLocation nearestEnemyBotOrTreeOrNeutralTree(){
    	MapLocation enemy = nearestEnemyBotOrTree();
    	if (enemy == null){
    		if(trees.getTreeCounts(Team.NEUTRAL) > 0)
    			return trees.getClosestTree(Team.NEUTRAL).location;
    	} 
    	return null; // no enemy or neutral tree
    }
    /**with preference for the furthest bullet tree*/
    public MapLocation nearestEnemyBotOrTreeOrBulletTree(){
        ArrayList<TreeInfo> bulletTrees = trees.getBulletTrees();
        if(bulletTrees.size() > 0) {
            return bulletTrees.get(bulletTrees.size()-1).location;
        }
        else return nearestEnemyBotOrTree(); //could be null
    }
    	
    public boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI / 2) {
            return false;
        }
        
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    /**
     * Returns the initial direction to the enemy archons
     * @param mapLocation Location of robot
     * @return Direction to the nearest enemy archon
     * @throws GameActionException
     */
    public Direction directionToEnemyArchon(MapLocation mapLocation) throws GameActionException {
        MapLocation[] locationsOfEnemyArchons = rc.getInitialArchonLocations(team.opponent());
        Direction directionToEnemyArchon = mapLocation.directionTo(locationsOfEnemyArchons[0]);
        return directionToEnemyArchon;
    }
    
}

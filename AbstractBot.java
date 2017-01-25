package battlecode2017;

import java.util.ArrayList;
import java.util.List;

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
	
	public AbstractBot(RobotController rc){
		this.rc = rc;
		this.team = rc.getTeam();
		this.trees = new TreeReport(rc);
		this.bots = new BotReport(rc);
		this.radio = new Radio(rc);
	}
	
	public abstract void run() throws GameActionException;
	
    /** Move in random direction*/
    public boolean wander() throws GameActionException {
        Direction dir = BotUtils.randomDirection();
        return tryMove(dir);
    }

    public boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,7);
    }
    

    /**basic function for moving to a map location*/
    public boolean moveTo(MapLocation dest) throws GameActionException {
        float distTo = rc.getLocation().distanceTo(dest);
        Direction dirTo = rc.getLocation().directionTo(dest);

        if (distTo > rc.getType().strideRadius) {
            return tryMove(dirTo);
        } else {
            if (rc.canMove(dest)) {
                rc.move(dest);
                return true;
            } else {
                return tryMove(dirTo);
                }
            }
    }

    public void donateBullets3() throws GameActionException {
        if (rc.getTeamBullets() > 1000) {
            rc.donate(rc.getTeamBullets());
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
        if (!rc.hasMoved() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(! rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
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
    
    
    public MapLocation nearestEnemyBotOrTree(){
    	if (bots.getBotCounts(team.opponent()) > 0){ // move to opponent
			return bots.getClosestbot(team.opponent()).location;
		} else if (trees.getTreeCounts(team.opponent()) > 0){ // move to enemy tree
			return trees.getClosestTree(team.opponent()).location;
		} else {
			return null;
		} 
    }
    
    public MapLocation nearestEnemyBotOrTreeOrNeutralTree(){
    	MapLocation enemy = nearestEnemyBotOrTree();
    	if (enemy == null){
    		if(trees.getTreeCounts(Team.NEUTRAL) > 0)
    			return trees.getClosestTree(Team.NEUTRAL).location;
    	} 
    	return null; // no enemy or neutral tree
    }
    
    public MapLocation nearestEnemyBotOrTreeOrBulletTree(){
    	MapLocation enemy = nearestEnemyBotOrTree();
    	if (enemy == null){
    		ArrayList<TreeInfo> bulletTrees = trees.getBulletTrees();
    		if(bulletTrees.size() > 0)
    			return bulletTrees.get(0).location;
    	} 
    	return null; // no enemy or bullet tree
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
    
    boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    public void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }
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



    /**
     * Attacks by shooting either a single, triad, or pentad shot.
     * Only called by soldiers, scouts, and tanks.
     *
     * @throws GameActionException
     */

    public void attack() throws GameActionException {

        MapLocation myLocation = rc.getLocation();
        //System.out.println(bots.getBotCounts(team.opponent()));

        if (bots.getBotCounts(team.opponent()) > 0) {
            boolean single = rc.canFireSingleShot();
            boolean triad = rc.canFireTriadShot();
            boolean pentad = rc.canFirePentadShot();
            RobotInfo closestEnemy = bots.getClosestbot(team.opponent());

            Direction directionToMove = myLocation.directionTo(closestEnemy.location);
            if (rc.canMove(directionToMove)) {
                tryMove(directionToMove, 10, 2);
            }

            if (single || triad || pentad) {
                Direction directionToShoot = myLocation.directionTo(closestEnemy.location);

                //setting appropriate ranges based on type of enemy to reduce wasted bullets and friendly fire
                //tweak the hardcoded numbers as appropriate
                float pentadRange = (float) 0.766 + rc.getType().bodyRadius;
                float triadRange = (float) 1.074 + rc.getType().bodyRadius;
                float singleRange = (float) 1.8 + rc.getType().bodyRadius;
                if (closestEnemy.getType().equals(RobotType.ARCHON) || closestEnemy.getType().equals(RobotType.TANK)) {
                    pentadRange = pentadRange + (float) 1;
                    triadRange = triadRange + (float) 1.3;
                    singleRange = singleRange + (float) 2.5;
                }
                float distToEnemy = rc.getLocation().distanceTo(closestEnemy.location);

                if (rc.canFirePentadShot() && distToEnemy < pentadRange)  //TODO take into account that friendlies might be in the way?
                    rc.firePentadShot(directionToShoot);
                else if (rc.canFireTriadShot() && distToEnemy < triadRange)
                    rc.fireTriadShot(directionToShoot);
                else if (distToEnemy < singleRange)
                    rc.fireSingleShot(directionToShoot);
            }
        }
        else {
            RobotInfo inDanger = bots.getWeakestbot(team);
            if (!inDanger.equals(null)) tryMove(rc.getLocation().directionTo(inDanger.location));


        }
    }
    

    public void donateBullets2() throws GameActionException {
        float round = rc.getRoundNum();
        float numBullets = rc.getTeamBullets();
        float conversion = (7.5f) + ((round * 12.5f) / 3000f);

        if (numBullets / conversion > 1000) {
            rc.donate(numBullets);
        }
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

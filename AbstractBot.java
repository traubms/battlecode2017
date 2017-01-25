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
    
    /**
     * Exchanges 10% of bullets for victory points every 100 rounds. If at the last round, donates all the bullets. 
     * @throws GameActionException
     */
    public void donateBullets() throws GameActionException{
        int round = rc.getRoundNum(); 
        if (round >= rc.getRoundLimit() - 1) {
            rc.donate(rc.getTeamBullets());
        }
        else {
            if (round == 1 || round % 100 == 0) {
                rc.donate((float) (0.1*rc.getTeamBullets()));
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
                rc.move(directionToMove, .5f);
            }

            if (single || triad || pentad) {
                Direction directionToShoot = myLocation.directionTo(closestEnemy.location);
                if (rc.canFirePentadShot())
                    rc.firePentadShot(directionToShoot);
                else if (rc.canFireTriadShot())
                    rc.fireTriadShot(directionToShoot);
                else
                    rc.fireSingleShot(directionToShoot);
            }
        }
        else {
            List<RobotInfo> teamBots = bots.getBots(rc.getTeam());
            if (teamBots.size() > 0) {
                Direction directionToFriend = myLocation.directionTo(teamBots.get(0).location);
                if (rc.canMove(directionToFriend)) {
                    tryMove(directionToFriend);
                }
            }
        }
    }
}

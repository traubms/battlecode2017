package battlecode2017;

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
	
	/**random direction */
    static Direction randomDirection() throws GameActionException {
        float k = ((float) Math.random() * (float) 2 * (float) Math.PI) - (float) Math.PI;
        return new Direction(k);
    }

    /** Move in random direction*/
    public boolean wander() throws GameActionException {
        Direction dir = randomDirection();
        return tryMove(dir);
    }
    


    /**find intersection between two circles
     *
     * @param center0 center of first circle
     * @param center1 cneter of second circle
     * @param radius0 radius of 1st circle
     * @param radius1 radius of 2nd circle
     *
     * @return MapLocation[] of the intersection point(s) or null if they don't exist
     *
     * @throws GameActionException
     */
    public MapLocation[] findCircleIntersections(MapLocation center0, MapLocation center1, float radius0, float radius1) throws GameActionException {
        float d = center0.distanceTo(center1);
        if (d > radius0+radius1) return null;
        if (d == radius0+radius1) {
            MapLocation[] onePoint = {center0.add(center0.directionTo(center1), radius0)};
            return onePoint;
        }
        if (center0.equals(center1)) return null;
        else {
            float a = (radius0*radius0 -radius1*radius1 +d*d) / (2*d);
            float h = (float) Math.sqrt((double) radius0*radius0 - a*a);
            MapLocation p2 = center0.add(center0.directionTo(center1), radius0);
            float x3_1 = p2.x + h * (center1.y - center0.y) / d;
            float y3_1 = p2.y - h * (center1.x - center0.x) / d;
            float x3_2 = p2.x - h * (center1.y - center0.y) / d;
            float y3_2 = p2.y + h * (center1.x - center0.x) / d;
            MapLocation[] twoPoints = {new MapLocation(x3_1, y3_1), new MapLocation(x3_2, y3_2)};
            return twoPoints;
        }
    }





    public boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,7);
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

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

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
}

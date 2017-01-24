package battlecode2017.ihatethis;


import battlecode.common.*;
public class RobotPlayer {




        static RobotController rc;


        public static void run(RobotController rc) throws GameActionException {

            RobotPlayer.rc = rc;

            switch (rc.getType()) {
                case ARCHON:
                    runArchon();
                    break;
                case GARDENER:
                    runGardener();
                    break;
                case SOLDIER:
                    runSoldier();
                    break;
                case LUMBERJACK:
                    runLumberjack();
                    break;
                case SCOUT:
                    runScout();
                    break;
                case TANK:
                    runTank();
                    break;

            }
        }
    static void runGardener() throws GameActionException {
            while (true) {
                try{
                    if (rc.getTeamBullets() >= 10000) { //Ready to win!
                        rc.donate(10000);
                    }
                    maybeLumberJack();
                        if (canSenseArchon()) {

                            tryMove(dirArchon(), 20, 6);
                        }
                            treeItUp();
                            Direction[] treeDirs = TriComb();
                            randWater(treeDirs);




                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
        static void runArchon() throws GameActionException {
            while (true) {
                try {
                    wander();

                    if (rc.getTeamBullets() >= 10000) { //Ready to win!
                        rc.donate(10000);
                        Clock.yield();
                    } else {
                        Direction rdir = randomDirection(); //build gardener in random direction
                        if (rc.canHireGardener(rdir)
                                && Math.random() <= 1.0) {
                            rc.hireGardener(rdir);
                            Clock.yield();
                        }
                        Clock.yield();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Clock.yield();
                }
            }

        }
    static void runSoldier() throws GameActionException {
        while (true) {
            try {
                rc.getTeam();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    static void runLumberjack() throws GameActionException {
        while (true) {
            try {
                seekAndDestroy();
                TreeInfo[] nti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
                if (nti.length > 0) {
                    int nearestTreeID = nti[0].ID;
                    if (rc.canChop(nearestTreeID)) {
                        rc.chop(nearestTreeID);
                        Clock.yield();
                    } else {
                        tryMove(rc.getLocation().directionTo(nti[0].location));
                        }

                } else { wander();}

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runScout() throws GameActionException {
        while (true) {
            try {
                rc.getRoundNum();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runTank() throws GameActionException {
        while (true) {
            try {
                rc.getType();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**random direction */
    static Direction randomDirection() throws GameActionException {
            float k = ((float) Math.random() * (float) 2 * (float) Math.PI) - (float) Math.PI;
        return new Direction(k);

    /**wander about aimlessly like me*/
    }public static boolean wander() throws GameActionException {
        Direction dir = randomDirection();
        return tryMove(dir);
    }
    public static Direction[] TriComb() throws GameActionException {
        Direction[] triangle = new Direction[]{new Direction(0), new Direction((float) (2.0 * Math.PI / 3.0 )), new Direction((float) (-2.0 * Math.PI / 3.0 ))};
        return triangle;
    }
    /**tree it upppppppppppppppp*/
    public static void treeItUp() throws GameActionException {

        Direction[] treeSpots = TriComb();
        //build the first tree if possible
        for (Direction d: treeSpots) {
        if (rc.canPlantTree(d)){
        rc.plantTree(d);
        System.out.println("planted tree.");
        }}}


        static Direction dirArchon() throws GameActionException {
        RobotInfo[] nearBots = rc.senseNearbyRobots();
        for (RobotInfo r : nearBots) {
            if (r.getType().equals(RobotType.ARCHON)) {
                return r.getLocation().directionTo(rc.getLocation());
            }
        }
        return randomDirection();
    }

    static boolean canSenseArchon() throws GameActionException {
        boolean canSense = false;
        RobotInfo[] nearBots = rc.senseNearbyRobots();
        for (RobotInfo r : nearBots) {
            if (r.getType().equals(RobotType.ARCHON)) {
                canSense = true;
                break;
            }
        }
        return canSense;
    }
    static boolean tryMove(Direction dir) throws GameActionException {
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
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

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

   /** random tree waterer
     * @param treeDirs, an array of directions (any length) in which trees are likely to exist
     * @throws GameActionException
     */
    public static void randWater(Direction[] treeDirs) throws GameActionException {
        TreeInfo[] ti = rc.senseNearbyTrees();
        int k = Math.min(ti.length, treeDirs.length);
        int j = (int) (Math.random() * k);
        MapLocation treeLoc = ti[j].getLocation();
        if (rc.canWater(treeLoc)) {
            rc.water(treeLoc);
        }

    }

    /** maybe make a lumberjack
     *
     */
    public static void maybeLumberJack() throws GameActionException {
        TreeInfo[] nti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        if (nti.length > 0) {
        Direction rdir = randomDirection();
        RobotInfo[] teammates = rc.senseNearbyRobots(-1, rc.getTeam());
        int lumCount = 0;
        for (RobotInfo t : teammates) {
            if (t.getType().equals(RobotType.LUMBERJACK)) {
                lumCount++;
            }
        }
        if (rc.canBuildRobot(RobotType.LUMBERJACK, rdir) && lumCount < 5) {
            rc.buildRobot(RobotType.LUMBERJACK, rdir);
            Clock.yield();
        }
    }}
    /**attack priority for lumberjacks*/
    public static void seekAndDestroy() throws GameActionException {
        dodge();
        RobotInfo[] bots = rc.senseNearbyRobots();
        for (RobotInfo b : bots) {
            if (b.getTeam() != rc.getTeam() && rc.canStrike()) {
                rc.strike();
                Direction chase = rc.getLocation().directionTo(b.getLocation());
                tryMove(chase);
                break;
            }
        }
    }

    static boolean willCollideWithMe(BulletInfo bullet) {
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
    static boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }

    }
}

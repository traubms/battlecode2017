package battlecode2017;

import battlecode.common.RobotController;

public class Soldier extends AbstractBot {

	public Soldier(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

	public void run() {
		System.out.println("soldier");
	}

	public void runSoldier() throws GameActionException {
		while (true) {
			try {
				RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
				MapLocation myLocation = rc.getLocation();

				if (robotInfo.length > 0) {
					boolean single = rc.canFireSingleShot();
					boolean triad = rc.canFireTriadShot();
					boolean pentad = rc.canFirePentadShot();

					if (single || triad || pentad) {
						Direction directionToShoot = myLocation.directionTo(robotInfo[0].location);
						if (rc.canFirePentadShot())
							rc.firePentadShot(directionToShoot);
						else if (rc.canFireTriadShot())
							rc.fireTriadShot(directionToShoot);
						else
							rc.fireSingleShot(directionToShoot);
					}

					Direction directionToMove = myLocation.directionTo(robotInfo[0].location);
					if (rc.canMove(directionToMove)) {
						rc.move(directionToMove);
					}
				}
				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

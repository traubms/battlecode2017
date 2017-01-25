package battlecode2017;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends AbstractBot {

	public Soldier(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

	public void run() throws GameActionException {
		bots.update();
		dodge();
		MapLocation myLocation = rc.getLocation();
		System.out.println(bots.getBotCounts(team.opponent()));

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
	}
}

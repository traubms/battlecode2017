package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Archon extends AbstractBot {

	public Archon(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

    public void run() throws GameActionException {
	    trees.update();
    	tryPlantGardener();
    	radio.addToBuildQueue(Codes.LUMBERJACK);
    }


	/** Trys to plant a gardener around an Archon by checking different
	 * possible planting locations around it. Only plants every 50 turns
	 *
	 * @throws GameActionException
	 */
	public void tryPlantGardener() throws GameActionException {
		int degree = 0;
		int round = rc.getRoundNum();
		while (degree < 360) {
			float radian = ((float) degree * (float) Math.PI) / 180;
			Direction direction = new Direction(radian);
			if (rc.canHireGardener(direction) && (round == 1 || round % 60 == 0)) {
				rc.hireGardener(direction);
			}
			degree += 60;
		}
	}
}

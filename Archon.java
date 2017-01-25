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

    public void run() {
	    trees.update();
    }
	/**build Gardener in given direction d
	 * @param d : direction in which to place thing
	 *          @return boolean as to whether or not it built one
	 *          @throws battlecode.common.GameActionException
	 */
	public boolean buildGardener(Direction d) throws GameActionException{
		if (rc.canBuildRobot(RobotType.GARDENER, d)){
			rc.buildRobot(RobotType.GARDENER, d);
			return true;
		} else {return false;}
	}

	/** Trys to plant a gardener around an Archon by checking different
	 * possible planting locations around it. Only plants every 50 turns
	 *
	 * @param round The current round of the game
	 * @throws GameActionException
	 */
	public void tryPlantGardener(int round) throws GameActionException {
		int degree = 0;
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

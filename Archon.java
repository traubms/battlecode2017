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

}

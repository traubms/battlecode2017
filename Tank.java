package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Tank extends AbstractBot {

	public Tank(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}

	public void run() throws GameActionException {
		dodge();
	}

}

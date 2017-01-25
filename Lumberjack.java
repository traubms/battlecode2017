package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Lumberjack extends AbstractBot {

	public Lumberjack(RobotController rc) {
		super(rc);
	}

	public void run() throws GameActionException {
		trees.update();
		dodge();
		TreeInfo closestNeutralTree = trees.getClosestTree(Team.NEUTRAL);
		TreeInfo closestEnemyTree = trees.getClosestTree(team.opponent());
		
    }
}

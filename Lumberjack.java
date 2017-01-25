package battlecode2017;

import java.util.List;

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
		List<TreeInfo> enemyCanInteract = trees.getTreesWithinInteract(team.opponent());
		List<TreeInfo> neutralCanInteract = trees.getTreesWithinInteract(team.NEUTRAL);
		List<TreeInfo> mineCanInteract = trees.getTreesWithinInteract(team);

		boolean chop = true;
		if (mineCanInteract.size() > 0){ // if any own trees around, don't attack all
			chop = true;
    }
	
	
}

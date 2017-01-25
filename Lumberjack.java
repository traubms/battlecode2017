package battlecode2017;

import java.util.List;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Lumberjack extends AbstractBot {

	public Lumberjack(RobotController rc) {
		super(rc);
	}

	public void run() throws GameActionException {
		trees.update();
		bots.update();
		dodge();
		strikeOrChop();
		moveTowardsOrWander(nearestEnemyBotOrTreeOrNeutralTree());
    }
	
	public void strike() throws GameActionException{
		if(rc.canStrike()){
			rc.strike();
		}
	}
	
	public void strikeOrChop() throws GameActionException{
		boolean shouldStrike;
		List<TreeInfo> enemyCanInteract = trees.getTreesWithinInteract(team.opponent());
		List<TreeInfo> neutralCanInteract = trees.getTreesWithinInteract(team.NEUTRAL);
		List<TreeInfo> mineCanInteract = trees.getTreesWithinInteract(team);
		int enemiesCount = bots.getBotCounts(team.opponent());
		int enemiesTreeCount = enemyCanInteract.size();
		int neutralTreeCount = neutralCanInteract.size();
		
		if (mineCanInteract.size() > 0 || bots.getBotCounts(this.team) > 0){ // if any own trees around, don't strike
			shouldStrike = false;
		} else if (enemiesCount > 0){ // if enemies around, attack
			shouldStrike = true;
		} else if (enemiesTreeCount + neutralTreeCount > 2){ // more cumulative damage done by strike
			shouldStrike = true;
		} else {
			shouldStrike = false;
		}
		
		// execute strike or chop
		if (shouldStrike){
			strike();
		} else if (enemiesTreeCount > 0){
			chop(enemyCanInteract.get(0).ID);
		} else if (neutralTreeCount > 0){
			chop(neutralCanInteract.get(0).ID);
		} 
	}
	public void chop(int id) throws GameActionException{
		if (rc.canChop(id)){
			rc.chop(id);
		}
	}
	
	
	
}

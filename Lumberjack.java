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
		
		List<TreeInfo> enemyCanInteract = trees.getTreesWithinInteract(team.opponent());
		List<TreeInfo> neutralCanInteract = trees.getTreesWithinInteract(team.NEUTRAL);
		List<TreeInfo> mineCanInteract = trees.getTreesWithinInteract(team);
		int enemiesCount = bots.getBotCounts(team.opponent());
		int enemiesTreeCount = enemyCanInteract.size();
		int neutralTreeCount = neutralCanInteract.size();

		// choosing strike or chop logic
		boolean shouldStrike;
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
		
		// Move
		MapLocation goal = null;
		if (enemiesCount > 0){ // move to opponent
			goal = bots.getClosestbot(team.opponent()).location;
		} else if (enemiesTreeCount > 0){ // move to enemy tree
			goal = trees.getClosestTree(team.opponent()).location;
		} else if (neutralTreeCount > 0){ // move to neutral tree
			goal = trees.getClosestTree(Team.NEUTRAL).location;
		} 
		
		
		if (goal == null) {
			wander();
		} else if(!rc.getLocation().isWithinDistance(goal, (float) 2.4)){
			this.tryMove(rc.getLocation().directionTo(goal));
		}
    }
	
	public void strike() throws GameActionException{
		if(rc.canStrike()){
			rc.strike();
		}
	}
	
	public void chop(int id) throws GameActionException{
		if (rc.canChop(id)){
			rc.chop(id);
		}
	}
	
	
}

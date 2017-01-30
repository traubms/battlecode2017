package battlecode2017;

import java.util.List;

import battlecode.common.*;

public class Lumberjack extends AbstractBot {

	public Lumberjack(RobotController rc) {
		super(rc);
	}

	public void run() throws GameActionException {
		trees.update();
		bots.update();
		shake();
		dodge();
		chaseAndStrikeEnemies();
		boolean sOrC = strikeOrChop();
//		if (radio.forwardMarch())
//			swarm = true;
		if (swarm)
			followMarchingOrders(); 
		else {
			if(!sOrC)
				moveTowardsOrWander(nearestEnemyBotOrTreeOrNeutralTree());
		}
    }
	
	public void strike() throws GameActionException{
		if(rc.canStrike()){
			rc.strike();
		}
	}

	/**This function has the lumberjack strike an enemy if it's in range or otherwise
	 *  move towards the nearest enemy if it is the closest friendly lumberjack
	 *  or otherwise make sure it won't get hit by the nearest friendly lumberjack.
	 *  This function will hit friendly bots!!!! I didn't bother fixing that and also thought it was worth it.
	 *  This function may not be perfect, but it will likely do decently well.
	 * @return true if we moved or struck, false otherwise
	 * @throws GameActionException
	 */
	public boolean chaseAndStrikeEnemies() throws GameActionException {
		//Get info I'll need
		RobotInfo nearestEBot = bots.getClosestbot(team.opponent());
		List<RobotInfo> FBots = bots.getBots(team);

		//if we found an enemy bot
		if (nearestEBot!=null) {
			//check if any friendly lumberjacks are closer
			boolean closerLumb = false;
			RobotInfo aforementionedLumb = null;
			for (RobotInfo ri: FBots) {
				if (ri.getType().equals(RobotType.LUMBERJACK)
						&& ri.location.distanceTo(nearestEBot.location)
						< rc.getLocation().distanceTo(nearestEBot.location)) {
					closerLumb = true;
					aforementionedLumb = ri;
					break;
				}
			}
			//if there are no closer lumberjacks, go in for the kill
			if (!closerLumb) {
				if (rc.getLocation().distanceTo(nearestEBot.location) - nearestEBot.getType().bodyRadius < GameConstants.LUMBERJACK_STRIKE_RADIUS) {
					//CAN strike friendlies!! lumberjacks know to avoid, but other friendlies do not, word of warning
					strike();
					return true;
				} else {
					return moveTo(nearestEBot.location);
				}
			}
			//if there is a closer lumberjack, make sure you keep your distance so you don't get chopped yourself
			else {
				//calculate where our closer lumberjack is gonna go
				MapLocation attackSpot = nearestEBot.location.add(nearestEBot.location.directionTo(aforementionedLumb.location), GameConstants.LUMBERJACK_STRIKE_RADIUS);
				//make sure we're out of the strike radius from that point. if not, get away further.
				if (rc.getLocation().distanceTo(attackSpot) - rc.getType().bodyRadius <= GameConstants.LUMBERJACK_STRIKE_RADIUS) {
					return tryMove(rc.getLocation().directionTo(attackSpot).rotateLeftDegrees((float) 180));
				} else return false; //If we're out of the way, just return false
			}
		}
		return false; //didn't see anyone
	}

	public boolean strikeOrChop() throws GameActionException{
		//save some time in case already did something
		if (!rc.canStrike()) return false;

		//carry on
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
		return shouldStrike;
	}
	public void chop(int id) throws GameActionException{
		if (rc.canChop(id)){
			rc.chop(id);
		}

	}

	
	
	
}

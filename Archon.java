package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Direction;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Archon extends AbstractBot {
    
	public Archon(RobotController rc) {
		super(rc);
	}

	/*
	 * Runs the Archon. Stays in one place. Builds gardeners.
	 * Maybe have the Archon dodge?
	 * If it senses too many trees nearby, build Gardeners that can build Lumberjacks.
	 * If it senses nearby bullet trees, build Gardeners that build Scouts to shake the trees. 
	 * 
	 */
    public void run() throws GameActionException {
	    trees.update();
    	bots.update();
		
    	if(trees.getTreeCounts(Team.NEUTRAL) > 3) { // If there's too many trees nearby w/in radius, build lumberjack
    	    hireGardener();  
    	    radio.addToBuildQueue(Codes.LUMBERJACK);
    	} 
    	if(trees.getTreeCounts(this.team) < 8) { // If there's too many trees nearby w/in radius, build lumberjack
    	    hireGardener();  
    	    radio.addToBuildQueue(Codes.TREE);
    	} 
    	
    	if(this.trees.getBulletTrees().size() > 0) { // If there are bullet trees, build gardeners to make scouts. 
    	    RobotInfo closestScout = findClosestRobotOfType(RobotType.SCOUT); 
    	    if(closestScout == null) {
    	        hireGardener();
    	        radio.addToBuildQueue(Codes.SCOUT);
    	    } else {
    	        // do nothing? 
    	    }
    	}
    	hireGardener(); 
    	radio.addToBuildQueue(Codes.SOLIDER);
    	//donateBullets();
   }
    
    public RobotInfo findClosestRobotOfType(RobotType type) {
        for(RobotInfo r : this.bots.getBots(rc.getTeam())) {
            if(r.getType() == type) {
                return r; 
            }
        }
        return null; //TODO: Change this null statement
    }
    
	/** Trys to plant a gardener around an Archon by checking different
	 * possible planting locations around it. Only plants every 50 turns
	 *
	 * @throws GameActionException
	 */
	public void hireGardener() throws GameActionException {
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

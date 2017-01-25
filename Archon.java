package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.HashMap;
import java.util.Map;

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
		makeBuildOrders();
    	hireGardener(); 
    	donateBullets2();
   }
    
    public RobotInfo findClosestRobotOfType(RobotType type) {
        for(RobotInfo r : this.bots.getBots(rc.getTeam())) {
            if(r.getType() == type) {
                return r; 
            }
        }
        return null; //TODO: Change this null statement
    }
    
    public void makeBuildOrders() throws GameActionException{
		int lumbers, tree, scouts, tanks, soldiers;
    	lumbers = (int) trees.getTreeCounts(Team.NEUTRAL) / 2;
    	tree = (int) Math.max(Math.random() + .2, 10 - trees.getTreeCounts(this.team));
    	RobotInfo closestScout = findClosestRobotOfType(RobotType.SCOUT);
    	if (closestScout == null)
    		scouts = 1;
    	else
    		scouts = 0;
    	tanks = 0;
    	soldiers = (int) (bots.getBotCounts(team.opponent()) / 2);
    	
    	Map<Codes, Integer> orders = new HashMap<Codes, Integer>();
    	orders.put(Codes.LUMBERJACK, lumbers);
    	orders.put(Codes.TANK, tanks);
    	orders.put(Codes.SCOUT, scouts);
    	orders.put(Codes.SOLIDER, soldiers);
    	orders.put(Codes.TREE, tree);
    	System.out.println(orders);
    	radio.makeBuildOrders(orders);
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
			degree += 120;
		}
	}
}

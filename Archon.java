package battlecode2017;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Archon extends AbstractBot {
    
	private int scoutsMade;
	private int gardenersMade;
	
	public Archon(RobotController rc) {
		super(rc);
		this.scoutsMade = 0;
		this.gardenersMade = 0;
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

    	orderScoutMode(); //tell all scout units which behavior protocol to run each turn
		makeBuildOrders(); //build robots
    	donateBullets2(); //buy victory points
    	wander();
    }


   	public void orderScoutMode() throws GameActionException { //broadcasts to scout units to dictate their behavior mode depending available bullet trees etc.

		ArrayList<TreeInfo> bulletTrees = trees.getBulletTrees();
		if (bulletTrees.size() > 0) { //neutral bullet trees still out there
			radio.broadcast(Channels.SCOUT_MODE, Codes.SCOUTMODE_HARVEST);
		} else { //no more bullet trees left to harvest
			radio.broadcast(Channels.SCOUT_MODE, Codes.SCOUTMODE_ATTACK);
		}

	}
    
    public RobotInfo findClosestRobotOfType(RobotType type) {
        for(RobotInfo r : this.bots.getBots(rc.getTeam())) {
            if(r.getType() == type) {
                return r; 
            }
        }
        return null; //TODO: Change this null statement
    }
    
    public int getTypeCount(RobotType type, Team t) {
        int count = 0; 
        List<RobotInfo> bots = (List<RobotInfo>) this.bots.getBots(t);
        for(RobotInfo b : bots) {
            if(b.getType() == type) {
                count++; 
            }
        }
        return count;
    }
    
    public boolean noBots(RobotType type, Team t) {
        return getTypeCount(type, t) == 0; 
    }
    
    /*
     * Priority Listing:
     *   Tank
     *   Lumberjack
     *   Scout
     *   Soldier
     *   Tree
     * 
     * make soldier first but also doesn't make more gardeners than we need
     */
    public void makeBuildOrders() throws GameActionException{
		int lumbers=0, tree=0, scouts=0, tanks=0, soldiers=0, gardeners=0;
    	//TODO
		tree = 3;
		if(trees.getTreeCounts(this.team) > 2){
			//Scout
	    	scouts = (int) (Math.random() + .1);
	    	
			//LumberJacks
			lumbers = (int) trees.getTreeCounts(Team.NEUTRAL) / 2;
    	
			soldiers = (int) (Math.random() + .3) + bots.getBotCounts(team.opponent());
			
			if (soldiers > 0)
				lumbers = 0;
    	
			if (rc.getTeamBullets() > 300)
				tanks = 1;
			
		}
		gardeners = 0;
    	if(getTypeCount(RobotType.GARDENER, this.team) < 2 || Math.random() < .1)
            gardeners = 1; 
    	if(getTypeCount(RobotType.GARDENER, this.team) >= 4 || gardenersMade >= 5)
    		gardeners = 0;
    	if(Math.random() < .05)
    		gardeners = 1;

    	Map<Codes, Integer> orders = new HashMap<Codes, Integer>();
        orders.put(Codes.SOLIDER, soldiers);
        orders.put(Codes.TANK, tanks);
        orders.put(Codes.LUMBERJACK, lumbers);
        orders.put(Codes.SCOUT, scouts);
    	orders.put(Codes.TREE, tree);
    	System.out.println(orders);
    	if (gardeners > 0)
    		hireGardener();
    	radio.makeBuildOrders(orders);
    }
    
	/** Trys to plant a gardener around an Archon by checking different
	 * possible planting locations around it. Only plants every 50 turns
	 *
	 * @throws GameActionException
	 */
	public boolean hireGardener() throws GameActionException {
		Direction dir = BotUtils.randomDirection();
		int numTries = 5, count = 0;
		while (count < numTries) {
			if (rc.canHireGardener(dir)) {
				rc.hireGardener(dir);
				this.gardenersMade++;
				return true;
			}
			dir = dir.rotateLeftDegrees(360 / numTries);
			count++;
		}
		return false;
	}

}

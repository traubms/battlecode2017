package battlecode2017;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Archon extends AbstractBot {
    
	private int scoutsMade;
	
	public Archon(RobotController rc) {
		super(rc);
		this.scoutsMade = 0;
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
        int maxGardeners = 15; 
		int lumbers, tree, scouts, tanks, soldiers, gardeners;
    	
		//LumberJacks
		lumbers = (int) trees.getTreeCounts(Team.NEUTRAL) / 2;
    	
		//Trees
		tree = (int) Math.max(Math.random() + .2, 10 - trees.getTreeCounts(this.team));
    	
		//Gardeners
		gardeners = (int) Math.min(rc.getTeamBullets() / 100, maxGardeners);
		
		//Scout
    	scouts = (int) (Math.random() + .3);
    	
    	//Tanks
    	tanks = 0; 
    	
    	//Soldiers
    	soldiers = 1;
    	
    	boolean noTanks = noBots(RobotType.TANK, this.team); 
        boolean needSoldiers = getTypeCount(RobotType.SOLDIER, this.team) < soldiers; 
    	Map<Codes, Integer> orders = new HashMap<Codes, Integer>();
    	
    	if(soldiers > 0 || lumbers > 0)
    		tree = 0;
        if(getTypeCount(RobotType.GARDENER, this.team) >= gardeners)
            gardeners = 0; 

        orders.put(Codes.SOLDIER, soldiers);
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

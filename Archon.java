package battlecode2017;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Archon extends AbstractBot {
    
	private List<MapLocation> enemyTargets;
	private Map<RobotType, Integer> rollCall;
	private boolean iAmHead;
	private int noHeadCount;
	
	
	public Archon(RobotController rc) {
		super(rc);
		this.iAmHead = false;
		this.noHeadCount = 0;
		this.enemyTargets = new ArrayList<MapLocation>();
		MapLocation[] enemyArch = rc.getInitialArchonLocations(team.opponent());
		for (MapLocation loc: enemyArch)
			enemyTargets.add(loc);
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
    	dodge();
    	if (bots.getBotCounts(team.opponent()) > 0)
    		avoidEnemies();
    	else
    		moveAvoidingGardeners();
    	updateTargets();
    	if (iAmHead) {
        	updateRollCall();
    		makeBuildOrders(); //build robots
    		decideSwarmLocation();
    	} else 
    		checkIfIAmHead();
    	followBuildOrders();
    }
    
    public void checkIfIAmHead() throws GameActionException {
    	boolean noOtherHeadSignal = radio.listen(Channels.ARCHON_COUNT) == 0;
    	if(noOtherHeadSignal){
			if (rc.getRoundNum() < 2 || noHeadCount > 3)
				iAmHead = true;
			else
				noHeadCount++;
		} else {
			this.noHeadCount = 0;
		}
    }
    
    public void updateRollCall() throws GameActionException{
    	rollCall = radio.rollCall();
    }
    
    public void decideSwarmLocation() throws GameActionException {
    	int round = rc.getRoundNum() ;
    	
		// Pick destination
		boolean march;
    	MapLocation target = null;
    	if (bots.getBotCounts(team.opponent()) > 0) { // Defend archon
    		march = true;
    		target = rc.getLocation();
    	} else if (enemyTargets.size() > 0) { // only if target available
    		// march schedule 
    		if (round >= 500) 
	    		march = true;
    		else
        		march = false;
    		
    		// go out to enemy or come back
    		if(march){
	    		if (enemyTargets.size() > 0) //&& round % 300 < 100) 
	    			target = this.closestEnemyTarget();
	    		else
	    			target = rc.getLocation();
    		}
    	} else 
    		march = false;
    	
    	// Send out location
    	if (march) {
    		radio.setSwarmLocation(target);
//    		System.out.println("SWARM: " + target);
    	}
    	radio.setForwardMarch(march);
    }
    
    public void updateTargets() throws GameActionException{
    	// Check if enemy reported
    	int round = rc.getRoundNum();
    	MapLocation enemiesReported;
    	if (round - radio.listen(Channels.ENEMY_DETECTED) / 100 < 20) {
    		enemiesReported = radio.getReportedEnemies();
    		if(enemiesReported != null && !enemyTargets.contains(enemyTargets))	{
	    		enemyTargets.add(enemiesReported);
    		}
		}
    	
    	// Check if destination reached
    	MapLocation reached = radio.checkReachSwarmLocation();
		if (reached != null){
			for(MapLocation loc: enemyTargets) {
				if (loc.distanceTo(reached) < 2) {
					enemyTargets.remove(loc);
					System.out.println("HEARD YOU LOUD AND CLEAR: " + enemyTargets.size());
					break;
				}
			}
		}
    }
    
    public MapLocation closestEnemyTarget(){
    	float minDist = 1000, dist;
    	MapLocation closest = null, myLoc = rc.getLocation();
    	for(MapLocation loc: this.enemyTargets) {
    		dist = myLoc.distanceTo(loc);
    		if (dist < minDist){
    			minDist = dist;
    			closest = loc;
    		}
    	}
    	return closest;
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
		int lumberOrder=0, treeOrder=0, scoutOrder=0, tankOrder=0, soldierOrder=0, gardenerOrder=0;
		int gardeners = rollCall.get(RobotType.GARDENER);
		int soldiers = rollCall.get(RobotType.SOLDIER);
    	
		//TODO
		treeOrder = 3;

    	scoutOrder = (int) (Math.random() + .03);
		lumberOrder = (int) trees.getTreeCounts(Team.NEUTRAL) / 2;
		soldierOrder = (int) (Math.random() + .3) + bots.getBotCounts(team.opponent());
			
		gardenerOrder = 0;
		if(gardeners == 0)
			gardenerOrder = 1;
		else if (gardeners == 1 && soldiers == 0)
			gardenerOrder = 0;
		else if(gardeners == 1){
			if(Math.random() < .40)
	            gardenerOrder = 1; 
		} else if(gardeners <= 4){
			if(Math.random() < .10)
	            gardenerOrder = 1; 
		} else {
			if(Math.random() < .04)
	            gardenerOrder = 1; 
		}

    	Map<Codes, Integer> orders = new HashMap<Codes, Integer>();
        orders.put(Codes.SOLIDER, soldierOrder);
        orders.put(Codes.TANK, tankOrder);
        orders.put(Codes.LUMBERJACK, lumberOrder);
        orders.put(Codes.SCOUT, scoutOrder);
    	orders.put(Codes.TREE, treeOrder);
    	orders.put(Codes.GARDENER, gardenerOrder);
    	radio.makeBuildOrders(orders);
    }
    
    public void followBuildOrders() throws GameActionException{
    	int gardeners = radio.checkBuildOrders().getOrDefault(Codes.GARDENER, 0);
    	if (gardeners > 0)
    		hireGardener();
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
				return true;
			}
			dir = dir.rotateLeftDegrees(360 / numTries);
			count++;
		}
		return false;
	}
	
   	public void orderScoutMode() throws GameActionException { //broadcasts to scout units to dictate their behavior mode depending available bullet trees etc.

		ArrayList<TreeInfo> bulletTrees = trees.getBulletTrees();
		if (bulletTrees.size() > 0) { //neutral bullet trees still out there
			radio.broadcast(Channels.SCOUT_MODE, Codes.SCOUTMODE_HARVEST);
		} else { //no more bullet trees left to harvest
			radio.broadcast(Channels.SCOUT_MODE, Codes.SCOUTMODE_ATTACK);
		}

	}

}

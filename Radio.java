package battlecode2017;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Radio {
		
	private RobotController rc;
	public final Codes[] BOTS = {
			Codes.TREE,
			Codes.LUMBERJACK,
			Codes.SCOUT,
			Codes.TANK,
			Codes.SOLIDER
	};

	public Radio(RobotController rc) {
		this.rc = rc;
	}
	
	public int listen(int channel) throws GameActionException{
		return (int)rc.readBroadcastFloat(channel);
	}
	
	public int listen(Channels channel) throws GameActionException{
		return listen(channel.getValue());
	}
	
	public void broadcast(int channel, float data) throws GameActionException{
		rc.broadcastFloat(channel, data);
	}
	
	public void broadcast(Channels channel, Codes data) throws GameActionException{
		broadcast(channel.getValue(), data.getValue());
	}
	
	public void broadcast(Channels channel, float data) throws GameActionException{
		broadcast(channel.getValue(), data);
	}
	
	public Map<Codes, Integer> checkBuildOrders() throws GameActionException{
		float message = listen(Channels.BUILD);
		Map<Codes, Integer> counts = new HashMap<Codes, Integer>();
		if (message > 1){
			int count;
			for(Codes bot: BOTS){
				count = 0;
				while (message > 0 && message % bot.getValue() == 0){
					count++;
					message /= bot.getValue();
				}
				if (count > 0)
					counts.put(bot, count);
			}
		}
		return counts;
	}
	
	public void makeBuildOrders(Map<Codes, Integer> counts) throws GameActionException{
		int message = 1;
		for(Codes bot: counts.keySet()){
			for(int i = 0; i < counts.get(bot); i++)
				message *= bot.getValue();
		}
		broadcast(Channels.BUILD, message);
	}
	
	public void reportBuild(Codes bot) throws GameActionException{
		float message = listen(Channels.BUILD);
		if(message % bot.getValue() != 0)
			broadcast(Channels.BUILD, message / bot.getValue());
	}
	
	public void addToBuildQueue(Codes bot) throws GameActionException{
		float message = listen(Channels.BUILD);
		if (message == 0)
			message = 1;
		if(message % bot.getValue() != 0)
			broadcast(Channels.BUILD, message * bot.getValue());
	}
	
	public void setForwardMarch(boolean march) throws GameActionException{
		this.broadcast(Channels.FORWARD_MARCH, (march) ? 1 : 0);
	}
	
	public boolean forwardMarch() throws GameActionException{
		return this.listen(Channels.FORWARD_MARCH) == 1;
	}
	
	public void setSwarmLocation(MapLocation loc) throws GameActionException{
		this.broadcast(Channels.SWARM_X, loc.x);
		this.broadcast(Channels.SWARM_Y, loc.y);
	}

	public MapLocation swarmLocation() throws GameActionException {
		return new MapLocation(listen(Channels.SWARM_X), this.listen(Channels.SWARM_Y));
	}
	
	public void reachedSwarmLocation(MapLocation loc) throws GameActionException{
		this.broadcast(Channels.REACHED_SWARM_X, loc.x);
		this.broadcast(Channels.REACHED_SWARM_Y, loc.y);
	}

	public MapLocation checkReachSwarmLocation() throws GameActionException {
		MapLocation loc = new MapLocation(listen(Channels.REACHED_SWARM_X), this.listen(Channels.REACHED_SWARM_Y));
		if (loc.x == 0 && loc.y == 0){
			return null;
		} else {
			reachedSwarmLocation(new MapLocation(0, 0));
			return loc;
		}
	}
	
	public void reportEnemies(BotReport bots) throws GameActionException{
		float message = listen(Channels.ENEMY_DETECTED);
		int round = (int) (message / 1000);
		int count = (int) (message % 1000);
		int numEnemies = bots.getBotCounts(rc.getTeam().opponent());
		MapLocation myLoc;
		if(rc.getRoundNum() - round > 20 || numEnemies > count) {
			myLoc = rc.getLocation();
			broadcast(Channels.ENEMY_DETECTED, 1000 * rc.getRoundNum() + numEnemies);
			broadcast(Channels.ENEMY_DETECTED_X, myLoc.x);
			broadcast(Channels.ENEMY_DETECTED_Y, myLoc.y);
		}
	}
	
	public MapLocation getReportedEnemies() throws GameActionException{
		return new MapLocation(listen(Channels.ENEMY_DETECTED_X), listen(Channels.ENEMY_DETECTED_Y));
	}
	
	public void reportTrees(TreeReport trees) throws GameActionException{
		int treeCount = trees.getNumberSurroundingTrees();
		if (treeCount > 0){
			float message = listen(Channels.TREE_COUNT);
			this.broadcast(Channels.TREE_COUNT, message + treeCount);
		}
	}
	
	public int getTreeCounts() throws GameActionException{
		float message = listen(Channels.TREE_COUNT);
		this.broadcast(Channels.TREE_COUNT, 0);
		return (int) message;
	}
	
	
	
	

}

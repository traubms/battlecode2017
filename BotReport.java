package battlecode2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.RobotInfo;

public class BotReport {

	private RobotController rc;
	private HashMap<Team, List<RobotInfo>> bots;
	private HashMap<Team, Integer> counts;
	private HashMap<Team, Float> lowestHealthValue;
	private HashMap<Team, RobotInfo> lowestHealthBot;
	
	public BotReport(RobotController rc) {
		this.rc = rc;
	}
	
	public void update(){
		RobotInfo[] botList = rc.senseNearbyRobots();
		reset(botList.length);
		RobotInfo bot;
		int count;
		for(int i = 0; i < botList.length; i++){
			bot = botList[i];
			
			count = this.counts.get(bot.team);
			this.bots.get(bot.team).add(bot);
			count++;
			this.counts.replace(bot.team, count);
			if (bot.health < this.lowestHealthValue.get(bot.team)){
				this.lowestHealthValue.replace(bot.team, bot.health);
				this.lowestHealthBot.replace(bot.team, bot);
			}
		}
		
	}
	
	public RobotInfo getWeakestbot(Team t){
		return this.lowestHealthBot.get(t);
	}
	
	public RobotInfo getClosestbot(Team t){
		if (this.counts.get(t) > 0)
			return this.bots.get(t).get(0);
		else
			return null;
	}
	
	public List<RobotInfo> getBots(Team t){
		return this.bots.get(t);
	}
	
	public int getBotCounts(Team t){
		return this.counts.get(t);
	}
	
	public int getTotalBots(){
		return getBotCounts(Team.A) + getBotCounts(Team.B);
	}
	
	private void reset(int length){
		this.bots = new HashMap<Team, List<RobotInfo>>();
		this.counts = new HashMap<Team, Integer>();
		this.lowestHealthValue = new HashMap<Team, Float>();
		this.lowestHealthBot = new HashMap<Team, RobotInfo>();

		for(Team t: Team.values()){
			if (t != Team.NEUTRAL){
				this.bots.put(t, new ArrayList<RobotInfo>(length));
				this.counts.put(t, 0);
				this.lowestHealthValue.put(t, (float) 1000000);
			}
		}
	}
	
	public void reset(){
		reset(0);
	}
	
}


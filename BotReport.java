package battlecode2017;

import java.util.ArrayList;
import java.util.HashMap;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.RobotInfo;

public class BotReport {

	private RobotController rc;
	private HashMap<Team, RobotInfo[]> bots;
	private HashMap<Team, Integer> counts;
	private HashMap<Team, Float> lowestHealthValue;
	private HashMap<Team, RobotInfo> lowestHealthBot;
	
	public BotReport(RobotController rc) {
		this.rc = rc;
		this.bots = null;
	}
	
	public void update(){
		RobotInfo[] botList = rc.senseNearbyRobots();
		reset(botList.length);
		RobotInfo bot;
		int count;
		for(int i = 0; i < botList.length; i++){
			bot = botList[i];
			
			count = this.counts.get(bot.team);
			this.bots.get(bot.team)[count] = bot;
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
			return this.bots.get(t)[0];
		else
			return null;
	}
	
	public int getBotCounts(Team t){
		return this.counts.get(t);
	}
	
	public int getTotalBots(){
		return getBotCounts(Team.A) + getBotCounts(Team.B);
	}
	
	private void reset(int length){
		bots = new HashMap<Team, RobotInfo[]>();
		counts = new HashMap<Team, Integer>();
		lowestHealthValue = new HashMap<Team, Float>();
		lowestHealthBot = new HashMap<Team, RobotInfo>();

		for(Team t: Team.values()){
			if (t != Team.NEUTRAL){
				this.bots.put(t, new RobotInfo[length]);
				this.counts.put(t, 0);
				this.lowestHealthValue.put(t, (float) 1000000);
			}
		}
	}
	
	public void reset(){
		reset(0);
	}
	
}


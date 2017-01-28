package battlecode2017;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.GameActionException;
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
		rc.broadcastFloat(channel, (int)data);
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
	

}

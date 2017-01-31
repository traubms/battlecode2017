package battlecode2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import battlecode.common.*;

import javax.xml.stream.Location;


public class Scout extends AbstractBot {

	Direction heading;
	
	public Scout(RobotController rc) throws GameActionException {
		super(rc);
		heading = BotUtils.randomDirection();
	}
	
	public void run() throws GameActionException {

        trees.update();
        bots.update();
        dodge();
        shake();
        hideOnTree();
        MapLocation goal = nearestEnemyBotOrTreeOrBulletTree();
        if (goal == null)
            wander();
        else
        	moveTowardsOrWander(goal);
        radio.reportEnemies(bots);
        attack();
    }
	
	public boolean wander() throws GameActionException{
		while(!rc.onTheMap(rc.getLocation().add(heading, 10)))
			heading = BotUtils.randomDirection();
		if(!avoidEnemies(5))
			return tryMove(heading);
		else {
			heading = BotUtils.randomDirection();
			return true;
		}
	}

    //************************** Scout Behavior Modes **************************//

	//pestering attack mode that targets nearest UNDEFENDED enemy bullet trees (Code 17)
    public void Attack_Mode() throws GameActionException {
		trees.update();
		bots.update();
		dodge();

		TreeInfo enemy_tree = trees.getClosestTree(rc.getTeam().opponent());
		List<RobotInfo> nearbyEnemyList = bots.getBots(rc.getTeam().opponent());

		float tree_dist;
		float enemy_dist;
		Direction retreat;
		if (enemy_tree == null)
			tree_dist = 100000;
		else 
			tree_dist = enemy_tree.getLocation().distanceTo(rc.getLocation());
		for (int i = 0; i < nearbyEnemyList.size(); i++) { //measure the risk (enemy distance vs target distance) to decide whether or not to attack
			enemy_dist = nearbyEnemyList.get(i).getLocation().distanceTo(rc.getLocation());
			if (enemy_dist - tree_dist >= 1.0) {
				attack();
				tryMove(rc.getLocation().directionTo(enemy_tree.getLocation())); //move towards target tree
			} else {
				while (enemy_dist <= 6.0) {
					//direction opposite enemy
					if (enemy_tree == null)
						retreat = nearbyEnemyList.get(i).getLocation().directionTo(rc.getLocation());
					else
						retreat = new Direction(rc.getLocation().directionTo(enemy_tree.getLocation()).rotateLeftRads((float)Math.PI).radians);
					if (tryMove(retreat) != false) {
						tryMove(retreat); //attempt to move away from enemy
						attack();
					} else {
						wander(); //if retreat fails, move randomly and attack
						attack();
					}
				}
			}
		}
	}

	//harvest bullets from neutral bullet trees mode (Code 19)
	public void Harvest_Mode() throws GameActionException {
		trees.update();
		bots.update();
		dodge();

		ArrayList<TreeInfo> bulletTrees = trees.getBulletTrees();
		if (bulletTrees.size() > 0) { //seek out neutral bullet trees

			if (!shake())
				this.tryMove(rc.getLocation().directionTo(bulletTrees.get(0).location));
			else
				wander();

		} else {
			wander();
		}
	}


	//defend unit type (Code 23)
	public void Defend_Mode() throws GameActionException {
		trees.update();
		bots.update();
		dodge();

		//find archon locations, store them in an array
		List<RobotInfo> botList = bots.getBots(rc.getTeam());
		List<MapLocation> archon_locations = null;
		for (int i = 0; i < botList.size(); i++) {
			if (botList.get(i).getType() == RobotType.ARCHON)
				archon_locations.add(botList.get(i).getLocation());
		}
		
		//randomly choose an archon to defend and find its location (ensures scouts are approximately evenly spread out per archon)
		int i = (int)Math.rint(archon_locations.size());
		MapLocation arc_loc = archon_locations.get(i);

		//find a random nearby location to defend from, and move to that location
		int degree = (int)Math.rint(360.0);
		float radian = ((float) degree * (float) Math.PI) / 180;
		Direction def_dir = new Direction(radian);
		float radius = (float)2;
		MapLocation def_pos = arc_loc.add(def_dir, radius); //float value argument in add() sets the radius of the defense perimeter

		while (rc.getLocation() != def_pos) { //attempt to move to selected defense position, choosing a new defense position if the robot can't move to the current one
			if (rc.getLocation().distanceTo(def_pos) <= 1.25) { // if desired position is within one stride

				if (tryMove(rc.getLocation().directionTo(def_pos)) == false) {
					Direction reposition = new Direction(radian);
					def_pos = def_pos.add(reposition, (float) 1);
				} else {
					tryMove(rc.getLocation().directionTo(def_pos));
				}

			} else {

				if (rc.getLocation() != def_pos && tryMove(rc.getLocation().directionTo(def_pos)) != false) //try to move to def_pos
					tryMove(rc.getLocation().directionTo(def_pos));
				else  //if the move fails, reposition by wandering randomly
					wander();

			}
		}

		//attack if enemy is sensed
		attack();
	}


	//scout mode - broadcast information on enemy position (by concentration) and map layout (Code 27)
	public void Recon_Mode() throws GameActionException {
		trees.update();
		bots.update();
		dodge();

		List<RobotInfo> enemies = bots.getBots(rc.getTeam().opponent());
		List<RobotInfo> friendlies = bots.getBots((rc.getTeam()));
		for (int i = 0; i < enemies.size(); i++) {
			if (enemies.get(i).getType() == RobotType.ARCHON) {
				tryMove(rc.getLocation().directionTo(enemies.get(i).getLocation()));
			} else {
				for (int in = 0; in < friendlies.size(); in++) {
					if (friendlies.get(in).getType() == RobotType.ARCHON) {
						tryMove(rc.getLocation().directionTo(friendlies.get(in).getLocation()).opposite());
					} else {
						wander();
					}
				}
			}

		}

		//implement a broadcast call to relay desired info type depending on strategy - currently set to broadcast enemy archon position.
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots((float)14, rc.getTeam().opponent());
		if (nearbyEnemies != null) {
			for (int in = 0; in < nearbyEnemies.length; in++) {
				if (nearbyEnemies[in].getType() == RobotType.ARCHON) {
					radio.broadcast(Channels.SCOUT_REPORT, Codes.SCOUT_REPORT_ARCHON);
				}
			}
		}

	}

	//***************************************************************************//

    /** Gets scouts to hide on enemy trees if they see gardeners and enemy trees, doesn't include attacking
     * @return whether or not the scout moved (returns false if couldn't sense gardener and enemy tree as well)
     * @throws GameActionException
     */

    public boolean hideOnTree() throws GameActionException {
        //Find closest enemy gardener
	    List<RobotInfo> EBots = bots.getBots(team.opponent());
	    RobotInfo closestGardener = null;
	    for (RobotInfo ri : EBots) {
	        if (ri.getType().equals(RobotType.GARDENER)) {
	            closestGardener = ri;
	            break;
            }
        }
        //If found enemy gardener, find enemy tree closest to said gardener
        if (closestGardener!=null) {
	        List<TreeInfo> nearTrees = trees.getTrees(team.opponent());
	        TreeInfo closest2gardener = null;
	        for (TreeInfo ti : nearTrees) {
	            //long if statement is "if we haven't found a tree yet or this tree is closer to the gardener than the previous closest"
	            if (closest2gardener == null
                        || closestGardener.location.distanceTo(ti.location)
                        < closestGardener.location.distanceTo(closest2gardener.location)) {
	                closest2gardener = ti;
                }
            }
            //If found appropriate gardener and tree, make a location slightly offset from the center of the tree
            //in the direction of the gardener and try to move there.
            if (closest2gardener != null) {
	            MapLocation slightlyOff =
                        closest2gardener.location.add(closest2gardener.location.directionTo(closestGardener.location), (float) 0.02);
	            return moveTo(slightlyOff);
            }
        }
        return false;
    }

}

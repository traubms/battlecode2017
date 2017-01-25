package battlecode2017;

import java.util.ArrayList;
import java.util.HashMap;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class TreeReport {

	private RobotController rc;
	private HashMap<Team, TreeInfo[]> trees;
	private HashMap<Team, Integer> counts;
	private HashMap<Team, Integer> withinInteract;
	private HashMap<Team, Float> lowestHealthValue;
	private HashMap<Team, TreeInfo> lowestHealthTree;
	
	private ArrayList<TreeInfo> containsBullets;
	private ArrayList<TreeInfo> containsRobot;
	
	public TreeReport(RobotController rc) {
		this.rc = rc;
		this.trees = null;
	}
	
	public void update(){
		TreeInfo[] treeList = rc.senseNearbyTrees();
		reset(treeList.length);
		TreeInfo tree;
		int count;
		boolean canInteract = true;
		for(int i = 0; i < treeList.length; i++){
			tree = treeList[i];
			if (canInteract)
				canInteract = rc.canInteractWithTree(tree.location);
			count = this.counts.get(tree.team);
			this.trees.get(tree.team)[count] = tree;
			count++;
			this.counts.replace(tree.team, count);
			if (canInteract){
				this.withinInteract.replace(tree.team, count);
				if (tree.health < this.lowestHealthValue.get(tree.team)){
					this.lowestHealthValue.replace(tree.team, tree.health);
					this.lowestHealthTree.replace(tree.team, tree);
				}
			}
			if (tree.getContainedBullets() > 0)
				this.containsBullets.add(tree);
			if (tree.getContainedRobot() != null)
				this.containsRobot.add(tree);
		}
	}
	
	public TreeInfo getWeakestTree(Team t){
		return this.lowestHealthTree.get(t);
	}
	
	public TreeInfo getClosestTree(Team t){
		if (this.counts.get(t) > 0)
			return this.trees.get(t)[0];
		else
			return null;
	}
	
	public int getTreeCounts(Team t){
		return this.counts.get(t);
	}
	
	public int getTotalTrees(){
		return getTreeCounts(Team.A) + getTreeCounts(Team.B) + getTreeCounts(Team.NEUTRAL);
	}
	
	public ArrayList<TreeInfo> getBulletTrees(){
		return this.containsBullets;
	}
	
	public ArrayList<TreeInfo> getRobotTrees(){
		return this.containsRobot;
	}
	
	public TreeInfo pickRandomTree(Team t){
		if (this.withinInteract.get(t) > 0){
			int index = (int) Math.random() * this.withinInteract.get(t);
			return this.trees.get(t)[index];
		} else
			return null;
	}
	
	private void reset(int length){
		this.trees = new HashMap<Team, TreeInfo[]>();
		this.counts = new HashMap<Team, Integer>();
		this.withinInteract = new HashMap<Team, Integer>();
		this.lowestHealthTree = new HashMap<Team, TreeInfo>();
		this.lowestHealthValue = new HashMap<Team, Float>();
		this.containsBullets = new ArrayList<TreeInfo>();
		this.containsRobot = new ArrayList<TreeInfo>();
		for(Team t: Team.values()){
			this.trees.put(t, new TreeInfo[length]);
			this.counts.put(t, 0);
			this.withinInteract.put(t, 0);
			this.lowestHealthTree.put(t, null);
			this.lowestHealthValue.put(t, (float) 1000000);
		}
		
	}
	
	public void reset(){
		reset(0);
	}
	
}

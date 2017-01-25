package battlecode2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class TreeReport {

	private RobotController rc;
	private TreeInfo[] treeList;
	private HashMap<Team, List<TreeInfo>> trees;
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
		reset(treeList.length);
		treeList = rc.senseNearbyTrees();
		
		TreeInfo tree;
		int count;
		boolean canInteract = true;
		for(int i = 0; i < treeList.length; i++){
			tree = treeList[i];
			if (canInteract)
				canInteract = rc.canInteractWithTree(tree.location);
			this.trees.get(tree.team).add(tree);
			if (canInteract){
				this.withinInteract.replace(tree.team, this.trees.get(tree.team).size());
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
		if (this.trees.get(t).size() > 0)
			return this.trees.get(t).get(0);
		else
			return null;
	}
	
	public TreeInfo getClosestTree(){
		if (this.treeList.length > 0)
			return this.treeList[0];
		else
			return null;
	}
	
	public int getTreeCounts(Team t){
		return this.trees.get(t).size();
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
	
	public List<TreeInfo> getTreesWithinInteract(Team t){
		return this.trees.get(t).subList(0, this.withinInteract.get(t));
	}
	
	public TreeInfo pickRandomTree(Team t){
		if (this.withinInteract.get(t) > 0){
			int index = (int) Math.random() * this.withinInteract.get(t);
			return this.trees.get(t).get(index);
		} else
			return null;
	}
	
	private void reset(int length){
		this.treeList = new TreeInfo[0];
		this.trees = new HashMap<Team, List<TreeInfo>>();
		this.withinInteract = new HashMap<Team, Integer>();
		this.lowestHealthTree = new HashMap<Team, TreeInfo>();
		this.lowestHealthValue = new HashMap<Team, Float>();
		this.containsBullets = new ArrayList<TreeInfo>();
		this.containsRobot = new ArrayList<TreeInfo>();
		for(Team t: Team.values()){
			this.trees.put(t, new ArrayList<TreeInfo>(length));
			this.withinInteract.put(t, 0);
			this.lowestHealthTree.put(t, null);
			this.lowestHealthValue.put(t, (float) 1000000);
		}
	}
	
	public void reset(){
		reset(0);
	}
	
}

package battlecode2017;

import battlecode.common.RobotType;

public enum Codes {

	TREE(2),
	LUMBERJACK(3, RobotType.LUMBERJACK),
	SCOUT(5, RobotType.SCOUT),
	SOLIDER(7, RobotType.SOLDIER),
	TANK(11, RobotType.TANK),
    GARDENER(13, RobotType.GARDENER),
    
	SCOUTMODE_ATTACK(17, RobotType.SCOUT),
	SCOUTMODE_HARVEST(19, RobotType.SCOUT),
	SCOUTMODE_DEFEND_ARCHONS(23, RobotType.SCOUT),
	SCOUTMODE_SCOUT_ENEMY(27, RobotType.SCOUT),
	SCOUT_REPORT_ARCHON(29, RobotType.SCOUT), //used to report the position of an enemy archon
	SCOUT_REPORT_BOTTREE(31, RobotType.SCOUT); //used to report a tree with a robot in it so a lumberjack can harvest


	private int value;
	private RobotType robotType;
	
	Codes(int value, RobotType robotType){
		this.value = value;
		this.robotType = robotType;
	}
	
	Codes(int value){
		this(value, null);
	}
	
	Codes(){
		this(0);
	}
	
	public int getValue(){
		return this.value;
	}
	
	public RobotType getRobotType(){
		return this.robotType;
	}
}

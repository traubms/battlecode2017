package battlecode2017;

import battlecode.common.RobotType;

public enum Codes {

	TREE(2),
	LUMBERJACK(3, RobotType.LUMBERJACK),
	SCOUT(5, RobotType.SCOUT),
	SOLDIER(7, RobotType.SOLDIER),
	TANK(11, RobotType.TANK),
    GARDENER(13, RobotType.GARDENER);
	
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

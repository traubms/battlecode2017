package battlecode2017;

import battlecode.common.RobotType;

public enum Codes {
	TREE(2), //coded by Traub
	LUMBERJACK(3, RobotType.LUMBERJACK), //coded by Traub
	SCOUT(5, RobotType.SCOUT), //coded by Traub
	SOLIDER(7, RobotType.SOLDIER), //coded by Traub
	TANK(11, RobotType.TANK); //coded by Traub

	
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

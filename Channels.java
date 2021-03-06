package battlecode2017;

public enum Channels {
	BUILD(1),
	SHOULD_BUILD(2),
	GARDENER_COMMANDS(3),
	GARDENER(4),
	SCOUT_MODE(5), //used to broadcast to scout to modify scout behavior mode based on needs
	SCOUT_REPORT(6), //used by a scout to broadcast information regarding enemy position etc.
	FORWARD_MARCH(7), 
	SWARM_X(8),
	SWARM_Y(9), 
	REACHED_SWARM_X(10),
	REACHED_SWARM_Y(11),
	ENEMY_DETECTED(12),
	ENEMY_DETECTED_X(13),
	ENEMY_DETECTED_Y(14),
	TREE_COUNT(15),
	SCOUT_COUNT(16),
	SOLDIER_COUNT(17),
	LUMBERJACK_COUNT(18),
	TANK_COUNT(19), 
	GARDENER_COUNT(20),
	ARCHON_COUNT(21),
	LEFT_EDGE(22),
	RIGHT_EDGE(23),
	BOTTOM_EDGE(24),
	TOP_EDGE(25);
	
	private int value;
	
	Channels(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}

}


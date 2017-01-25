package battlecode2017;

public enum Channels {
	BUILD(1),
	SHOULD_BUILD(2),
	GARDENER_COMMANDS(3),
	GARDENER(4);
	
	private int value;
	
	Channels(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}

}


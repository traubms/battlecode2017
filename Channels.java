package battlecode2017;

public enum Channels {
	BUILD(1), //coded by Traub
	SHOULD_BUILD(2), //coded by Traub
	GARDENER_COMMANDS(3), //coded by Traub
	GARDENER(4); //coded by Traub
	
	private int value;
	
	Channels(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}

}


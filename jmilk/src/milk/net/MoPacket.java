package milk.net;


public class MoPacket {

	public int type;
	int id;
	byte[] payload;

	MoPacket(int type, int id, byte[] payload) {
		this.type = type;
		this.id = id;
		this.payload = payload;
	}

}

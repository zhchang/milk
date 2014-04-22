package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InOneResourceMessage extends InMessage {

	public String msgId;
	public String domain;
	public String game;
	public String resourceId;
	public int version;
	public byte[] resourceData;

	InOneResourceMessage(String msgId) {
		this.msgId = msgId;
	}

	void readFromStream(MilkInputStream dis) throws IOException {
		domain = readVarChar(dis);
		game = readVarChar(dis);
		resourceId = readVarChar(dis);
		version = dis.readInt();
		int len = dis.readInt();
		resourceData = new byte[len];
		dis.read(resourceData);
	}

}

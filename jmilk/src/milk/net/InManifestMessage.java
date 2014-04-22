package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InManifestMessage extends InMessage {

	public String domain;
	public String game;
	public byte[] manifestData;

	void readFromStream(MilkInputStream dis) throws IOException {
		domain = readVarChar(dis);
		game = readVarChar(dis);
		int len = dis.readInt();
		manifestData = new byte[len];
		dis.readFully(manifestData);
	}

}

package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InMultiResourceMessage extends InMessage {

	public String domain;
	public String game;
	public int resCount;
	public String resourceId[];
	public int version[];
	public byte[][] resourceData;

	void readFromStream(MilkInputStream dis) throws IOException {

		domain = readVarChar(dis);
		game = readVarChar(dis);
		resCount = dis.readInt();
		resourceId = new String[resCount];
		version = new int[resCount];
		resourceData = new byte[resCount][];

		for (int i = 0; i < resCount; i++) {
			resourceId[i] = readVarChar(dis);
			version[i] = dis.readInt();
			int len = dis.readInt();
			resourceData[i] = new byte[len];
			dis.read(resourceData[i]);
		}
	}

}

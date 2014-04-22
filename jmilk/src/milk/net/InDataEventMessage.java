package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InDataEventMessage extends InMessage {

	public String key;
	public String value;
	public String msgId;

	void readFromStream(MilkInputStream dis) throws IOException {
		readVarChar(dis);
		readVarChar(dis);
		key = readVarChar(dis);
		value = readIntStr(dis);
	}
}

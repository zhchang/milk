package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InDynamicPayMessage extends InMessage {

	public String messageId;
	public byte action;
	public int result;
	public String parameters;

	InDynamicPayMessage(String messageId, byte action) {
		this.messageId = messageId;
		this.action = action;
	}

	void readFromStream(MilkInputStream dis) throws IOException {
		result = dis.readInt();
		parameters = readIntStr(dis);

		// System.out.println("InDynamicPayMess age parameters:" + parameters);
	}

}

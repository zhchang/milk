package milk.net;

import java.io.IOException;

import milk.implement.MilkOutputStream;

public class OutGameMessage extends OutMessage {

	private String msgId;
	private byte[] msgData;

	public OutGameMessage(int serviceId, String msgId, byte[] body) {
		super(serviceId, OutMessage.ID_GameMessage, null);
		this.msgId = msgId;
		this.msgData = body;
	}

	void writeToStream(MilkOutputStream dos) throws IOException {
		writeVarChar(dos, msgId);
		dos.write(msgData);
	}

}

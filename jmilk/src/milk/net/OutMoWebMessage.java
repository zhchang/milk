package milk.net;

import java.io.IOException;

import milk.implement.MilkOutputStream;

public class OutMoWebMessage extends OutMessage {

	private String msgId;
	private byte[] msgData;

	public OutMoWebMessage(int serviceId, String msgId, byte[] body) {
		super(serviceId, OutMessage.ID_MoWebMessage, null);
		this.msgId = msgId;
		this.msgData = body;
	}

	void writeToStream(MilkOutputStream dos) throws IOException {
		writeVarChar(dos, msgId);
		dos.write(msgData);
	}

}

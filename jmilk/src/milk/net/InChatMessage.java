package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InChatMessage extends InMessage {

	public String messageId;
	public byte action;
	public byte messageType;
	public int senderId;
	public String senderName;

	public int displayColor;
	public String messageContent;

	public int duration;

	InChatMessage(String messageId, byte action) {
		this.messageId = messageId;
		this.action = action;
	}

	void readFromStream(MilkInputStream dis) throws IOException {
		messageType = dis.readByte();
		senderId = dis.readInt();
		senderName = readIntStr(dis);
		displayColor = dis.readInt();
		messageContent = readIntStr(dis);
		duration = dis.readInt();
	}

}

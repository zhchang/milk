package milk.net;

import java.io.IOException;

import milk.chat.core.Def;
import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;

public class OutChatMessage extends OutMessage {

	private static final String MESSAGE_ID_PREFIX = getMessageIdPrefix();

	private String messageId;
	private byte action;
	private int messageType;
	private int receiveId;
	private String senderName;
	private String messageContent;

	public OutChatMessage(int messageId, byte action, byte messageType,
			int receiveId, String senderName, String messageContent) {
		super(Adaptor.getInstance().chatServiceId, Def.CHAT_SERVICE_ID, null);
		this.action = action;
		this.messageId = MESSAGE_ID_PREFIX + messageId;
		this.messageType = messageType;
		this.receiveId = receiveId;
		this.senderName = senderName;
		this.messageContent = messageContent;

	}

	public static String getChatMessageIdPrefix() {
		return MESSAGE_ID_PREFIX;
	}

	public String getMessageId() {
		return messageId;
	}

	void writeToStream(MilkOutputStream dos) throws IOException {
		writeVarChar(dos, messageId);
		dos.writeByte(action);
		dos.writeByte(messageType);
		dos.writeInt(receiveId);
		writeIntStr(dos, senderName);
		writeIntStr(dos, messageContent);
	}

	private static String getMessageIdPrefix() {
		return String.valueOf(System.currentTimeMillis());
	}
}

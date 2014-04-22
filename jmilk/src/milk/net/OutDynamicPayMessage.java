package milk.net;

import java.io.IOException;

import milk.chat.core.Def;
import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;

public class OutDynamicPayMessage extends OutMessage {

	private String messageId;
	private byte action;
	private String parameters;

	public OutDynamicPayMessage(int messageId, byte action, String param) {
		super(Adaptor.getInstance().chatServiceId, Def.CHAT_SERVICE_ID, null);
		this.messageId = OutChatMessage.getChatMessageIdPrefix() + messageId;
		this.action = action;
		this.parameters = param;
	}

	void writeToStream(MilkOutputStream dos) throws IOException {
		writeVarChar(dos, messageId);
		dos.writeByte(action);
		writeIntStr(dos, parameters);
	}

}

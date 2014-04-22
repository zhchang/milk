package milk.net;

import java.io.IOException;

import milk.chat.port.CoreListener;
import milk.implement.MilkInputStream;

public class InChatResponseMessage extends InMessage {

	public byte result;
	public String errorMessage;
	public String messageId;

	InChatResponseMessage(String messageId) {
		this.messageId = messageId;
	}

	void readFromStream(MilkInputStream dis) throws IOException {
		result = dis.readByte();
		if (result != CoreListener.CHAT_SEND_SUCCESS) {
			errorMessage = readIntStr(dis);
		}
	}

}

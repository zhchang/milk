package milk.chat.core;

import milk.net.InMessage;

public interface ChatListener {
	void receiveMessage(InMessage message);
}

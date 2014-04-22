package milk.chat.port;

import milk.chat.core.Message;


public interface MsgListener {

	void receiveTopMessage(Message topMessage);
	
	void receiveWorldMessage(Message worldMessage);
	
	void receiveFamilyMessage(Message familyMessage);
	
	void receivePrivateMessage(Message privateMessage);
	
	void receiveSystemMessage(Message systemMessage);
	
	void sendMessageFail(String msgId);

	void sendMessageSuccess(String msgId);
	
}

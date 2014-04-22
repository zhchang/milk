package milk.chat.core;


public interface PopMenuListener {
	
    String actionMainPage="mainPage";
    String actionPrivateChat="privateChat";
    String actionTopSend="actionTopSend";
    String actionCancel="cancel";
    String actionReply="reply";
    String actionPayForTool="payForTool";
    String actionWorldMsgTo="worldMessageTo";
    
	void handlePopMenuEvent(MenuItem item, int userId, String userName, int focus);

}

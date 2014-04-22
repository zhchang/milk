package milk.chat.port;

import milk.chat.core.FaceCoreHandler;
import milk.chat.core.MenuItem;
import milk.chat.core.Message;

public interface CoreListener {

	byte CHAT_SEND_SUCCESS = 0;
	//fail reason code define
	byte SEND_FAIL_FLOODING = 1;
	byte SEND_FAIL_FORBIDDEN_WORD = 2;
	byte SEND_FAIL_WRONG_ID = 3;
	byte SEND_FAIL_NO_TOOL = 4;
	byte SEND_FAIL_TOO_LONG = 5;
	byte SEND_FAIL_LAST_TOPMESSAGE_VALID=6;
	
	void initListener(MsgListener ml, UIListener ul);
	
	void sendMessage(byte type,int receiveId, String receiveName,String content);

	MenuItem[] getInputMenuItem(byte roomType);
	
	MenuItem[] getAndroidInputMenuItem(byte roomType);
	
	boolean showInputMenu(byte roomType,String title,int toId,String toName);
	
	MenuItem[] getMessageMenuItems(Message message);
	
	MenuItem[] getAndroidMessageMenuItems(Message message);
	
	boolean showMessageMenu(Message message);
	
	FaceCoreHandler getFaceHandler();
	
	void setMessageLineWidth(int messageLineWidth);
	
	void setTopMessageLineWidth(int topMessageLineWidth);

	void initL10nString();
	
	String getTitle();

	String[] getRoomNameList() ;

	byte[] getRoomTypeList();
	
	byte getRoomType(int focus);

	int getRoomIndex(byte type) ;
	
	String getL10nString(String info, String replace);
	
	void setDebug();
	
	boolean isDebug();
	
}

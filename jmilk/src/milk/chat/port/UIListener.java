package milk.chat.port;

import milk.chat.core.MenuItem;
import milk.chat.core.PopMenuListener;
import milk.ui2.MilkFont;

public interface UIListener extends PopMenuListener{

	void setCoreListener(CoreListener l);
	
	void showHall();
	
	void setFocusRoomType(byte roomType);
	
    void notifyShowPopMenu(String title,MenuItem items[], int toId, String toName,PopMenuListener l);
    
    void notifyShowAlertInfo(String info);
 
    void exitApp();
    
    void setChatUser(int userId,String userName);
    
    boolean isInChatScreen();
    
    int getLineHeight();

    void handlePopMenuEvent(MenuItem item, int toId,String toName,int focus);
    
    MilkFont getFont();
    
}

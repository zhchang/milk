package milk.ui.androidchat;

import milk.chat.core.Def;
import milk.ui2.MilkGraphics;

public class AndroidButtonScreen {
	
    private static int btnSpaceX=2;
    private static int buttonWidth=AndroidResourceManager.iconBg.getWidth();
    private static int sendWidth=AndroidResourceManager.send.getWidth();
    private static boolean needLoad=true;
    
    private static AndroidButton send;
    private static AndroidButton face,topSend,friend;
    
    private AndroidButtonListener listener;
    private AndroidButton list[];
	
	AndroidButtonScreen(int screenW, int screenH, byte type) {
		if (screenW >= 480) {
			btnSpaceX=6;
		} else if (screenW >= 320) {
			btnSpaceX=4;
		} else {
			btnSpaceX=2;
		}
		init(screenW, screenH);
		switch (type) {
		case Def.CHAT_TYPE_WORLD:
			list = new AndroidButton[] { face, topSend, send };
			break;
		case Def.CHAT_TYPE_PRIVATE:
			list = new AndroidButton[] { face, friend, send };
			break;
		case Def.CHAT_TYPE_FAMILY:
			list = new AndroidButton[] { face, send };
			break;
		}

	}
	
	private void init(int screenW, int screenH) {
		if (needLoad) {

			int buttonY = screenH - AndroidResourceManager.send.getHeight() - 2;
			send = new AndroidButton(AndroidResourceManager.send, AndroidResourceManager.sendFocus, "Send");
			send.setPosition(screenW - AndroidResourceManager.send.getWidth() - btnSpaceX,
					buttonY);

			face = new AndroidButton(AndroidResourceManager.iconBg, AndroidResourceManager.iconBgFocus,
					AndroidResourceManager.face);
			face.setPosition(btnSpaceX, buttonY);

			topSend = new AndroidButton(AndroidResourceManager.iconBg, AndroidResourceManager.iconBgFocus,
					AndroidResourceManager.topMsg);
			topSend.setPosition(AndroidResourceManager.iconBg.getWidth() + btnSpaceX * 2,
					buttonY);

			friend = new AndroidButton(AndroidResourceManager.iconBg,
					AndroidResourceManager.iconBgFocus, AndroidResourceManager.selectFriend);
			friend.setPosition(AndroidResourceManager.iconBg.getWidth() + btnSpaceX * 2,
					buttonY);
			needLoad=false;
		}
	}
	
	boolean isTouchFaceIcon(int x,int y){
		return face.isTouched(x, y)&&isActive(face);
	}
	
	boolean isTouchTopSendIcon(int x,int y){
		return topSend.isTouched(x, y)&&isActive(topSend);
	}
	
	boolean isTouchSendIcon(int x,int y){
		return send.isTouched(x, y)&&isActive(send);
	}
	
	boolean isTouchFriendIcon(int x,int y){
		return friend.isTouched(x, y)&&isActive(friend);
	}
	
	private boolean isActive(AndroidButton button){
		for(int i=0;i<list.length;i++){
			if(list[i]==button){
				return true;
			}
		}
		return false;
	}
	
	void setButtonListener(AndroidButtonListener bl){
		listener=bl;
	}
	
	static int getButtonHeight(){
		return AndroidResourceManager.send.getHeight();
	}
	
	static int getAllButtonWidth(byte type){
		return sendWidth+btnSpaceX*2+getLeftButtonWidth(type);
	}
	
	static int getLeftButtonWidth(byte type) {
		if (type == Def.CHAT_TYPE_FAMILY)
			return buttonWidth + btnSpaceX * 2;
		return buttonWidth * 2 + btnSpaceX * 3;
	}

	void draw(MilkGraphics g, byte type) {
		for (int i = 0; i < list.length; i++) {
			list[i].draw(g);
		}
	}
	
	void pointerPressed(int x, int y, byte type) {
		for (int i = 0; i < list.length; i++) {
			if (list[i].isTouched(x, y)) {
				handleButton(list[i]);
			}
		}
	}
	
	private void handleButton(AndroidButton button) {
		if (face == button) {
			listener.handleFace();
		} else if (topSend == button) {
			listener.handleTopSend();
		} else if (send == button) {
			listener.handleSend();
		} else if (friend == button) {
			listener.handleFriend();
		}
	}
}

package milk.ui.androidchat;

import milk.chat.core.Def;



import milk.chat.core.HallAccess;
import milk.chat.core.Utils;
import milk.chat.port.CoreListener;
import milk.implement.EditorSetting;
import milk.implement.InputReceiver;
import milk.ui.HandlerMsg;
import milk.ui.MilkFontImpl;
import milk.ui.UIHelper;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class AndroidInputBox implements InputReceiver, AndroidFaceListener,AndroidButtonListener,SendListener {
	
	private static final int BACK_COLOR=0x8d6b4b;
	private static final int MAX_INPUT_CONTENT_LENGTH = 75;
	private static final int MIN_SEND_INTERVAL_TIME = 1000;
	private int screenWidth, screenHeight;
	private long previousSendTime;
	private int rectX, rectY, rectW, rectH;
//	private static MilkApp factory;
	private boolean isFocus = true;
	private EditorSetting editorSetting;
	private byte chatType;
	private CoreListener coreListener;
	private AndroidButtonScreen screenButton;
	
	public AndroidInputBox(MilkApp factory, byte chatType) {
//		InputBox.factory = factory;
		screenWidth=factory.getCanvasWidth();
		screenHeight=factory.getCanvasHeight();
		this.chatType = chatType;
		editorSetting = new EditorSetting();
		screenButton = new AndroidButtonScreen(screenWidth,screenHeight,chatType);
		screenButton.setButtonListener(this);
	}

	boolean isTouchFaceIcon(int x,int y){
		return screenButton.isTouchFaceIcon(x, y);
	}
	
	boolean isTouchTopSendIcon(int x,int y){
		return screenButton.isTouchTopSendIcon(x, y);
	}
	
	boolean isTouchSendIcon(int x,int y){
		return screenButton.isTouchSendIcon(x, y);
	}
	
	boolean isTouchFriendIcon(int x,int y){
		return screenButton.isTouchFriendIcon(x, y);
	}
	
	public void setFocus(final boolean focus) {
		isFocus = focus;
	}


	private String msgTo;
	
	void setMsgTo(String msgto){
		msgTo=msgto;
	}
	
	public void setInitInputText(String input) {
		if (input == null || input.length() == 0) {
			input = "";
		}
		inputText = input;
		HandlerMsg.setInputText(input);
	}
	
	public void updateInput(final String input) {
		inputText = input;
//		Utils.info("-------updateInput text:" + inputText);
		
	}

	public String getInitText() {
		return (inputText == null) ? "" : inputText;
	}

	public void initInputBox(int x, int y, int w, int h) {
		rectX = x;
		rectY = y + 2;
		rectH = h;
		rectW = w;

		editorSetting.x = rectX;
		editorSetting.y = rectY + 2;
		editorSetting.width = rectW;
		editorSetting.height = h - 4;
		editorSetting.maxlength = 75;
		editorSetting.maxLines = 7;
		editorSetting.bgColor = 0xfffea3;
		editorSetting.receiver = this;
		editorSetting.setTextSize = true;
		editorSetting.textSize = MilkFontImpl.getDefaultFont().getHeight();
	}

	public void showEdit() {
		if (editorSetting.receiver == null) {
			editorSetting.receiver = this;
		}
		UIHelper.milk.showInput(editorSetting);
	}

	public static void hideEdit() {
		UIHelper.milk.hideInput();
	}
	
	void pointerPressed(int x, int y) {
		screenButton.pointerPressed(x, y,chatType);
	}

	public void draw(MilkGraphics g) {
		g.setClip(0, 0, screenWidth,screenHeight);
		g.setColor(BACK_COLOR);
		g.fillRect(0, rectY-2, screenWidth, rectH+4);
		screenButton.draw(g,chatType);	
		if (isFocus){
			return;
		}
		RoundRect inputRect= AndroidResourceManager.inputFrame;
		g.setClip(0, rectY, screenWidth, rectH);
		inputRect.drawRoundRect(g, rectX, rectY, rectW, rectH);
		g.setClip(rectX, 0, rectW, screenHeight);
		g.setColor(0x000000);
		MilkFont font = g.getFont();
		g.setFont(MilkFontImpl.getDefaultFont());
		if (inputText != null){
			g.drawString(inputText, rectX +1, rectY+(rectH-g.getFont().getHeight())/2, 0);
		}
		g.setClip(0, 0, screenWidth, screenHeight);
		
		g.setFont(font);
	}

	public boolean isTouch(int x, int y) {
		return Utils.pointInRect(x, y, rectX, rectY, rectW, rectH);
	}

	private String popToName;
	private int popToId;

	public void setTarget(int id, String name) {
		popToId = id;
		popToName = name;
	}

	private String inputText;

	public void showNotify() {
		HandlerMsg.focusInput();
	}

	private boolean sendChatMessage(byte sendType) {
		String contents = inputText;
//		Utils.info("------ sendChatMessage contents:" +contents);
		coreListener = HallAccess.getCoreListener();
		if (contents != null && contents.length() > 0) {
			if (contents.length() > MAX_INPUT_CONTENT_LENGTH) {
				String info = coreListener.getL10nString(
						Def.chatInpuMsgTooLong, MAX_INPUT_CONTENT_LENGTH + "");
				showInputError(info);
				return false;
			}
			if (System.currentTimeMillis() - previousSendTime < MIN_SEND_INTERVAL_TIME) {
				showInputError(Def.chatInputSendTooFast);
				return false;
			}
			if (coreListener != null) {
				if(sendType==Def.CHAT_TYPE_WORLD&&this.msgTo!=null){
					contents=msgTo+contents;
					msgTo=null;
				}
				coreListener
						.sendMessage(sendType, popToId, popToName, contents);
				previousSendTime = System.currentTimeMillis();
			} else {
				return false;
//				throw new NullPointerException("chatListener=null");
			}
		} else {
			showInputError(Def.chatInputMsgIsNull);
			return false;
		}
		return true;
	}

	private void showInputError(String info) {
//		Utils.info("-------inputBox showInputError:" + info);
		HandlerMsg.showAlert(info);
	}

	public void insertFace(String faceName) {
//		Utils.info("-------inputBox insertFace face:" + faceName);
		HandlerMsg.insertFace(faceName);
	}
	
	private void clearInputText(){
		inputText = "";
		HandlerMsg.setInputText("");
	}
	
	public void notifySendEvent(){
		Utils.info("------ ------ notifyPayEvent:");
		clearInputText();
	}

	public void handleSend() {
		if (popToId <= 0 && chatType == Def.CHAT_TYPE_PRIVATE) {
			showInputError(Def.chatErrorNoTarget);
			return;
		} else if (chatType == Def.CHAT_TYPE_FAMILY) {
			if (HallAccess.myFamilyId <= 0) {
				showInputError(Def.chatErrorNoTribe);
				return;
			} else {
				popToId = HallAccess.myFamilyId;
				popToName = HallAccess.myFamilyName;
			}
		}
		boolean send=sendChatMessage(chatType);
		if (send) {
			AndroidHallScreen.setSendListener(this);
		}
//		clearInputText();
	}

	public void handleTopSend() {
//		Utils.info("------ handleTopSend inputText:" +inputText);
		boolean send=sendChatMessage(Def.CHAT_TYPE_WORLD_TOP);
		if(send){
			AndroidHallScreen.setSendListener(this);
		}
//		clearInputText();
	}

	public void handleFace() {
		if (AndroidRoom.faceScreen.isPopup()) {
			AndroidRoom.faceScreen.hideScreen();
		} else {
			AndroidRoom.faceScreen.setFaceListener(this);
			AndroidRoom.faceScreen.initScreenByInputBoxY(rectY);
			AndroidRoom.faceScreen.popupFaceScreen();
			if(!this.isFocus){
				AndroidRoomManager.getFocusRoom().focusInput();
			}
		}
	}
	
	public void handleFriend() {
		// Utils.info("------------handleFriend------------");
		if (AndroidRoom.friendScreen.isPopup()) {
			AndroidRoom.friendScreen.hideScreen();
		} else {
			if (!AndroidRoom.friendScreen.hasFriendList()) {
				showInputError(Def.chatfriendlistempty);
				return;
			}
			AndroidRoom.friendScreen.setSelectListener(AndroidRoomManager.getFocusRoom());
			AndroidRoom.friendScreen.popFriendScreen(rectY);
		}
	}

}

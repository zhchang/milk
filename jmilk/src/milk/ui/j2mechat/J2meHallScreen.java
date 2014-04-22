package milk.ui.j2mechat;

import milk.chat.core.Def;


import milk.chat.core.FaceCoreHandler;
import milk.chat.core.Message;
import milk.chat.core.MenuItem;
import milk.chat.core.PopMenuListener;
import milk.chat.core.Utils;
import milk.chat.port.CoreListener;
import milk.chat.port.UIListener;
import milk.chat.port.MsgListener;
import milk.implement.Adaptor;
import milk.implement.Core;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.IMEvent.MKeyEvent;
import milk.implement.IMEvent.MRightKeyEvent;
import milk.implement.Scene;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class J2meHallScreen extends Scene implements J2meTabBarListener, UIListener,
		MsgListener {

	public static final MilkFont font = Adaptor.uiFactory.getFont(
			MilkFont.STYLE_PLAIN, MilkFont.SIZE_MEDIUM);
	public static J2meFaceScreen faceScreen;

	private String hallTitle;
	private String roomTabNameList[];
	private static final int roomX = 10;
	private static final int hallItemSpace = 3;
	private MilkApp factory;
	private final int screenWidth, screenHeight;
	private Scene backScene;
	private final int roomTabMenuY;
	private int roomY;
	private final int roomW;
	private int roomH;
	private J2meTabBar roomTabMenuBar;
	private MilkImage back;
	private int menuWidth, menuHeight, menuY;
	private J2meBackGround backGround;
	private CoreListener hallCore;

	public J2meHallScreen(MilkApp factory) {
		this.factory = factory;
		faceScreen = new J2meFaceScreen(factory);
		screenWidth = this.factory.getCanvasWidth();
		screenHeight = this.factory.getCanvasHeight();
		roomW = screenWidth - 2 * roomX;
		if (screenHeight > screenWidth)
			roomTabMenuY = font.getHeight() + 1;
		else {
			roomTabMenuY = 1;
		}
		J2meResourceManager.init();
		J2meRoomManager.init(factory);
		J2mePopMenu.init(screenWidth, screenHeight);
		J2meRoom.setChatListener(this);
		back = Utils.getImage("chat-backbtn");
		init();
		activateRightKey();
	}

	public void setCoreListener(CoreListener l) {
		hallCore = l;
		hallTitle = hallCore.getTitle();
		roomTabNameList = hallCore.getRoomNameList();
		hallCore.initListener(this, this);
		this.getFaceHandler().setFaceHeight(this.getLineHeight());
		if (faceScreen != null)
			faceScreen.setFaceCoreHandler(this.getFaceHandler());
		if (roomTabMenuBar == null) {
			roomTabMenuBar = new J2meTabBar(roomTabNameList, roomW,
					J2meResourceManager.roomTab, J2meResourceManager.roomTabFocus);
		}
		roomTabMenuBar.setPosition(roomX, roomTabMenuY);
		roomTabMenuBar.setTabListener(this);
		setFocusRoomType(Def.CHAT_TYPE_WORLD);
		hallCore.setMessageLineWidth(roomW-6);
		hallCore.setTopMessageLineWidth(roomW-6);
	}

	private void init() {
		final int inputHeight = font.getHeight();
		roomY = roomTabMenuY + J2meResourceManager.roomTab.getHeight();
		roomH = screenHeight - roomTabMenuY - inputHeight * 2 - hallItemSpace
				* 2 - hallItemSpace * 2;
		menuWidth = roomX + inputHeight;
		menuY = screenHeight - inputHeight;
		menuHeight = inputHeight;
		J2meRoomManager.initRoom(roomX, roomY, roomW, roomH, inputHeight);
	}

	public void showHall() {
		initL10n();
		hallCore.setMessageLineWidth(roomW - 4);
		hallCore.setTopMessageLineWidth(roomW - 4);
		backScene = Core.getInstance().getCurrentScene();
		Core.getInstance().switchSceneForChat(this);
		
	}

	protected void draw(MilkGraphics g) {
		J2meResourceManager.loadResource();
		g.setFont(font);
		if (backGround == null) {
			backGround = new J2meBackGround(screenWidth, screenHeight);
		}
		backGround.drawBackGround(g, hallTitle);
		Message.setLineHeight(this.getLineHeight());
		roomTabMenuBar.draw(g);
		J2meRoomManager.getFocusRoom().draw(g);
		g.setClip(0, 0, screenWidth, screenHeight);
		if (J2mePopMenu.getInstance().isShown()) {
			J2mePopMenu.getInstance().draw(g);
		}
		J2meInputBox.drawBack(g, back);
	}

	public void setFocusRoomType(byte type) {
		J2meRoomManager.switchChatRoom(type);
		if (roomTabMenuBar != null) {
			int focus = this.hallCore.getRoomIndex(type);
			roomTabMenuBar.setFocus(focus);
		}
	}

	public void handleKeyEvent(MKeyEvent key) {
		if (J2mePopMenu.getInstance().isShown()) {
			J2mePopMenu.getInstance().handleKeyEvent(key);
			return;
		}
		
		if (!J2meRoomManager.getFocusRoom().lockChatHallScreen())
			roomTabMenuBar.handleKeyEvent(key);
		J2meRoomManager.getFocusRoom().handleKeyEvent(key);
	}

	public void handleFingerEvent(MFingerEvent finger) {
		int type = finger.getType();
		int x = finger.getX();
		int y = finger.getY();
		if (J2mePopMenu.getInstance().isShown()) {
			if (type == Adaptor.POINTER_PRESSED)
				J2mePopMenu.getInstance().pointerPressed(x, y);
			return;
		}

		if (type == Adaptor.POINTER_PRESSED) {
			Utils.info("menuWidth" + menuWidth + "/menuHeight" + menuHeight);
			if (Utils.pointInRect(x, y, 0, menuY, menuWidth, menuHeight)) {
//				if (factory.getPlatform().equals("J2ME")) {
					J2meRoomManager.getFocusRoom().openChatInput();
//				}
			}
			if (Utils.pointInRect(x, y, screenWidth - menuWidth, menuY,
					menuWidth, menuHeight)//&& !factory.getPlatform().equals("BB")
					) {
				handleRightKey(null);
				return;
			}
			J2meRoomManager.getFocusRoom().pointerPressed(x, y);
			if (J2meRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabMenuBar.pointerPressed(x, y);

		} else if (type == Adaptor.POINTER_DRAGGED) {
			if (J2meRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabMenuBar.pointerDragged(x, y);
			J2meRoomManager.getFocusRoom().pointerDragged(x, y);
		} else if (type == Adaptor.POINTER_RELEASED) {
			if (J2meRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabMenuBar.pointerReleased(x, y);
			J2meRoomManager.getFocusRoom().pointerReleased(x, y);
		}
	}

	public void runCallbacks() {
		super.runCallbacks();
		J2meRoomManager.getFocusRoom().update();
//		if (PopMenu.getInstance().isShown()) {
//			if (!factory.getPlatform().equals("J2ME"))
//				factory.getMilkCanvas().setCallSuperEvent(true);
//		}
	}

	public void handleLeftKeyEvent(MRightKeyEvent rightKey) {
		J2meRoomManager.getFocusRoom().openChatInput();
	}

	public boolean handleRightKey(MRightKeyEvent rightKeyEvent) {
		if (J2mePopMenu.getInstance().isShown()) {
			J2mePopMenu.getInstance().hide();
			return true;
		}
		// if (!factory.getPlatform().equals("J2ME") && Room.bbFace.isPopup()) {
		// Room.bbFace.hideFaceScreen();
		// return true;
		// }
		boolean handle = J2meRoomManager.getFocusRoom().doRightSoftKey();
		if (!handle) {
			factory.hideInput();
			Core.getInstance().switchSceneForChat(backScene);
		}
		return true;
	}

	private boolean isFocusScreen = false;

	public void showNotify() {
		J2meRoomManager.getFocusRoom().showNotify();
		isFocusScreen = true;
//		RoomManager.getFocusRoom().hideOrShowEdit();
	}

	public void hideNotify() {
		J2meRoomManager.getFocusRoom().hideNotify();
		isFocusScreen = false;
	}

	public void notifyShowPopMenu(String title, MenuItem items[], int toId,
			String toName, PopMenuListener listener) {
		String infoTitle=title;
		if(infoTitle==null||infoTitle.length()==0){
			infoTitle=Def.popTitleWantTo;
		}
		if (listener != null) {
			J2mePopMenu.getInstance().initPopMenu(infoTitle, items, toId,
					toName, listener);
		} else {
			J2mePopMenu.getInstance().initPopMenu(infoTitle, items, toId,
					toName, this);
		}
		J2mePopMenu.getInstance().show();
	}

	public void notifyShowAlertInfo(String info) {
		J2mePopMenu.getInstance().showPopNotifyInfo(info);
	}

	public void exitApp() {
		J2meResourceManager.exit();
	}

	public void loadResource() {
		J2meResourceManager.loadResource();
	}

	public void setChatUser(int userId, String userName) {
		J2meRoomManager.getFocusRoom().setChatUser(userId, userName);
	}

	public boolean isInChatScreen() {
		return J2meFaceScreen.inFaceScreen || isFocusScreen;
	}

	public int getLineHeight() {
		return font.getHeight();
	}

	public void focusTabChange(String tabName, int focus) {
		byte roomType = this.hallCore.getRoomType(focus);
		J2meRoomManager.switchChatRoom(roomType);
//		RoomManager.getFocusRoom().hideOrShowEdit();
	}

//	public CoreListener getCoreListener() {
//		return hallCore;
//	}

	public FaceCoreHandler getFaceHandler() {
		return hallCore.getFaceHandler();
	}

	public MilkFont getFont() {
		return font;
	}

	private void initL10n() {
		hallCore.initL10nString();
		hallTitle = hallCore.getTitle();
		roomTabNameList = hallCore.getRoomNameList();
		roomTabMenuBar.initL10nString(roomTabNameList);
		J2meRoom.initL10nString();
	}

	public void handlePopMenuEvent(MenuItem item, int toId, String toName,
			int focus) {
		String popName = item.actionName;
		if (popName.equals(actionMainPage)) {
			Core.getInstance().replaceScene(backScene);
			factory.hideInput();
			Adaptor.getInstance().showOtherHomePage(toId);
		} else if (popName.equals(actionPrivateChat)) {
			setFocusRoomType(Def.CHAT_TYPE_PRIVATE);
			J2meRoomManager.getFocusRoom().setChatUser(toId, toName);
			J2meRoomManager.getFocusRoom().openChatInput();
//			RoomManager.getFocusRoom().showBBInputEdit();
		} else if (popName.equals(actionReply)) {
			J2meRoomManager.getFocusRoom().setChatUser(toId, toName);
			J2meRoomManager.getFocusRoom().openChatInput();
		} else if (popName.equals(actionTopSend)) {// top message
			J2meRoomManager.getWorldRoom().openTopMessageChatInput();
		} else if (popName.equals(actionWorldMsgTo)) {// world msg to a user
			J2meRoomManager.getWorldRoom().openWordChatInput(item.payParameters);
		}
	}

	public void receiveTopMessage(Message topMessage) {
		J2meRoomManager.getWorldRoom().showTopMessage(topMessage);
	}

	public void receiveWorldMessage(Message worldMessage) {
		J2meRoomManager.getWorldRoom().showMessage(worldMessage);
	}

	public void receiveFamilyMessage(Message familyMessage) {
		J2meRoomManager.getFamilyRoom().showMessage(familyMessage);
	}

	public void receivePrivateMessage(Message privateMessage) {
		J2meRoomManager.getPrivateRoom().showMessage(privateMessage);
	}

	public void receiveSystemMessage(Message systemMessage) {
		J2meRoomManager.getSystemRoom().showMessage(systemMessage);
	}

	public void sendMessageFail(String msgId) {
		J2meRoomManager.removeMessageById(msgId);
	}
	
	public void sendMessageSuccess(String msgId){
		
	}

}

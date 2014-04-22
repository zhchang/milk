package milk.ui.bbchat;

import milk.chat.core.Def;
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

public class BBHallScreen extends Scene implements BBTabBarListener, UIListener,
		MsgListener {

	public static final MilkFont font = Adaptor.uiFactory.getFont(
			MilkFont.STYLE_PLAIN, MilkFont.SIZE_MEDIUM);
	public static BBFaceScreen faceScreen;

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
	private BBTabBar roomTabMenuBar;
	private int menuWidth, menuHeight;
	private BBBackGround backGround;
	private CoreListener hallCore;

	public BBHallScreen(MilkApp factory) {
		this.factory = factory;
		faceScreen = new BBFaceScreen(factory);
		screenWidth = this.factory.getCanvasWidth();
		screenHeight = this.factory.getCanvasHeight();
		roomW = screenWidth - 2 * roomX;
		if (screenHeight > screenWidth)
			roomTabMenuY = font.getHeight() + 1;
		else {
			roomTabMenuY = 1;
		}
		BBResourceManager.init();
		BBRoomManager.init(factory);
		BBPopMenu.init(screenWidth, screenHeight);
		BBRoom.setChatListener(this);
		init();
		activateRightKey();
	}

	public void setCoreListener(CoreListener l) {
		hallCore = l;
		hallTitle = hallCore.getTitle();
		roomTabNameList = hallCore.getRoomNameList();
		hallCore.initListener(this, this);
		this.hallCore.getFaceHandler().setFaceHeight(this.getLineHeight());
		if (faceScreen != null)
			faceScreen.setFaceCoreHandler(hallCore.getFaceHandler());
		if (roomTabMenuBar == null) {
			roomTabMenuBar = new BBTabBar(roomTabNameList, roomW,
					BBResourceManager.roomTab, BBResourceManager.roomTabFocus);
		}
		roomTabMenuBar.setPosition(roomX, roomTabMenuY);
		roomTabMenuBar.setTabListener(this);
		setFocusRoomType(Def.CHAT_TYPE_WORLD);
		hallCore.setMessageLineWidth(roomW-6);
		hallCore.setTopMessageLineWidth(roomW-6);
	}

	private void init() {
		final int inputHeight = font.getHeight();
		roomY = roomTabMenuY + BBResourceManager.roomTab.getHeight();
		roomH = screenHeight - roomTabMenuY - inputHeight * 2 - hallItemSpace
				* 2 - hallItemSpace * 2;
		menuWidth = roomX + inputHeight;
//		menuY = screenHeight - inputHeight;
		menuHeight = inputHeight;
		BBRoomManager.initRoom(roomX, roomY, roomW, roomH, inputHeight);
	}

	public void showHall() {
		initL10n();
		hallCore.setMessageLineWidth(roomW - 4);
		hallCore.setTopMessageLineWidth(roomW - 4);
		backScene = Core.getInstance().getCurrentScene();
		Core.getInstance().switchSceneForChat(this);
		
	}

	protected void draw(MilkGraphics g) {
		 BBResourceManager.loadResource();
		g.setFont(font);
		if (backGround == null) {
			backGround = new BBBackGround(screenWidth, screenHeight);
		}
		backGround.drawBackGround(g, hallTitle);
		Message.setLineHeight(this.getLineHeight());
		roomTabMenuBar.draw(g);
		BBRoomManager.getFocusRoom().draw(g);
		g.setClip(0, 0, screenWidth, screenHeight);
		if (BBPopMenu.getInstance().isShown()) {
			BBPopMenu.getInstance().draw(g);
		}
//		InputBox.drawBack(g, back);
	}

	public void setFocusRoomType(byte type) {
		BBRoomManager.switchChatRoom(type);
		if (roomTabMenuBar != null) {
			int focus = this.hallCore.getRoomIndex(type);
			roomTabMenuBar.setFocus(focus);
		}
	}

	public void handleKeyEvent(MKeyEvent key) {
		if (BBPopMenu.getInstance().isShown()) {
			BBPopMenu.getInstance().handleKeyEvent(key);
			return;
		}
		
		if (!BBRoomManager.getFocusRoom().lockChatHallScreen())
			roomTabMenuBar.handleKeyEvent(key);
		BBRoomManager.getFocusRoom().handleKeyEvent(key);
	}

	public void handleFingerEvent(MFingerEvent finger) {
		int type = finger.getType();
		int x = finger.getX();
		int y = finger.getY();
		if (BBPopMenu.getInstance().isShown()) {
			if (type == Adaptor.POINTER_PRESSED)
				BBPopMenu.getInstance().pointerPressed(x, y);
			return;
		}

		if (type == Adaptor.POINTER_PRESSED) {
			Utils.info("menuWidth" + menuWidth + "/menuHeight" + menuHeight);
//			if (Utils.pointInRect(x, y, 0, menuY, menuWidth, menuHeight)) {
//				if (factory.getPlatform().equals("J2ME")) {
//					RoomManager.getFocusRoom().openChatInput();
//				}
//			}
//			if (Utils.pointInRect(x, y, screenWidth - menuWidth, menuY,
//					menuWidth, menuHeight)
//					&& !factory.getPlatform().equals("BB")) {
//				handleRightKey(null);
//				return;
//			}
			BBRoomManager.getFocusRoom().pointerPressed(x, y);
			if (BBRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabMenuBar.pointerPressed(x, y);

		} else if (type == Adaptor.POINTER_DRAGGED) {
			if (BBRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabMenuBar.pointerDragged(x, y);
			BBRoomManager.getFocusRoom().pointerDragged(x, y);
		} else if (type == Adaptor.POINTER_RELEASED) {
			if (BBRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabMenuBar.pointerReleased(x, y);
			BBRoomManager.getFocusRoom().pointerReleased(x, y);
		}
	}

	public void runCallbacks() {
		super.runCallbacks();
		BBRoomManager.getFocusRoom().update();
		if (BBPopMenu.getInstance().isShown()) {
//			if (!factory.getPlatform().equals("J2ME"))
				factory.getMilkCanvas().setCallSuperEvent(true);
		}
	}

	public void handleLeftKeyEvent(MRightKeyEvent rightKey) {
		BBRoomManager.getFocusRoom().openChatInput();
	}

	public boolean handleRightKey(MRightKeyEvent rightKeyEvent) {
		if (BBPopMenu.getInstance().isShown()) {
			BBPopMenu.getInstance().hide();
			return true;
		}
		// if (!factory.getPlatform().equals("J2ME") && Room.bbFace.isPopup()) {
		// Room.bbFace.hideFaceScreen();
		// return true;
		// }
		boolean handle = BBRoomManager.getFocusRoom().doRightSoftKey();
		if (!handle) {
			factory.hideInput();
			Core.getInstance().switchSceneForChat(backScene);
		}
		return true;
	}

	private boolean isFocusScreen = false;

	public void showNotify() {
		BBRoomManager.getFocusRoom().showNotify();
		isFocusScreen = true;
		BBRoomManager.getFocusRoom().hideOrShowEdit();
	}

	public void hideNotify() {
		BBRoomManager.getFocusRoom().hideNotify();
		isFocusScreen = false;
	}

	public void notifyShowPopMenu(String title, MenuItem items[], int toId,
			String toName, PopMenuListener listener) {
		String infoTitle=title;
		if(infoTitle==null||infoTitle.length()==0){
			infoTitle=Def.popTitleWantTo;
		}
		if (listener != null) {
			BBPopMenu.getInstance().initPopMenu(infoTitle, items, toId,
					toName, listener);
		} else {
			BBPopMenu.getInstance().initPopMenu(infoTitle, items, toId,
					toName, this);
		}
		BBPopMenu.getInstance().show();
	}

	public void notifyShowAlertInfo(String info) {
		BBPopMenu.getInstance().showPopNotifyInfo(info);
	}

	public void exitApp() {
		BBResourceManager.exit();
	}

	public void loadResource() {
		BBResourceManager.loadResource();
	}

	public void setChatUser(int userId, String userName) {
		BBRoomManager.getFocusRoom().setChatUser(userId, userName);
	}

	public boolean isInChatScreen() {
		return isFocusScreen;
	}

	public int getLineHeight() {
		return font.getHeight();
	}

	public void focusTabChange(String tabName, int focus) {
		byte roomType = this.hallCore.getRoomType(focus);
		BBRoomManager.switchChatRoom(roomType);
		BBRoomManager.getFocusRoom().hideOrShowEdit();
	}

//	public CoreListener getCoreListener() {
//		return hallCore;
//	}

//	public FaceCoreHandler getFaceHandler() {
//		return hallCore.getFaceHandler();
//	}

	public MilkFont getFont() {
		return font;
	}

	private void initL10n() {
		hallCore.initL10nString();
		hallTitle = hallCore.getTitle();
		roomTabNameList = hallCore.getRoomNameList();
		roomTabMenuBar.initL10nString(roomTabNameList);
//		Room.initL10nString();
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
			BBRoomManager.getFocusRoom().setChatUser(toId, toName);
			BBRoomManager.getFocusRoom().openChatInput();
			BBRoomManager.getFocusRoom().showBBInputEdit();
		} else if (popName.equals(actionReply)) {
			BBRoomManager.getFocusRoom().setChatUser(toId, toName);
			BBRoomManager.getFocusRoom().openChatInput();
		} else if (popName.equals(actionTopSend)) {// top message
			BBRoomManager.getWorldRoom().openTopMessageChatInput();
		} else if (popName.equals(actionWorldMsgTo)) {// world msg to a user
			BBRoomManager.getWorldRoom().openWordChatInput(item.payParameters);
		}
	}

	public void receiveTopMessage(Message topMessage) {
		BBRoomManager.getWorldRoom().showTopMessage(topMessage);
	}

	public void receiveWorldMessage(Message worldMessage) {
		BBRoomManager.getWorldRoom().showMessage(worldMessage);
	}

	public void receiveFamilyMessage(Message familyMessage) {
		BBRoomManager.getFamilyRoom().showMessage(familyMessage);
	}

	public void receivePrivateMessage(Message privateMessage) {
		BBRoomManager.getPrivateRoom().showMessage(privateMessage);
	}

	public void receiveSystemMessage(Message systemMessage) {
		BBRoomManager.getSystemRoom().showMessage(systemMessage);
	}

	public void sendMessageFail(String msgId) {
		BBRoomManager.removeMessageById(msgId);
	}

	public void sendMessageSuccess(String msgId){
		
	}
	
}

package milk.chat.core;

import java.util.Vector;


import milk.chat.port.CoreListener;
import milk.chat.port.UIListener;
import milk.implement.Adaptor;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class HallAccess {

	private static final int MAX_MSEEAGE_SHOW_TIME = 5000;
	private static MilkApp factory;
	private static int normalTabLineH;
	private static NotifyTab topNotifyTab, normalNotifyTab;
	private static Vector messageQueue = new Vector();
	private static long lastMessageStartShowTime;
	public static boolean isLanguageAr = false;
	private static UIListener uiListener;
	public static String myUserName = "User-A", myFamilyName = "Family-A";
	public static int myMonetId = -1;
	public static int myFamilyId = 0;
	private static RoundRect ninePatch1, ninePatch2;
	private static HallCore hallCore = new HallCore();

	public static void init(MilkApp milk, UIListener listener) {
		factory = milk;
		uiListener = listener;
		HallCore.setMilkApp(milk);
		uiListener.setCoreListener(hallCore);
		normalTabLineH = uiListener.getLineHeight();
		topNotifyTab = new NotifyTab(factory);
		normalNotifyTab = new NotifyTab(factory);
		
		int normalTabWidth = factory.getCanvasWidth() - 34 * 2 - 6;
		int normalTabX = (factory.getCanvasWidth() - normalTabWidth) / 2;

		initTabImage();
		setTopNotifyTab(40, 20, factory.getCanvasWidth() - 40);
		setNormalNotifyTab(normalTabX, factory.getCanvasHeight(),
				normalTabWidth);
	}

	private static void initTabImage() {
		ninePatch1 = Adaptor.uiFactory
				.createRoundRect("chat-topmessage1", 5, 0);
		ninePatch2 = Adaptor.uiFactory
				.createRoundRect("chat-topmessage2", 5, 0);
		if (topNinePatch != null) {
			topNotifyTab.initRect(topNinePatch, topNinePatch);
			topNotifyTab.setFrameSize(roundSize-3);
		} else {
			topNotifyTab.initRect(ninePatch1, ninePatch2);
		}
		normalNotifyTab.initRect(ninePatch1, ninePatch2);
	}
	

	private static RoundRect topNinePatch;
	private static boolean initTopTab=false;
	private static int roundSize;
	public static void initAndroidTopMessageTab(RoundRect ninePatch,int round) {
		topNinePatch = ninePatch;
		initTopTab = true;
		roundSize = round;
	}

	private static void init(){
		if (Adaptor.getInstance().language.equals("ar")) {
			isLanguageAr = true;
		} else {
			isLanguageAr = false;
		}
		myMonetId=Adaptor.getInstance().getMonetId();
	}

	public static void showNormalMessage(Message message) {
		if (uiListener.isInChatScreen()&&!hallCore.isDebug())
			return;
		if (messageQueue.size() <= 30)
			messageQueue.addElement(message);
	}

	public static void clearAllNotifyMessage(){
		messageQueue.removeAllElements();
		topNotifyTab.hideNotifyTabImmediately();
		normalNotifyTab.hideNotifyTabImmediately();
	}
	
	public static void showTopMessage(Message message) {
		if (uiListener.isInChatScreen()&&!hallCore.isDebug())
			return;
		topNotifyTab.showMessage(message);
	}

	public static void setTopNotifyTab(int leftX, int topY, int rectWidth) {
		int dy = 0;
		if (initTopTab) {
			dy = 2 * roundSize - 6;
			// Utils.info("----------setTopNotifyTab---------------rectWidth:"+rectWidth);
		}
		topNotifyTab.init(leftX, topY, rectWidth, normalTabLineH * 2 + dy);
	}

	static void hideTopMessageNotifyTab() {
		if (topNotifyTab.isShown())
			topNotifyTab.hideNotifyTab();
	}

	public static void setNormalNotifyTab(int leftX, int bottomY, int rectWidth) {
		normalNotifyTab.init(leftX, bottomY - normalTabLineH, rectWidth,
				normalTabLineH);
	}

	public static void intoWorldRoom() {
		init();
		getCoreListener().initL10nString();
		uiListener.setFocusRoomType(Def.CHAT_TYPE_WORLD);
		uiListener.showHall();
	}

	public static void intoPrivateRoom(int userId, String userName) {
		init();
		getCoreListener().initL10nString();
		uiListener.setFocusRoomType(Def.CHAT_TYPE_PRIVATE);
		uiListener.setChatUser(userId, userName);
		uiListener.showHall();
	}

	public static void exit() {
		uiListener.exitApp();
		uiListener = null;
		topNotifyTab = null;
		normalNotifyTab = null;
		messageQueue = null;
		factory = null;
		ninePatch1 = null;
		ninePatch2 = null;
		hallCore=null;
	}

	public static boolean keyPressed(int keyCode) {
		if (uiListener.isInChatScreen())
			return false;
		if (keyCode != Adaptor.KEY_NUM7)
			return false;
		if (hasMoreNormalMessage() || normalNotifyTab.isShown()) {
			init();
			intoChatHall(Def.CHAT_TYPE_WORLD);
			return true;
		} else {
			showPrivateChatRoom();
			return true;
		}
	}

	public static boolean pointerPressedTopTab(int x, int y) {
		if (uiListener.isInChatScreen())
			return false;
//		if (topNotifyTab.isTouch(x, y)) {
//			intoWorldRoom();
//			return true;
//		}
		return false;
	}

	public static boolean pointerPressedBottomTab(int x, int y) {
		if (uiListener.isInChatScreen())
			return false;
		if (!hasMoreNormalMessage() && !normalNotifyTab.isShown())
			return false;
		if (normalNotifyTab.isTouch(x, y)) {
			byte roomType=Def.CHAT_TYPE_WORLD;
			Message msg=normalNotifyTab.getMessage();
			if(msg!=null){
				roomType=msg.getType();
			}
			intoChatHall(roomType);
			return true;
		}
		return false;
	}

	private static TopMessageNotifyEffect topNotifyEffect;
	
	public static void setTopNotifyEffect(TopMessageNotifyEffect effect){
		topNotifyEffect=effect;
	}
	
	public static void drawTopNotificationBar(MilkGraphics g) {
		if (uiListener.isInChatScreen())
			return;
		MilkFont font = g.getFont();
		Message.setLineHeight(font.getHeight());
		normalTabLineH = font.getHeight();
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());
		topNotifyTab.drawTopNotifyTab(g, isLanguageAr);
		if (topNotifyEffect != null && topNotifyTab.isShown()) {
			int x = topNotifyTab.getLeftX();
			int y = topNotifyTab.getTopY();
			int rightX = topNotifyTab.getLeftX() + topNotifyTab.getRectWidth();
			topNotifyEffect.drawTopMessageNotifyEffect(g, x, y, rightX);
		}
		topNotifyTab.update();
		if (Message.getTopMessageValidTimeSecond() <= 0) {
			hideTopMessageNotifyTab();
		}
	}

	public static void drawBottomNotificationBar(MilkGraphics g) {
		if (uiListener.isInChatScreen())
			return;
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());
		MilkFont font = g.getFont();
		Message.setLineHeight(font.getHeight());
		normalTabLineH = font.getHeight();
		normalNotifyTab.drawNormalNotifyTab(g, isLanguageAr);
		if (hasMoreNormalMessage()) {
			if (normalMessageShowTimeOut()) {
				lastMessageStartShowTime = System.currentTimeMillis();
				Message normal = (Message) messageQueue.elementAt(0);
				normal.initNormalNotifyBodyString();
//				if(!normalNotifyTab.isShown()){//null
//					normalNotifyTab.showNormalMessage(normal);
//				}
//				else
				normalNotifyTab.showMessage(normal);
				messageQueue.removeElementAt(0);
			}

		} else if (normalMessageShowTimeOut()) {
			if (normalNotifyTab.isShown())
				normalNotifyTab.hideNotifyTab();
		}
		normalNotifyTab.update();
	}

	private static boolean normalMessageShowTimeOut() {
		int timeCount=1;
		if (!hasMoreNormalMessage()) {
			timeCount=3;
		}
		return System.currentTimeMillis() - lastMessageStartShowTime > MAX_MSEEAGE_SHOW_TIME*timeCount;
	}

	public static void setMyChatUserInfo(String userName, int familyId,
			String familyName) {
		myUserName = userName;
		myFamilyId = familyId;
		myFamilyName = familyName;
		myMonetId = Adaptor.getInstance().getMonetId();
	}

	public static CoreListener getCoreListener() {
		return hallCore;
		//uiListener.getCoreListener();
	}

	public static ChatListener getChatListener() {
		return hallCore;
	}

	private static void showPrivateChatRoom() {
		init();
		getCoreListener().initL10nString();
		uiListener.setFocusRoomType(Def.CHAT_TYPE_PRIVATE);
		uiListener.showHall();
	}

	private static boolean hasMoreNormalMessage() {
		return messageQueue.size() > 0;
	}

	private static void intoChatHall(byte roomType) {
		init();
		getCoreListener().initL10nString();
		uiListener.setFocusRoomType(roomType);
		uiListener.showHall();
	}
	
	static String getL10nString(String info, String replace) {
		return hallCore.getL10nString(info, replace);
	}

}

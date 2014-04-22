package milk.ui.androidchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import milk.chat.core.Def;
import milk.chat.core.HallAccess;
import milk.chat.core.Message;
import milk.chat.core.MenuItem;
import milk.chat.core.PopMenuListener;
import milk.chat.core.TopMessageNotifyEffect;
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
import milk.ui.MilkGraphicsImpl;
import milk.ui.MilkImageImpl;
import milk.ui.R;
import milk.ui.UIHelper;
import milk.ui.gesture.Gesture;
import milk.ui.gesture.GestureListener;
import milk.ui.graphics.Image;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.RoundRect;

public class AndroidHallScreen extends Scene implements AndroidTabBarListener, UIListener,
		MsgListener,GestureListener,TopMessageNotifyEffect {

	public static final MilkFont font = Adaptor.uiFactory.getFont(
			MilkFont.STYLE_PLAIN, MilkFont.SIZE_MEDIUM);
	public static AndroidPopScreen faceScreen;

	private String hallTitle;
	private String roomTabNameList[];
	private static final int roomX = 0;
	private MilkApp factory;
	private final int screenWidth, screenHeight;
	private Scene backScene;
	private final int roomTabMenuY;
	private int roomY;
	private final int roomW;
	private int roomH;
	private AndroidTabBar roomTabBar;
	private AndroidBackGround backGround;
	private CoreListener hallCore;
//	private MilkImage back;
	
	public AndroidHallScreen(MilkApp factory) {
		this.factory = factory;
		screenWidth = this.factory.getCanvasWidth();
		screenHeight = this.factory.getCanvasHeight();
		if (screenHeight > screenWidth)
			roomTabMenuY = font.getHeight()+4;
		else {
			roomTabMenuY = 4;
		}
		AndroidResourceManager.initChatImage();
		AndroidTab.init(screenWidth/4);
		double resizeFactor = AndroidTab.getResizeFactor();
		if(resizeFactor<0.8)
		   AndroidResourceManager.resizeChatImage(resizeFactor);
//		Utils.info("--AndroidHallScreen resizeFactor:" + resizeFactor);
		faceScreen = new AndroidPopScreen(factory);
		roomW = screenWidth;
		AndroidRoomManager.init(factory);
		AndroidPopMenu.init(screenWidth, screenHeight);
		AndroidRoom.setUIListener(this);
		init();
		activateRightKey();
		AndroidBubble9Patch.initScreenSize(screenWidth);
		initTopMessageImage();
		Message.initAndroidMessage();
		HallAccess.setTopNotifyEffect(this);
//		back= MilkImageImpl.createImage(R.drawable.chat_back,font.getHeight(),font.getHeight());
	}
	
	private Drawable star;
	private MilkImage speaker ,arraw;
	private void initTopMessageImage() {
		final int topRectH = font.getHeight() ;
		speaker = MilkImageImpl.createImage(R.drawable.topspeaker);
//		float resizeFactor = (float) topRectH / speaker.getHeight();
//		Utils.info("--AndroidHallScreen initTopMessageImage resizeFactor:"
//				+ resizeFactor);
//		resize(speaker, resizeFactor);
		Bitmap bitmapStar=BitmapFactory.decodeResource(UIHelper.milk.getResources(), R.drawable.topstar1);
		int realSize=topRectH+topRectH/2;
		Bitmap starReal=Image.resizeImage(bitmapStar, realSize, realSize);
		star=new BitmapDrawable(starReal);
		 
	    Utils.info("--AndroidHallScreen star w:" + star.getIntrinsicWidth()+"/ h:"+star.getIntrinsicHeight());
		MilkImage back = MilkImageImpl.createImage(R.drawable.topback);
		// resize(back, resizeFactor);

		arraw = MilkImageImpl.createImage(R.drawable.toparraw);
//		resize(arraw, resizeFactor);

		int round = 10 * back.getHeight() / 28;
//		Utils.info("--AndroidHallScreen initTopMessageImage round:" + round);
		RoundRect ninePatch = new AndroidBubble9Patch(back, round, 0);

		HallAccess.initAndroidTopMessageTab(ninePatch,round);

	}
	
//	private void resize(MilkImage img,float resizeFactor){
//		int newWidth=(int)(img.getWidth()*resizeFactor);
//		int newHeight=(int)(img.getWidth()*resizeFactor);
//		MilkImageImpl.resizeImage(img, newWidth, newHeight);
//	}

	public void setCoreListener(CoreListener l) {
		hallCore = l;
		hallTitle = hallCore.getTitle();
		roomTabNameList = hallCore.getRoomNameList();
		hallCore.initListener(this, this);
		hallCore.getFaceHandler().setFaceHeight(this.getLineHeight());
		if (faceScreen != null)
			faceScreen.setFaceCoreHandler(hallCore.getFaceHandler());
		if (roomTabBar == null) {
			roomTabBar = new AndroidTabBar(roomTabMenuY);
		}
		roomTabBar.setTabListener(this);
		setFocusRoomType(Def.CHAT_TYPE_WORLD);
		hallCore.setMessageLineWidth(roomW*75/100);//bubble
		hallCore.setTopMessageLineWidth(roomW-20);
	}

	private void init() {
		final int inputHeight = AndroidButtonScreen.getButtonHeight();
		roomY = roomTabMenuY + AndroidTab.getTabHeight();
		roomH = screenHeight - roomY - inputHeight-4;
		AndroidRoomManager.initRoom(roomX, roomY, roomW, roomH, inputHeight);
	}

	public void showHall() {
		initL10n();
		backScene = Core.getInstance().getCurrentScene();
		Core.getInstance().switchSceneForChat(this);
	}

	protected void draw(MilkGraphics g) {
		AndroidResourceManager.loadResource();
		g.setFont(font);
		if (backGround == null) {
			backGround = new AndroidBackGround(screenWidth, screenHeight);
		}
		backGround.drawBackGround(g, hallTitle);
		Message.setLineHeight(getLineHeight());
		Message.setPaintBubble(AndroidBubble9Patch.getArcSize());
		
		roomTabBar.draw(g);
		AndroidRoomManager.getFocusRoom().draw(g);
		g.setClip(0, 0, screenWidth, screenHeight);
		if (AndroidPopMenu.getInstance().isShown()) {
			AndroidPopMenu.getInstance().draw(g);
		}
	}
	
	public void drawTopMessageNotifyEffect(MilkGraphics g,int x,int y,int rightX) {
		g.drawImage(speaker, x - speaker.getWidth(), y + 2, 0);
		final int arrawDy = (16 * speaker.getHeight()) / 50;
		int arrawDx = 2;
		if (factory.getCanvasWidth() >= 320) {
			arrawDx = 4;
		}
		g.drawImage(arraw, x - arraw.getWidth() + arrawDx, y
				+ arrawDy, 0);
		
		drawImageRotate(((MilkGraphicsImpl)g).getG().getCanvas(),star,rightX-star.getIntrinsicWidth(),
				y,0,starRotate);
		
		if (starRotate + 36 >= 360) {
			starRotate = 0;
		} else {
			starRotate += 36;
		}
	}
	private float starRotate=0;

	private void drawImageRotate(Canvas canvas, Drawable img, int x, int y,
			int anchor, float degrees) {
		if (img != null && canvas != null) {
			float cx = x + img.getIntrinsicWidth() / 2;
			float cy = y + img.getIntrinsicHeight() / 2;
			canvas.save();
			canvas.rotate(degrees, cx, cy);
			img.setBounds(x, y, x+img.getIntrinsicWidth(), y+img.getIntrinsicHeight());
			img.draw(canvas);
			canvas.restore();
		}
	}

	public void setFocusRoomType(byte type) {
		AndroidRoomManager.switchChatRoom(type);
		if (roomTabBar != null) {
			int focus = this.hallCore.getRoomIndex(type);
			roomTabBar.setFocus(focus);
		}
	}

	public void handleKeyEvent(MKeyEvent key) {
		if (AndroidPopMenu.getInstance().isShown()) {
			AndroidPopMenu.getInstance().handleKeyEvent(key);
			return;
		}
		if (!AndroidRoomManager.getFocusRoom().lockChatHallScreen())
			roomTabBar.handleKeyEvent(key);
		AndroidRoomManager.getFocusRoom().handleKeyEvent(key);
	}

	public void handleFingerEvent(MFingerEvent finger) {
		int type = finger.getType();
		int x = finger.getX();
		int y = finger.getY();
//		if(this.back!=null&&type == Adaptor.POINTER_PRESSED){
//			if(Utils.pointInRect(x, y, screenWidth-back.getWidth()-10, 0, back.getWidth()+10, back.getHeight())){
//				handleBack();
//				return;
//			}
//		}
		if (AndroidPopMenu.getInstance().isShown()) {
			if (type == Adaptor.POINTER_PRESSED)
				AndroidPopMenu.getInstance().pointerPressed(x, y);
			return;
		}

		if (type == Adaptor.POINTER_PRESSED) {
			pressedEventEatByPopupScreen=AndroidRoomManager.getFocusRoom().pointerPressed(x, y);
			if (AndroidRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			roomTabBar.pointerPressed(x, y);

		} else if (type == Adaptor.POINTER_DRAGGED) {
			if (AndroidRoomManager.getFocusRoom().lockChatHallScreen())
				return;
//			roomTabBar.pointerDragged(x, y);
			AndroidRoomManager.getFocusRoom().pointerDragged(x, y);
		} else if (type == Adaptor.POINTER_RELEASED) {
			if (AndroidRoomManager.getFocusRoom().lockChatHallScreen())
				return;
			if (!focusRoomChangedBySlideEvent&&!pressedEventEatByPopupScreen) {
				roomTabBar.pointerReleased(x, y);
				AndroidRoomManager.getFocusRoom().pointerReleased(x, y);
			}
			focusRoomChangedBySlideEvent = false;
			pressedEventEatByPopupScreen=false;
		}
	}

	public void runCallbacks() {
		super.runCallbacks();
		AndroidRoomManager.getFocusRoom().update();
	}

	public void handleLeftKeyEvent(MRightKeyEvent rightKey) {
		AndroidRoomManager.getFocusRoom().openChatInput();
	}

	public boolean handleRightKey(MRightKeyEvent rightKeyEvent) {
		return handleBack();
	}
	
	private boolean handleBack(){
		if (AndroidPopMenu.getInstance().isShown()) {
			AndroidPopMenu.getInstance().hide();
			return true;
		}
		boolean handle=AndroidRoom.handleRightKey();
		if(handle){
			return true;
		}
		
	    handle = AndroidRoomManager.getFocusRoom().doRightSoftKey();
		if (!handle) {
			factory.hideInput();
			Core.getInstance().switchSceneForChat(backScene);
		}
		return true;
	}

	private boolean isFocusScreen = false;

	public void showNotify() {
		AndroidRoomManager.getFocusRoom().showNotify();
		isFocusScreen = true;
		AndroidRoomManager.getFocusRoom().hideOrShowEdit();
		Gesture.getInstance().addGestureListener(this);
		if(!hallCore.isDebug())
		    HallAccess.clearAllNotifyMessage();
	}

	public void hideNotify() {
		AndroidRoomManager.getFocusRoom().hideNotify();
		isFocusScreen = false;
		Gesture.getInstance().removeGestureListener(this);
	}

	public void notifyShowPopMenu(String title, MenuItem items[], int toId,
			String toName, PopMenuListener listener) {
		String titleShow=Def.popTitleWantTo;
		if(title!=null&&title.length()>0){
			titleShow=title;
		}
		if (listener != null) {
			AndroidPopMenu.getInstance().initPopMenu(titleShow, items, toId,
					toName, listener,roomY+roomH-2);
		} else {
			AndroidPopMenu.getInstance().initPopMenu(titleShow, items, toId,
					toName, this,roomY+roomH-2);
		}
		AndroidPopMenu.getInstance().show();
	}

	public void notifyShowAlertInfo(String info) {
		AndroidPopMenu.getInstance().showPopNotifyInfo(info,roomY+roomH-2-20);
	}

	public void exitApp() {
		AndroidResourceManager.exit();
	}

	public void loadResource() {
		AndroidResourceManager.loadResource();
	}

	public void setChatUser(int userId, String userName) {
		AndroidRoomManager.getFocusRoom().setChatUser(userId, userName);
	}

	public boolean isInChatScreen() {
		return isFocusScreen;
	}

	public int getLineHeight() {
		return font.getHeight();
	}

	public void focusTabChange(int focus) {
		byte roomType = this.hallCore.getRoomType(focus);
		AndroidRoomManager.switchChatRoom(roomType);
		AndroidRoomManager.getFocusRoom().hideOrShowEdit();
	}

	public MilkFont getFont() {
		return font;
	}

	private void initL10n() {
		hallCore.initL10nString();
		hallTitle = hallCore.getTitle();
		roomTabNameList = hallCore.getRoomNameList();
		this.roomTabBar.setNameList(roomTabNameList);
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
			AndroidRoomManager.getFocusRoom().setChatUser(toId, toName);
			AndroidRoomManager.getFocusRoom().openChatInput();
			AndroidRoomManager.getFocusRoom().showBBInputEdit();
		} else if (popName.equals(actionReply)) {
			AndroidRoomManager.getFocusRoom().setChatUser(toId, toName);
			AndroidRoomManager.getFocusRoom().openChatInput();
		} else if (popName.equals(actionTopSend)) {// top message
			AndroidRoomManager.getWorldRoom().openTopMessageChatInput();
		} else if (popName.equals(actionWorldMsgTo)) {// world msg to a user
			AndroidRoomManager.getWorldRoom().openWordChatInput(item.payParameters);
		}
        else if (popName.equals(actionPayForTool)) {
        	if(sendListener!=null){
        	   sendListener.notifySendEvent();
        	   sendListener=null;
        	}
		}
	}
	
	private static SendListener sendListener;
	
	static void setSendListener(SendListener l){
		sendListener=l;
	}
	
	public void receiveTopMessage(Message topMessage) {
		AndroidRoomManager.getWorldRoom().showTopMessage(topMessage);
	}

	public void receiveWorldMessage(Message worldMessage) {
		AndroidRoomManager.getWorldRoom().showMessage(worldMessage);
	}

	public void receiveFamilyMessage(Message familyMessage) {
		AndroidRoomManager.getFamilyRoom().showMessage(familyMessage);
	}

	public void receivePrivateMessage(Message privateMessage) {
		AndroidRoomManager.getPrivateRoom().showMessage(privateMessage);
	}

	public void receiveSystemMessage(Message systemMessage) {
		AndroidRoomManager.getSystemRoom().showMessage(systemMessage);
	}

	public void sendMessageFail(String msgId) {
		AndroidRoomManager.removeMessageById(msgId);
	}
	
	public void sendMessageSuccess(String msgId){
    	if(sendListener!=null){
     	   sendListener.notifySendEvent();
     	   sendListener=null;
     	}
	}

	private boolean focusRoomChangedBySlideEvent=false,pressedEventEatByPopupScreen=false;
	@Override
	public void onSlide(int direct, float distance, float speed) {
		if (AndroidRoomManager.getFocusRoom().lockChatHallScreen())
			return;
		System.out.println("------onSlide direct:"+ direct+"/ distance:"+distance+"/ speed:"+speed);
		switch (direct) {
		case GestureListener.SLIDE_LEFT:
			focusRoomChangedBySlideEvent=roomTabBar.onMoveFocus(1);
			break;
		case GestureListener.SLIDE_RIGHT:
			focusRoomChangedBySlideEvent=roomTabBar.onMoveFocus(-1);
			break;
		case GestureListener.SLIDE_UP:
			break;
		case GestureListener.SLIDE_DOWN:
			break;
		}
	}

}

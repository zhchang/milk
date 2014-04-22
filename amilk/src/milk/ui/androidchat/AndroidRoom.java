package milk.ui.androidchat;

import java.util.Vector;

import milk.chat.core.Def;
import milk.chat.core.HallAccess;
import milk.chat.core.MenuItem;
import milk.chat.core.Message;
import milk.chat.core.Utils;
import milk.chat.port.UIListener;
import milk.implement.Adaptor;
import milk.implement.IMEvent.MKeyEvent;
import milk.ui2.MilkApp;
import milk.ui2.MilkGraphics;

public class AndroidRoom implements AndroidSelectBoxListener {
	
	private static final int MAX_MESSAGE_QUEUE_LENGTH = 30;
	private static final int fillNormalRoomColor = 0xac8a59;
	private static int msgDisplayY = 8;
	private static final int messageSpace = 6;
	
	private int focus = 0;
	private int scrollY = 0;
	private int roomRectX, roomRectY, roomRectW, roomRectH;
	private int inputBoxHeight;
	
	private int toId = -1;
	private String toName;
	private AndroidInputBox inputBox;
	
	private Object focusObject;
	
	private static UIListener uiListener;
	private static MilkApp factory;
	
	private final Vector<Message> messageQueue;
	private Message topMessage;
	private byte roomType;
	
	public static AndroidPopScreen faceScreen;
	public static AndroidPopScreen friendScreen;

	public static void setUIListener(UIListener l) {
		uiListener = l;
	}

	public AndroidRoom(MilkApp app, byte type) {
		factory = app;
		messageQueue = new Vector<Message>(MAX_MESSAGE_QUEUE_LENGTH);
		scrollY = 0;
		roomType = type;
		switch (roomType) {
		case Def.CHAT_TYPE_PRIVATE:
			inputBox = new AndroidInputBox(factory, roomType);
			break;
		case Def.CHAT_TYPE_FAMILY:
			inputBox = new AndroidInputBox(factory, roomType);
			this.toId = HallAccess.myFamilyId;
			this.toName = HallAccess.myFamilyName;
			break;
		case Def.CHAT_TYPE_WORLD:
			inputBox = new AndroidInputBox(factory, roomType);
			break;
		case Def.CHAT_TYPE_SYSTEM:
			break;
		default:
			throw new IllegalArgumentException("ChatRoom type:" + roomType);
		}
		if (friendScreen == null) {
			friendScreen = new AndroidPopScreen(factory);
			if (HallAccess.getCoreListener().isDebug()) {
				for (int i = 0; i < 10; i++)
					friendScreen.addItem("test--" + i, 100 + i);
			}
		}
		if (faceScreen == null) {
			faceScreen = AndroidHallScreen.faceScreen;
		}
		setCurrenFocusObject(null);

	}
	
	static boolean handleRightKey(){
		if(friendScreen!=null&&friendScreen.isPopup()){
			friendScreen.hideScreen();
			return true;
		}
		if (faceScreen == null&&faceScreen.isPopup()){
			faceScreen.hideScreen();
			return true;
		}
		return false;
	}

	public void hideOrShowEdit() {
		if (inputBox != null) {
			if (inputBox == this.focusObject) {
				inputBox.showEdit();
			}
		} else {
			AndroidInputBox.hideEdit();
//			Utils.info("-------------------hide edit");
		}
	}

	void setChatUser(int id, String name) {
		toId = id;
		toName = name;
		if (friendScreen != null) {
			friendScreen.addItem(name, id);
		}
		if (inputBox != null) {
			inputBox.setTarget(toId, toName);
		}
	}

	public void initRoom(int x, int y, int w, int h, int inputHeight) {
		roomRectX = x;
		roomRectY = y;
		roomRectW = w;
		roomRectH = h;
		inputBoxHeight = inputHeight;
		int inputRectY = roomRectY + roomRectH;
		if (inputBox != null) {
			int inputX=x+AndroidButtonScreen.getLeftButtonWidth(roomType);
			int inputW=w-AndroidButtonScreen.getAllButtonWidth(roomType);
			inputBox.initInputBox(inputX, inputRectY, inputW, inputBoxHeight);
			setCurrenFocusObject(inputBox);
		} else {
			roomRectH = h + inputBoxHeight+4;
		}
		firstLayoutTopMessage = true;
		
	}
	
	void focusInput(){
		if (inputBox != null) {
			setCurrenFocusObject(inputBox);
			inputBox.showNotify();
		}
	}

	public void showNotify() {
		if (inputBox != null) {
			setCurrenFocusObject(inputBox);
			inputBox.showNotify();
		} else {
			focusLastMessage();
		}
		if (toId == -1 && roomType == Def.CHAT_TYPE_PRIVATE) {
			friendScreen.setSelectListener(this);
			friendScreen.initUserQueue(messageQueue);
		}
		if (topMessage != null) {
			int topMessageValidSecond = Message.getTopMessageValidTimeSecond();
			if (topMessageValidSecond <= 0) {
				topMessage.resetTopMessageNotifyInfo();
			}
		}
		else if(roomType == Def.CHAT_TYPE_WORLD){
			topMessage = new Message();
		}

	}

	public void hideNotify() {
		scrollYSpeed = 0;
	}
  
	public Message removeMessage(String id) {
		for (int i = messageQueue.size() - 1; i >= 0; i--) {
			Message chat = messageQueue.elementAt(i);
			if (id.equals(chat.getMessageId())) {
				messageQueue.removeElement(chat);
				// System.out.println(" room->remove Message id=" + id);
				return chat;
			}
		}
		return null;
	}

	private static int topMessageH;
    void draw(MilkGraphics g) {
		if (firstLayoutTopMessage && roomType == Def.CHAT_TYPE_WORLD) {
//			Message.setLineHeight(g.getFont().getHeight());
		    topMessageH = Message.getTopMessageHeight()+6;
//			roomRectY = roomRectY + topMessageH;
//			roomRectH -= topMessageH;
			firstLayoutTopMessage = false;
		}
		if (msgDisplayY < AndroidBubble9Patch.getArcSize() * 2+6) {
			msgDisplayY = AndroidBubble9Patch.getArcSize() * 2+6;
		}
		g.setColor(fillNormalRoomColor);
		g.fillRect(roomRectX, roomRectY, roomRectW, roomRectH);
		g.fillRect(roomRectX, roomRectY, roomRectW, 30);
		int messageRectW = roomRectW - 2 * messageSpace;
		int messageRectX = roomRectX + messageSpace / 2+4;

		boolean isLanguageAr=HallAccess.isLanguageAr;
		synchronized (messageQueue) {
		drawAllMessage(g, messageRectW, messageRectX, isLanguageAr);
		}
		if (Message.getTopMessageValidTimeSecond() > 0) {
			drawTopMessage(g, messageRectW, messageRectX, topMessageH,
					isLanguageAr);
		} else {
			if(topMessage!=null)
			   topMessage.resetTopMessageNotifyInfo();
		}
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());

		if (this.messageQueue.size() == 0) {
			String noMessage = null;
			if (this.roomType == Def.CHAT_TYPE_FAMILY) {
				noMessage = Def.noTribeMessage;
			} else if (this.roomType == Def.CHAT_TYPE_SYSTEM) {
				noMessage = Def.noSystemMessage;
			}
			if(noMessage!=null){
				int drawX=(factory.getCanvasWidth()-g.getFont().stringWidth(noMessage))/2;
				int drawY=factory.getCanvasHeight()/2-20;
				Message.drawNoMessageNotifyInfo(g, noMessage, drawX, drawY, 0xfff000);
			}
		}
		if (inputBox != null) {
			inputBox.draw(g);
		}
		
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());

		drawScroolBar(g);
		if (faceScreen.isPopup()) {
			faceScreen.drawFaceScreen(g);
		}
		if (friendScreen.isPopup()) {
			friendScreen.drawFriendScreen(g);
		}

	}

	private void drawAllMessage(MilkGraphics g, int messageRectW,
			int messageRectX,boolean isLanguageAr) {
		int messageRectY = scrollY + roomRectY+msgDisplayY;
		g.setClip(roomRectX + 1, roomRectY + 1, roomRectW - 2, roomRectH - 2);
		
		for (int i = 0; i < messageQueue.size(); i++) {
			Message chat = messageQueue.elementAt(i);
			boolean isFous = (focus == i && focusObject instanceof Message && focusObject != topMessage);
			boolean sendMyself=chat.isSendByMyself();
			
			AndroidBubble9Patch bubbleRect9;
			if (sendMyself) {
				bubbleRect9=AndroidResourceManager.bubbleMe;
			}
			else{
				bubbleRect9=AndroidResourceManager.bubbleOther;
			}
			
			int messageH = chat.getDrawHeight();
			int bubbleW = chat.getBubbleWidth();
			int bubbleH = chat.getBubbleHeight();
//			int msgLeftX=messageRectX+bubbleRect9.getFrameSize();
			int bubbleX=messageRectX;
			if(sendMyself){
//				msgLeftX=roomRectX+roomRectW-12-bubbleW+bubbleRect9.getFrameSize();
				bubbleX=roomRectX+roomRectW-12-bubbleW;
			}
			int border=0;
			if(isFous&& (System.currentTimeMillis() / 500) % 2 > 0){
				border=2;
			}
			final int bubbleY=messageRectY-border+chat.getTitleHeight();
			bubbleRect9.drawRoundRect(g, bubbleX-border, 
					bubbleY, bubbleW+border*2, bubbleH+border*2);
			
			if (messageRectY + messageH > roomRectY
					&& messageRectY < roomRectY + roomRectH) {
				int msgX=bubbleX+bubbleRect9.getFrameSize();
				int msgW=bubbleW-2*bubbleRect9.getFrameSize();
				chat.drawBubbleMessage(g, msgX, messageRectY+bubbleRect9.getFrameSize(), msgW,
						isFous,isLanguageAr,bubbleY,bubbleX,bubbleX+bubbleW,bubbleRect9.getFrameSize());
			}
			messageRectY += chat.getDrawHeight()+msgDisplayY;
		}
	}

	private static boolean firstLayoutTopMessage = true;

	private void drawTopMessage(MilkGraphics g, int messageRectW,
			final int messageRectX,final int topMessageRectH,boolean isLanguageAr) {
		if (topMessage != null && topMessage.getCreateTime() != 0) {
//			int topMessageH = topMessage.getTopMessageRectHeight();
			boolean isFousTopMessage = (focus == 0 && focusObject == topMessage);
			final int focusRectY = roomRectY;
			final int focusRectX = roomRectX;
			final int focusRectW = roomRectW;
//			final int focusRectH = topMessageH;
			int offsetY = 0;
			g.setColor(0x8d6b4b);
			AndroidBubble9Patch bubbleRect9;
			g.fillRect(focusRectX, focusRectY, focusRectW, topMessageRectH);

			if (topMessage.isSendByMyself()) {
				bubbleRect9 = AndroidResourceManager.bubbleMe;
			} else {
				bubbleRect9 = AndroidResourceManager.bubbleOther;
			}
			int bubbleW = topMessage.getBubbleWidth();
			int bubbleH = topMessage.getBubbleHeight();
			offsetY = (topMessageRectH - topMessage.getDrawHeight()) / 2;

			int bubbleX = messageRectX;
			if (topMessage.isSendByMyself()) {
				bubbleX = focusRectX + focusRectW - bubbleW - messageRectX;
			}
			int border = 0;
			if (isFousTopMessage && (System.currentTimeMillis() / 500) % 2 > 0) {
				border = 2;
			}
			final int bubbleY = focusRectY + offsetY - border
					+ topMessage.getTitleHeight();
			
			bubbleRect9.drawRoundRect(g, bubbleX - border, bubbleY, bubbleW
					+ border * 2, bubbleH + border * 2);
			g.setClip(focusRectX + 1, focusRectY + 1, focusRectW - 2,topMessageRectH - 2);

//			if (Message.getTopMessageValidTimeSecond() > 0) {
				int msgX = bubbleX + bubbleRect9.getFrameSize();
				int msgW = bubbleW - 2 * bubbleRect9.getFrameSize();
				topMessage.drawBubbleMessage(g, msgX, focusRectY + offsetY
						+ bubbleRect9.getFrameSize(), msgW, isFousTopMessage,
						isLanguageAr, bubbleY, bubbleX, bubbleX + bubbleW,
						bubbleRect9.getFrameSize());
//			} 
//			else {
//				topMessage.resetTopMessageNotifyInfo();
//			}
		}
	}

	private void drawScroolBar(MilkGraphics g) {
		int allMessageHeight = getAllMessageHeight();
		int maxScroolY = allMessageHeight - (roomRectH);
		if (maxScroolY <= 0)
			return;

		int barTotalX = roomRectX + roomRectW - 6;

		int barTotalH = roomRectH;
		int barTotalW = 6;

		int focusBarH = barTotalH - barTotalH * maxScroolY / allMessageHeight;
		int maxFocusBarY = barTotalH - focusBarH;
		int focusBarY;
		int absScroolY = Math.abs(scrollY);
		if (absScroolY == 0) {
			focusBarY = roomRectY;
		} else if (absScroolY == maxScroolY) {
			focusBarY = maxFocusBarY + roomRectY;
		} else {
			focusBarY = maxFocusBarY * absScroolY / maxScroolY + roomRectY;
		}

		g.setColor(0x61482c);
		g.fillRoundRect(barTotalX + 1, focusBarY + 1, barTotalW - 1,
				focusBarH - 1, 4, 4);
	}

	private int toScreenY(int y) {
		return y + scrollY + roomRectY;
	}

	private void calculateScrollY() {
		int focusMsgBottomY = getFocusMessageBottomY();
		int focusMsgScreenBottomY = toScreenY(focusMsgBottomY);
		int focusMsgScreenTopY = focusMsgScreenBottomY- getMessageHeight(focus);

		if (focusMsgScreenTopY < roomRectY) {
			scrollY += (roomRectY - focusMsgScreenTopY);
		} else if (focusMsgScreenBottomY > roomRectY + roomRectH) {
			scrollY -= (focusMsgScreenBottomY - (roomRectY + roomRectH));
		}
		setScrollYSafely(scrollY);
	}
	
	private void setMaxScrollY() {
		int allMessageHeight = getAllMessageHeight();
		if (allMessageHeight <= roomRectH) {
			scrollY = 0;
			return;
		}
		scrollY=-(allMessageHeight-roomRectH);
	}

	private int getFocusMessageBottomY() {
		int focusBottomY = 0;
		for (int i = 0; i <= focus; i++) {
			focusBottomY += getMessageHeight(i)+msgDisplayY;
		}
		return focusBottomY+2*msgDisplayY;
	}

	private void setScrollYSafely(int newScrollY) {
		int allMessageHeight = getAllMessageHeight();
		if (allMessageHeight < roomRectH) {
			scrollY = 0;
			return;
		}
		scrollY = newScrollY;
		if (scrollY > 0) {
			scrollY = 0;
		} else if (scrollY < -(allMessageHeight - roomRectH)) {
			scrollY = -(allMessageHeight - roomRectH);
		}
	}

	boolean lockChatHallScreen() {
		if (faceScreen.isPopup()||friendScreen.isPopup()) {
			return true;
		}
		return false;
	}

	boolean doRightSoftKey() {
		if (faceScreen.isPopup()) {
			faceScreen.hideScreen();
			return true;
		}
		return false;
	}

	void handleKeyEvent(MKeyEvent key) {
		int keyType = key.getType();
		int keyCode = key.getCode();
		if (faceScreen.isPopup()) {
			if (keyType == Adaptor.KEYSTATE_PRESSED) {
				faceScreen.keyPressed(keyCode);
			}
			return;
		}
		if (keyType == Adaptor.KEYSTATE_PRESSED) {
			switch (keyCode) {
			case Adaptor.KEY_FIRE:
				keyFire();
				break;
			case Adaptor.KEY_MENU:
				if (inputBox != null) {
					if (inputBox == focusObject) {
						inputBox.setTarget(toId,toName);
					} else {
						boolean eat = keyFire();
						if (!eat) {
							inputBox.setTarget(toId,toName);
							inputBox.showEdit();
						}
					}
				}
				break;
			case Adaptor.KEY_UP:
				moveFocusUp();
				break;
			case Adaptor.KEY_DOWN:
				moveFocusDown();
				break;
			}
		}
	}

	private boolean keyFire() {
		if (focusObject instanceof AndroidInputBox) {
			inputBox.setTarget(toId, toName);
			return false;
		} 
		else if (focusObject instanceof Message) {
			Message message = (Message) focusObject;
			if (message.getCreateTime() != 0) {
				MenuItem menu[] = HallAccess.getCoreListener().getAndroidMessageMenuItems(message);
				if (menu != null) {
					int toId = message.getPopMenuUserId();
					String toName = message.getPopMenuUserName();
					uiListener.notifyShowPopMenu(Def.popTitleWantTo, menu,
							toId, toName, null);
				}
			}
		}
		return false;
	}


	void openChatInput() {
		inputBox.setMsgTo(null);
		setCurrenFocusObject(inputBox);
	}
	
	void openWordChatInput(String toName) {
		if (inputBox != null) {
			String msgTo=HallAccess.getCoreListener().getL10nString("To {0}:", toName)+'\n';
			inputBox.setMsgTo(msgTo);
			inputBox.setInitInputText("");	
			this.setCurrenFocusObject(inputBox);
		}
	}

	void openTopMessageChatInput() {
		inputBox.setMsgTo(null);
		inputBox.setInitInputText("");
		this.setCurrenFocusObject(inputBox);
	}

	private long fingerPressedTime;
	private int pointPressedY, pointDraggedY;
	private int scrollYSpeed;

	boolean pointerPressed(int x, int y) {
		if (faceScreen.isPopup()) {
			if (inputBox != null & inputBox.isTouchFaceIcon(x, y)) {
				faceScreen.hideScreen();
			} 
			else
			if (inputBox != null & inputBox.isTouchTopSendIcon(x, y)) {
				faceScreen.hideScreen();
				inputBox.handleTopSend();
			} 
			else
			if (inputBox != null & inputBox.isTouchSendIcon(x, y)) {
				faceScreen.hideScreen();
				inputBox.handleSend();
			} 
			else
			if (inputBox != null & inputBox.isTouchFriendIcon(x, y)) {
				faceScreen.hideScreen();
				inputBox.handleFriend();
			} 
			else{
				return faceScreen.pointerPressedFaceScreen(x, y);
			}
			return false;
		}
		if (friendScreen.isPopup()) {
			if (inputBox != null & inputBox.isTouchFriendIcon(x, y)) {
				friendScreen.hideScreen();
			} else if (inputBox != null & inputBox.isTouchFaceIcon(x, y)) {
				friendScreen.hideScreen();
				inputBox.handleFace();
			} 
			else
			if (inputBox != null & inputBox.isTouchTopSendIcon(x, y)) {
				friendScreen.hideScreen();
				inputBox.handleTopSend();
			} 
			else
			if (inputBox != null & inputBox.isTouchSendIcon(x, y)) {
				friendScreen.hideScreen();
				inputBox.handleSend();
			}
			else {
				return friendScreen.pointerPressedFriendScreen(x, y);
			}
			return false;
		}
		if (inputBox != null) {
			if (inputBox.isTouch(x, y)) {
				if (focusObject != inputBox)
					setCurrenFocusObject(inputBox);
				return false;
			}
			inputBox.pointerPressed(x, y);
		}
		if (isInMessageArea(x, y)) {
			pointDraggedY = 0;
			scrollYSpeed = 0;
			pointPressedY = y;
			fingerPressedTime = System.currentTimeMillis();
		}
       return false;
	}
	
	private void handleMesssagePointerEvent(int x,int y){
		if (topMessage != null&&topMessage.getCreateTime()!=0) {
			int rectH=topMessage.getTopMessageRectHeight();
//			int topMsgH = topMessage.getDrawHeight();
			int bubbleW = topMessage.getBubbleWidth();
			int bubbleH = topMessage.getBubbleHeight();
			int offsetY=(rectH-topMessage.getDrawHeight())/2;
			int messageRectX=roomRectX + messageSpace / 2+4;
			int msgX=messageRectX;
			int focusRectW=roomRectW - 2 * messageSpace;
			if (topMessage.isSendByMyself()) {
				msgX=roomRectX+focusRectW-bubbleW-messageRectX;
			}
			if (Utils.pointInRect(x, y, msgX, roomRectY+offsetY+topMessage.getTitleHeight(),bubbleW, bubbleH)) {
				setCurrenFocusObject(topMessage);
				keyFire();
				Utils.info("--------handleMesssagePointerEvent topMessage-----");
				return;
			}
		}
		if (isInMessageArea(x, y)) {
			int drawY = scrollY + roomRectY+AndroidRoom.msgDisplayY;
			for (int i = 0; i < messageQueue.size(); i++) {
				Message message = messageQueue.elementAt(i);
				boolean sendMyself=message.isSendByMyself();
				
				int focusH = message.getDrawHeight();
				int bubbleW = message.getBubbleWidth();
				int bubbleH = message.getBubbleHeight();
				int msgLeftX=roomRectX;
				if(sendMyself){
					msgLeftX=roomRectX+roomRectW-8-bubbleW;
				}
				
				if (Utils.pointInRect(x, y, msgLeftX, drawY+message.getTitleHeight(), bubbleW, bubbleH)) {
					this.focus = i;
					calculateScrollY();
					setCurrenFocusObject(message);
					MenuItem menu[] = HallAccess.getCoreListener().getMessageMenuItems(message);
					if (menu != null){
						int toId = message.getPopMenuUserId();
						String toName = message.getPopMenuUserName();
					    uiListener.notifyShowPopMenu(Def.popTitleWantTo, menu,toId, toName,null);
					}
					return;
				}
				drawY += focusH+msgDisplayY;
			}

		}
	}

	private int getAllMessageHeight() {
		int height = 0;
		for (int i = messageQueue.size() - 1; i >= 0; i--) {
			Message chat = messageQueue.elementAt(i);
			height += chat.getDrawHeight()+msgDisplayY;
		}
		return height+msgDisplayY;
	}

	void pointerDragged(int x, int y) {
		if (isInMessageArea(x, y)) {
			int initSpeed = 0;
			if (pointDraggedY == 0) {
				pointDraggedY = y;
				initSpeed = pointDraggedY - pointPressedY;
			} else {
				initSpeed = y - pointDraggedY;
				pointDraggedY = y;
			}
			this.setScrollYSafely(this.scrollY + initSpeed);
		}
	}

	void pointerReleased(int x, int y) {
		if (isInMessageArea(x, y) && pointDraggedY != 0) {
			long timeTake = (System.currentTimeMillis() - fingerPressedTime);
			if (timeTake < 2000) {
				int dy = y - pointPressedY;
				int speedY = -(int) (dy * 1000 / timeTake);
				pointDraggedY = 0;
				if (Math.abs(speedY / 5) >= 20) {
					scrollYSpeed = speedY / 5;
				}
			}
		}
		handleMesssagePointerEvent(x,y);
	}

	void update() {
		if (scrollYSpeed != 0) {
			int realySpeed = scrollYSpeed / 5;
			setScrollYSafely(this.scrollY - realySpeed);
			if (scrollYSpeed > 0)
				scrollYSpeed -= 5;
			else {
				scrollYSpeed += 5;
			}
			if (Math.abs(scrollYSpeed) <= 5) {
				scrollYSpeed = 0;
			}
		}
//		if(this.inputBox!=null){
//			inputBox.checkFocus();
//		}
	}

	private boolean isInMessageArea(int x, int y) {
		return Utils.pointInRect(x, y, roomRectX, roomRectY, roomRectW,roomRectH);
	}

	private void moveFocusUp() {
		if (focusObject instanceof AndroidInputBox) {
			if (messageQueue.size() > 0) {
				focusLastMessage();
			} else if (topMessage != null && focusObject != topMessage) {
				setCurrenFocusObject(topMessage);
			}
		} else if (focus > 0) {
			focus--;
			calculateScrollY();
			focusMessageChange();
		} else if (topMessage != null && focusObject != topMessage) {
			setCurrenFocusObject(topMessage);
		}
	}

	private void focusMessageChange() {
		if (messageQueue.size() == 0) {
			setCurrenFocusObject(null);
		} else {
			Message chat = messageQueue.elementAt(this.focus);
			setCurrenFocusObject(chat);
		}
	}

	private void focusLastMessage() {
		if (messageQueue.size() > 0) {
			focus = messageQueue.size() - 1;
			calculateScrollY();
			focusMessageChange();
		}
	}

	private void moveFocusDown() {
		if (focusObject instanceof AndroidInputBox) {
			return;
		} else if (topMessage != null && focusObject == topMessage
				&& messageQueue.size() > 0) {
			focus = 0;
			calculateScrollY();
			focusMessageChange();
		} else if (focus < messageQueue.size() - 1) {
			focus++;
			calculateScrollY();
			focusMessageChange();
		} 
		else if (inputBox != null) {
			setCurrenFocusObject(inputBox);
		}
	}

	private int getMessageHeight(int msgIndex) {
		Message chat = messageQueue.elementAt(msgIndex);
		return chat.getDrawHeight();
	}

	public void focusItemChange(int itemId, String itemName) {
		if (this.roomType == Def.CHAT_TYPE_PRIVATE) {
			toId = itemId;
			toName = itemName;
			if (friendScreen != null) {
				friendScreen.addItem(itemName, itemId);
			}
			if (inputBox != null) {
				inputBox.setTarget(toId, toName);
			}
//			Utils.info("------------focusItemChange--------name-"+itemName);
		}
	}

	void showBBInputEdit() {
		if (inputBox!=null) {
			inputBox.showEdit();
		}
	}

	private void setCurrenFocusObject(Object newFocus) {
		if (newFocus instanceof AndroidInputBox) {
			((AndroidInputBox) newFocus).setFocus(true);
			if (!(focusObject instanceof AndroidInputBox) && focusObject != null)
				inputBox.showEdit();
		} 
		else if (newFocus instanceof Message) {
			if (inputBox != null) {
				inputBox.setFocus(false);
				if (focusObject == inputBox)
					AndroidInputBox.hideEdit();
			}
			if(newFocus==this.topMessage){
				focus=0;
			}
		}
		focusObject = newFocus;
	}
	
	public void showTopMessage(Message message){
		this.topMessage = message;
	}

	public void showMessage(Message message) {
		if (message.getType() == Def.CHAT_TYPE_WORLD_TOP) {
			this.topMessage = message;
		} else {
			synchronized (messageQueue) {
				messageQueue.addElement(message);
				if (messageQueue.size() >= MAX_MESSAGE_QUEUE_LENGTH) {
					focus = messageQueue.size() - 1;
					Message temp = (Message) messageQueue.elementAt(0);
					messageQueue.removeElementAt(0);
					if (temp != null) {
						temp.destoryMessage();
						temp = null;
					}
					setMaxScrollY();
				} else if (focusObject instanceof Message
						&& focusObject != topMessage) {
					if (focusObject != inputBox)
						moveFocusDown();
				} else {// focusObject==inputBox
					focus = messageQueue.size() - 1;
					setMaxScrollY();
				}
			}
		}
	}

}

package milk.ui.j2mechat;

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
import milk.ui.j2mechat.J2meNativeInput;

public class J2meRoom implements J2meSelectBoxListener {
	
	private static final int MAX_MESSAGE_QUEUE_LENGTH = 30;
	private static final int fillNormalRoomColor = 0xac8a59;
	private static final int fillFocusMessageBackColor = 0xdf963b;
	private static final int messageSpace = 6;
	private static final int inputRectSpace = 3;
	
	private int focus = 0;
	private int scrollY = 0;
	private int roomRectX, roomRectY, roomRectW, roomRectH;
	private int inputBoxHeight;
	
	private int currentChatUserId = -1;
	private String currentChatUserName;
	private J2meInputBox inputBox;
	private J2meSelectBox selectBox;

	private static J2meNativeInput chatInput;
	private Object focusObject;
//	public static FaceScreen bbFace;
	private static UIListener chatListener;
	private static MilkApp factory;
	
	private Vector messageQueue;
	private Message topMessage;
	private byte roomType;
	
	public static void setChatListener(UIListener l) {
		chatListener = l;
	}

	public J2meRoom(MilkApp app, byte type) {
		factory = app;
		messageQueue = new Vector(MAX_MESSAGE_QUEUE_LENGTH);
		scrollY = 0;
		roomType = type;
		switch (roomType) {
		case Def.CHAT_TYPE_PRIVATE:
			inputBox = new J2meInputBox(factory, roomType);
			selectBox = new J2meSelectBox(factory);
			break;
		case Def.CHAT_TYPE_FAMILY:
			inputBox = new J2meInputBox(factory, roomType);
			this.currentChatUserId = HallAccess.myFamilyId;
			this.currentChatUserName = HallAccess.myFamilyName;
			break;
		case Def.CHAT_TYPE_WORLD:
			inputBox = new J2meInputBox(factory, roomType);
		
			break;
		case Def.CHAT_TYPE_SYSTEM:
			break;
		default:
			throw new IllegalArgumentException("ChatRoom type:" + roomType);
		}
		if (chatInput == null//&&!isBBDevice()
				) {
			chatInput = new J2meNativeInput();
		}
		if (selectBox != null) {
			selectBox.setItemSelectBoxListener(this);
		}
//		if (bbFace == null && isBBDevice()) {
//			bbFace = HallScreen.faceScreen;
//		}
		setCurrenFocusObject(null);
	}



//	private static boolean isBBDevice() {
//		return !factory.getPlatform().equals("J2ME");
//	}

//	public void hideOrShowEdit() {
//		if (inputBox != null) {
//			if (inputBox == this.focusObject) {
//				inputBox.showEdit();
//			}
//		} else {
//			InputBox.hideEdit();
//			Utils.info("-------------------hide edit");
//		}
//	}

	void setChatUser(int id, String name) {
		currentChatUserId = id;
		currentChatUserName = name;
		if (selectBox != null) {
			selectBox.addItem(name, id);
		}
//		if (inputBox != null) {
//			inputBox.setTarget(currentChatUserId, currentChatUserName);
//		}
	}

	public void initRoom(int x, int y, int w, int h, int inputHeight) {

		roomRectX = x;
		roomRectY = y;
		roomRectW = w;
		roomRectH = h;
		inputBoxHeight = inputHeight;

		int inputRectY = roomRectY + roomRectH + inputRectSpace - 1;
		if (inputBox != null) {
			inputBox.initInputBox(x, inputRectY, w, inputBoxHeight);
			setCurrenFocusObject(inputBox);
		} else {
			roomRectH = h + inputBoxHeight;
		}

		if (selectBox != null) {
			int selectY = inputRectY - inputBoxHeight - 3;
			selectBox.initSelectBox(x + w - J2meSelectBox.DEFAULT_FRAME_WIDTH,
					selectY, J2meSelectBox.DEFAULT_FRAME_WIDTH, inputBoxHeight);
			roomRectH -= inputBoxHeight + 3;
		} else {
			if (Def.CHAT_TYPE_SYSTEM == roomType) {
				roomRectH -= inputBoxHeight;
			}
		}
		firstLayoutTopMessage = true;
	}

	public void showNotify() {
		if (inputBox != null) {
			setCurrenFocusObject(inputBox);
		} else {
			focusLastMessage();
		}
		if (selectBox != null) {
			selectBox.setFocus(false);
		}
		if (currentChatUserId == -1 && roomType == Def.CHAT_TYPE_PRIVATE) {
			if (selectBox != null && selectBox.isEmpty()) {
				selectBox.initUserQueue(messageQueue);
			}
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


	static void initL10nString() {
		if (chatInput != null)
			chatInput.initL10nString();
	}

	public void hideNotify() {
		scrollYSpeed = 0;
	}

  
	public Message removeMessage(String id) {
		for (int i = messageQueue.size() - 1; i >= 0; i--) {
			Message chat = (Message) messageQueue.elementAt(i);
			if (id.equals(chat.getMessageId())) {
				messageQueue.removeElement(chat);
				// System.out.println(" room->remove Message id=" + id);
				return chat;
			}
		}
		return null;
	}

	void draw(MilkGraphics g) {
		if (firstLayoutTopMessage && roomType == Def.CHAT_TYPE_WORLD) {
			Message.setLineHeight(g.getFont().getHeight());
			int topMessageH = Message.getTopMessageHeight();
			roomRectY = roomRectY + topMessageH;
			roomRectH -= topMessageH;
			firstLayoutTopMessage = false;
		}
		g.setColor(fillNormalRoomColor);
		g.fillRoundRect(roomRectX, roomRectY, roomRectW, roomRectH, 8, 8);
		g.fillRect(roomRectX, roomRectY, roomRectW, 30);
		int messageRectW = roomRectW - 2 * messageSpace;
		int messageRectX = roomRectX + messageSpace / 2;

		boolean isLanguageAr=HallAccess.isLanguageAr;
		drawTopMessage(g, messageRectW, messageRectX,isLanguageAr);

		drawAllMessage(g, messageRectW, messageRectX,isLanguageAr);

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
		if (selectBox != null) {
			selectBox.draw(g);
		}

		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());

		drawScroolBar(g);
//
//		if (isBBDevice()) {
//			if (bbFace.isPopup()) {
//				bbFace.drawBBFace(g);
//			}
//		}
	}

	private void drawAllMessage(MilkGraphics g, int messageRectW,
			int messageRectX,boolean isLanguageAr) {
		int messageRectY = scrollY + roomRectY;
		g.setClip(roomRectX + 1, roomRectY + 1, roomRectW - 2, roomRectH - 2);

		for (int i = 0; i < messageQueue.size(); i++) {
			Message chat = (Message) messageQueue.elementAt(i);
			boolean isFous = (focus == i && focusObject instanceof Message && focusObject != topMessage);
			int messageH = chat.getDrawHeight();
			if (isFous) {
				g.setColor(fillFocusMessageBackColor);
				int focusRectY = messageRectY + 1;
				int focusRectX = roomRectX + 1;
				int focusRectW = roomRectW - 2;
				int focusRectH = messageH - 1;
				if (i == messageQueue.size() - 1
						&& getAllMessageHeight() > roomRectH) {
					g.fillRoundRect(focusRectX, focusRectY, focusRectW,
							focusRectH, 8, 8);
					g.fillRect(focusRectX, focusRectY, focusRectW, 12);
				} else {
					g.fillRect(focusRectX, focusRectY, focusRectW, focusRectH);
				}
			}

			if (messageRectY + messageH > roomRectY
					&& messageRectY < roomRectY + roomRectH) {
				chat.draw(g, messageRectX, messageRectY, messageRectW, isFous,isLanguageAr);
			}
			messageRectY += chat.getDrawHeight();
		}
	}

	private static boolean firstLayoutTopMessage = true;

	private void drawTopMessage(MilkGraphics g, int messageRectW,
			int messageRectX,boolean isLanguageAr) {
		if (topMessage != null) {
			int topMessageH = topMessage.getDrawHeight();
			boolean isFousTopMessage = (focus == 0 && focusObject == topMessage);
			int focusRectY = roomRectY - topMessageH;
			int focusRectX = roomRectX;
			int focusRectW = roomRectW;
			int focusRectH = topMessageH;

			if (!isFousTopMessage) {
				g.setColor(0x8d6b4b);
			} else {
				g.setColor(fillFocusMessageBackColor);
			}

			g.fillRect(focusRectX, focusRectY, focusRectW, focusRectH);
			g.setClip(focusRectX + 1, focusRectY + 1, focusRectW - 2,
					focusRectH - 2);

			if (topMessage.getCreateTime() == 0) {
				topMessage.drawTopMessageNotifyInfo(g, messageRectX,
						focusRectY, messageRectW, topMessageH);
			} else {
				if (Message.getTopMessageValidTimeSecond() > 0) {
					topMessage.draw(g, messageRectX, focusRectY, messageRectW,
							isFousTopMessage,isLanguageAr);
				} else {
					topMessage.resetTopMessageNotifyInfo();
				}
			}
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
		int focusMsgScreenTopY = focusMsgScreenBottomY
				- getMessageHeight(focus);

		if (focusMsgScreenTopY < roomRectY) {
			scrollY += (roomRectY - focusMsgScreenTopY);
		} else if (focusMsgScreenBottomY > roomRectY + roomRectH) {
			scrollY -= (focusMsgScreenBottomY - (roomRectY + roomRectH));
		}
		setScrollYSafely(scrollY);
	}

	private int getFocusMessageBottomY() {
		int focusBottomY = 0;
		for (int i = 0; i <= focus; i++) {
			focusBottomY += getMessageHeight(i);
		}
		return focusBottomY;
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
		if (focusObject instanceof J2meSelectBox) {
			return ((J2meSelectBox) focusObject).isShownSelectBox();
		}
//		if (isBBDevice() && bbFace.isPopup()) {
//			return true;
//		}
		return false;
	}

	boolean doRightSoftKey() {
//		if (isBBDevice() && bbFace.isPopup()) {
//			bbFace.hideFaceScreen();
//			return true;
//		}
		if (focusObject instanceof J2meSelectBox) {
			if (((J2meSelectBox) focusObject).doRightSoftKey()) {
				return true;
			}
		}
		return false;
	}

	void handleKeyEvent(MKeyEvent key) {
		if (focusObject instanceof J2meSelectBox) {
			J2meSelectBox box = (J2meSelectBox) focusObject;
			if (box.isShownSelectBox()) {
				box.handleKeyEvent(key);
				return;
			}
		}
		int keyType = key.getType();
		int keyCode = key.getCode();
//		if (isBBDevice() && bbFace.isPopup()) {
//			if (keyType == Adaptor.KEYSTATE_PRESSED) {
//				bbFace.keyPressed(keyCode);
//			}
//			return;
//		}
		if (keyType == Adaptor.KEYSTATE_PRESSED) {
			switch (keyCode) {
			case Adaptor.KEY_FIRE:
				keyFire();
				break;
//			case Adaptor.KEY_MENU:
//				if (isBBDevice() && inputBox != null) {
//					if (inputBox == focusObject) {
//						inputBox.setTarget(currentChatUserId,
//								currentChatUserName);
//						inputBox.showPopMenu();
//					} else {
//						boolean eat = keyFire();
//						if (!eat) {
//							inputBox.setTarget(currentChatUserId,
//									currentChatUserName);
//							inputBox.showEdit();
//							inputBox.showPopMenu();
//						}
//					}
//				}
//				break;
			case Adaptor.KEY_UP:
				moveFocusUp();
				break;
			case Adaptor.KEY_DOWN:
				moveFocusDown();
				break;
			case Adaptor.KEY_NUM9:
				if (selectBox != null) {
					setCurrenFocusObject(selectBox);
					selectBox.keyFire();
				}
				break;
			case Adaptor.KEY_NUM5:
				if (inputBox != null) {
//					if (isBBDevice()) {
//						inputBox.setTarget(currentChatUserId,
//								currentChatUserName);
//					} else {
						setCurrenFocusObject(inputBox);
						openChatInput();
//					}
				}
				break;
			case Adaptor.KEY_NUM6:
				if (roomType == Def.CHAT_TYPE_WORLD //&& !isBBDevice()
				) {
					openTopMessageChatInput();
				}
				break;
			}
		}
	}

	private boolean keyFire() {
		if (focusObject instanceof J2meInputBox) {
//			if (isBBDevice()) {
//				inputBox.setTarget(currentChatUserId, currentChatUserName);
//				return false;
//			} else {
				openChatInput();
//			}
		} else if (focusObject instanceof J2meSelectBox) {
			((J2meSelectBox) focusObject).keyFire();
		} else if (focusObject instanceof Message) {
			Message message = (Message) focusObject;
			MenuItem menu[] = HallAccess.getCoreListener().getMessageMenuItems(message);
			if (menu != null){
				int toId = message.getPopMenuUserId();
				String toName = message.getPopMenuUserName();
			    chatListener.notifyShowPopMenu(Def.popTitleWantTo, menu,toId, toName,null);
			}
		}
		return false;
	}


	void openChatInput() {
//		if (isBBDevice()) {
//			setCurrenFocusObject(inputBox);
//		} else 
			if (chatInput != null && this.inputBox != null) {
			if (currentChatUserId == -1 && roomType == Def.CHAT_TYPE_PRIVATE) {
//				PopMenu.getInstance().showPopNotifyInfo(Def.chatErrorNoTarget);
				chatListener.notifyShowAlertInfo(Def.chatErrorNoTarget);
				return;
			} else if (roomType == Def.CHAT_TYPE_FAMILY) {
				if (HallAccess.myFamilyId <= 0) {
//					PopMenu.getInstance().showPopNotifyInfo(Def.chatErrorNoTribe);
					chatListener.notifyShowAlertInfo(Def.chatErrorNoTribe);
					return;
				} else {
					currentChatUserId = HallAccess.myFamilyId;
					currentChatUserName = HallAccess.myFamilyName;
				}
			}
			chatInput.setChatListener(HallAccess.getCoreListener());
			chatInput.deleteAll();
			chatInput.openInputPage(roomType, currentChatUserId,
					currentChatUserName);
		}
		// else {
		// throw new NullPointerException("chatInput=null");
		// }
	}

//	private MArray array = new MArray();

	void openWordChatInput(String toName) {
//		if (isBBDevice()) {// bb chu
//			if (inputBox != null) {
//				inputBox.setInitInputText(HallAccess.getCoreListener().getL10nString("To {0}:", toName));	
//				this.setCurrenFocusObject(inputBox);
//			}
//		} else 
			if (chatInput != null) {
			chatInput.setChatListener(HallAccess.getCoreListener());
			chatInput.deleteAll();
			chatInput.openInputPage(Def.CHAT_TYPE_WORLD, toName);
		} else {
			throw new NullPointerException("chatInput=null");
		}
	}

	void openTopMessageChatInput() {
//		if (isBBDevice()) {
//			inputBox.setInitInputText("");
//			this.setCurrenFocusObject(inputBox);
//		} else 
			if (chatInput != null) {
			chatInput.setChatListener(HallAccess.getCoreListener());
			chatInput.deleteAll();
			chatInput.openInputPage(Def.CHAT_TYPE_WORLD_TOP, currentChatUserId,
					currentChatUserName);
		} else {
			throw new NullPointerException("chatInput=null");
		}
	}

	private long fingerPressedTime;
	private int pointPressedY, pointDraggedY;
	private int scrollYSpeed;

	void pointerPressed(int x, int y) {
		if (inputBox != null) {
//			if (isBBDevice() && bbFace.isPopup()) {
//				bbFace.pointerPressed(x, y);
//				return;
//			}
			if (inputBox.isTouch(x, y)) {
//				if(isBBDevice()){
//					if (focusObject != inputBox)
//						setCurrenFocusObject(inputBox);
//					return;
//				}
				openChatInput();
				if (this.focusObject != inputBox)
					setCurrenFocusObject(inputBox);
				return;
			}
		}
		if (selectBox != null) {
			boolean handle = selectBox.pointerPressed(x, y);
			if (handle) {
				setCurrenFocusObject(selectBox);
				return;
			}

		}
		if (topMessage != null) {
			int topMsgH = topMessage.getDrawHeight();
			if (Utils.pointInRect(x, y, roomRectX, roomRectY - topMsgH,
					roomRectW, topMsgH)) {
				setCurrenFocusObject(topMessage);
				keyFire();
				return;
			}
		}

		if (isInMessageArea(x, y)) {

			int drawY = scrollY + roomRectY;
			for (int i = 0; i < messageQueue.size(); i++) {
				Message message = (Message) messageQueue.elementAt(i);
				int focusH = message.getDrawHeight();
				if (Utils.pointInRect(x, y, roomRectX, drawY, roomRectW, focusH)) {
					this.focus = i;
					calculateScrollY();
					setCurrenFocusObject(message);
					MenuItem menu[] = HallAccess.getCoreListener().getMessageMenuItems(message);
					if (menu != null){
						int toId = message.getPopMenuUserId();
						String toName = message.getPopMenuUserName();
					    chatListener.notifyShowPopMenu(Def.popTitleWantTo, menu,toId, toName,null);
					}
					return;
				}
				drawY += focusH;
			}
			if (selectBox != null && selectBox.isShownSelectBox()) {
				return;
			}
			pointDraggedY = 0;
			scrollYSpeed = 0;
			pointPressedY = y;
			fingerPressedTime = System.currentTimeMillis();

		}
	}

	private int getAllMessageHeight() {
		int height = 0;
		for (int i = messageQueue.size() - 1; i >= 0; i--) {
			Message chat = (Message) messageQueue.elementAt(i);
			height += chat.getDrawHeight();
		}
		return height;
	}

	void pointerDragged(int x, int y) {
		if (isInMessageArea(x, y)) {
			int dy = 0;
			if (pointDraggedY == 0) {
				pointDraggedY = y;
				dy = pointDraggedY - pointPressedY;
			} else {
				dy = y - pointDraggedY;
				pointDraggedY = y;
			}
			this.setScrollYSafely(this.scrollY + dy);
		}
	}

	void pointerReleased(int x, int y) {
		if (isInMessageArea(x, y) && pointDraggedY != 0) {
			long timeTake = (System.currentTimeMillis() - fingerPressedTime);
			if (timeTake < 2000) {
				int dy = y - pointPressedY;
				int speedY = -(int) (dy * 1000 / timeTake);
				pointDraggedY = 0;
				if (Math.abs(speedY / 10) >= 20) {
					scrollYSpeed = speedY / 10;
				}
			}
		}
	}

	void update() {
		if (scrollYSpeed != 0) {
			int realySpeed = scrollYSpeed / 10;
			setScrollYSafely(this.scrollY - realySpeed);
			if (scrollYSpeed > 0)
				scrollYSpeed -= 5;
			else {
				scrollYSpeed += 5;
			}
			if (Math.abs(scrollYSpeed) <= 3) {
				scrollYSpeed = 0;
			}
		}
	}

	private boolean isInMessageArea(int x, int y) {
		return Utils.pointInRect(x, y, roomRectX, roomRectY, roomRectW,
				roomRectH);
	}

	private void moveFocusUp() {
		if (focusObject instanceof J2meInputBox) {
			if (selectBox != null) {
				setCurrenFocusObject(selectBox);
			} else {
				if (messageQueue.size() > 0) {
					focusLastMessage();
				} else if (topMessage != null && focusObject != topMessage) {
					setCurrenFocusObject(topMessage);
				}
			}
		} else if (focusObject instanceof J2meSelectBox) {
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
			Message chat = (Message) messageQueue.elementAt(this.focus);
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
		if (focusObject instanceof J2meInputBox) {
			return;
		} else if (focusObject instanceof J2meSelectBox) {
			if (inputBox != null) {
				setCurrenFocusObject(inputBox);
			}
		} else if (topMessage != null && focusObject == topMessage
				&& messageQueue.size() > 0) {
			focus = 0;
			calculateScrollY();
			focusMessageChange();
		} else if (focus < messageQueue.size() - 1) {
			focus++;
			calculateScrollY();
			focusMessageChange();
		} else if (selectBox != null) {
			setCurrenFocusObject(selectBox);
		} else if (inputBox != null) {
			setCurrenFocusObject(inputBox);
		}
	}

	private int getMessageHeight(int msgIndex) {
		Message chat = (Message) messageQueue.elementAt(msgIndex);
		return chat.getDrawHeight();
	}

	public void focusItemChange(int itemId, String itemName) {
		if (this.roomType == Def.CHAT_TYPE_PRIVATE) {
			currentChatUserId = itemId;
			currentChatUserName = itemName;
			if (selectBox != null) {
				selectBox.addItem(itemName, itemId);
			}
//			if (inputBox != null) {
//				inputBox.setTarget(currentChatUserId, currentChatUserName);
//			}
		}
	}

//	void showBBInputEdit() {
//		if (isBBDevice()) {
//			inputBox.showEdit();
//		}
//	}

	private void setCurrenFocusObject(Object newFocus) {
		if (newFocus instanceof J2meInputBox) {
			((J2meInputBox) newFocus).setFocus(true);
//			if (!(focusObject instanceof InputBox) && focusObject != null)
//				inputBox.showEdit();
			if (selectBox != null)
				selectBox.setFocus(false);
		} else if (newFocus instanceof J2meSelectBox) {
			((J2meSelectBox) newFocus).setFocus(true);
			if (inputBox != null && focusObject == inputBox) {
				inputBox.setFocus(false);
//				InputBox.hideEdit();
			}
		} else if (newFocus instanceof Message) {
			if (selectBox != null)
				selectBox.setFocus(false);
			if (inputBox != null) {
				inputBox.setFocus(false);
//				if (focusObject == inputBox)
//					InputBox.hideEdit();
			}
		}
		focusObject = newFocus;
	}
	
	public void showTopMessage(Message message){
		this.topMessage = message;
	}

	public void showMessage(Message message){
		if (message.getType() == Def.CHAT_TYPE_WORLD_TOP) {
			this.topMessage = message;
		} else {
			messageQueue.addElement(message);
			if (messageQueue.size()>= MAX_MESSAGE_QUEUE_LENGTH) {
				Message temp=(Message)messageQueue.elementAt(0);
				messageQueue.removeElementAt(0);
				if (temp != null) {
					temp.destoryMessage();
					temp = null;
				}
				calculateScrollY();
			} else if (focusObject instanceof Message
					&& focusObject != topMessage) {
				if (focusObject != inputBox)
					moveFocusDown();
			} else if (focusObject != inputBox) {
				if (focus + 1 <= messageQueue.size() - 1)
					focus++;
				if (focusObject == null) {
					setCurrenFocusObject(messageQueue.elementAt(focus));
				}
				calculateScrollY();
			} else {// focusObject==inputBox
				focus = messageQueue.size() - 1;
				calculateScrollY();
			}
		}
	}

}

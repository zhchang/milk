package milk.chat.core;

import milk.chat.port.CoreListener;
import milk.chat.port.UIListener;
import milk.chat.port.MsgListener;
import milk.implement.Adaptor;
import milk.implement.mk.MArray;
import milk.net.InChatMessage;
import milk.net.InChatResponseMessage;
import milk.net.InDynamicPayMessage;
import milk.net.InMessage;
import milk.net.OutChatMessage;
import milk.net.OutDynamicPayMessage;
import milk.ui2.MilkApp;

public final class HallCore implements CoreListener, PopMenuListener,
		ChatListener {
	
	private boolean debug=false;
	
	private int msgSeq;
	private FaceCoreHandler faceHandler;
	private UIListener uiListener;
	private MenuItemManager menuItemManager;
	private int messageWidth;
	private int topMessageWidth;
	
	private MsgListener msgListener;
	private static MilkApp milkApp;

	public HallCore() {
		faceHandler = new FaceCore();
		Message.initFaceHandler(faceHandler);
	}

	static void setMilkApp(MilkApp milkApp) {
		HallCore.milkApp = milkApp;
	}
	
	public void setDebug() {
		debug = true;
	}
	
	public boolean isDebug(){
		return debug;
	}

	static String[] getTopMessageNotify() {
		if (!milkApp.getPlatform().equals("J2ME")) {
			return new String[] { Def.chatSendTopMsg };
		} else {
			return new String[] { Def.chatSendTopMsg, Def.chatInputTopMsgKey6 };
		}
	}

	public FaceCoreHandler getFaceHandler() {
		return faceHandler;
	}

	public void initListener(MsgListener ml, UIListener ul) {
		msgListener = ml;
		uiListener = ul;
	}

	public void setMessageLineWidth(int msgLineWidth) {
		this.messageWidth = msgLineWidth;
		// HallAccess.myMonetId = Adaptor.getInstance().getMonetId();
	}
	
	public void setTopMessageLineWidth(int topMessageWidth){
		this.topMessageWidth=topMessageWidth;
	}

	public MenuItem[] getInputMenuItem(byte roomType) {
		return getMenuItemManager().getInputMenuItems(roomType);
	}

	public MenuItem[] getAndroidInputMenuItem(byte roomType){
		return getMenuItemManager().getAndroidInputMenuItems(roomType);
	}
	
	public boolean showInputMenu(byte roomType, String title, int toId,
			String toName) {
		MenuItem[] menu = getInputMenuItem(roomType);
		if (menu == null) {
			return false;
		}
		uiListener.notifyShowPopMenu(title, menu, toId, toName, null);
		return true;
	}

	private MenuItemManager getMenuItemManager() {
		if (menuItemManager == null)
			menuItemManager = new MenuItemManager();
		return menuItemManager;
	}

	public void handlePopMenuEvent(MenuItem item, int userId, String userName,
			int focus) {
		String popName = item.actionName;
		if (popName.equals(actionPayForTool)) {// pay for tool
			byte action = Byte.parseByte(item.payAction);
			String param = item.payParameters;
			Adaptor.debug("send chat pay : " + param);
			OutDynamicPayMessage pay = new OutDynamicPayMessage(msgSeq, action,
					param);
			msgSeq++;
			Adaptor.milk.send(pay);
		}
//		else {
			uiListener.handlePopMenuEvent(item, userId, userName, focus);// call																// up																// //																// up
//		}
	}

	public void sendMessage(byte type, int receiveId, String receiveName,
			String content) {
		Adaptor.debug("chat to " + receiveName + "<" + receiveId + "> : "
				+ content);
		Utils.validateOutChatMessageType(type);
		OutChatMessage chat = new OutChatMessage(msgSeq, Def.CHAT_ACTION_SEND,
				type, receiveId, HallAccess.myUserName, content);
		if (!this.debug) {
			Adaptor.milk.send(chat);
		}
		msgSeq++;
		if (faceHandler != null) {
			int splitWidth=messageWidth;
			if(type == Def.CHAT_TYPE_WORLD_TOP){
				splitWidth=this.topMessageWidth;
			}
			MessageLine[] line = faceHandler.splitFaceMessage(content,
					uiListener.getFont(), splitWidth);
//			Message.setFont(uiListener.getFont());
			
			String body[]=Utils.getMessageLines(line);
			int width[]=Utils.getMessageLineWidth(line);
			boolean sendByMyself=true;
			if(this.debug){
				if(System.currentTimeMillis()%2==0){
					sendByMyself=false;
				}
			}
			int senderId = HallAccess.myMonetId;
			Message local = new Message(type, senderId, HallAccess.myUserName,
					receiveId, receiveName, body, sendByMyself, chat.getMessageId(),
					content, 10000,width);
			if (this.debug) {
				showMessage(local);
				if (type == Def.CHAT_TYPE_WORLD_TOP) {
					Message.setTopMessageValidTime(20 * 1000);
					HallAccess.showTopMessage(local);
				} else {
					HallAccess.showNormalMessage(local);
				}
			} else if (type == Def.CHAT_TYPE_WORLD_TOP
					|| type == Def.CHAT_TYPE_WORLD) {
				lastSendMessage = local;
			} else {
				showMessage(local);
			}
		} else {
			throw new NullPointerException("sendMessage face=null");
		}
	}

	private final void showMessage(Message message) {
		byte localType = message.getType();
		switch (localType) {
		case Def.CHAT_TYPE_WORLD_TOP:
			this.msgListener.receiveTopMessage(message);
			break;
		case Def.CHAT_TYPE_PRIVATE:
			msgListener.receivePrivateMessage(message);
			break;
		case Def.CHAT_TYPE_FAMILY:
			msgListener.receiveFamilyMessage(message);
			break;
		case Def.CHAT_TYPE_WORLD:
			msgListener.receiveWorldMessage(message);
			break;
		case Def.CHAT_TYPE_SYSTEM:
			msgListener.receiveSystemMessage(message);
			break;
		default:
			throw new IllegalArgumentException("HallCore showMessage msg Type:"
					+ localType);
		}
	}

	public void receiveMessage(InMessage message) {
		if (message instanceof InChatMessage) {
			receiveChatMessage((InChatMessage) message);
		} else if (message instanceof InChatResponseMessage) {
			receiveChatResponse((InChatResponseMessage) message);
		} else if (message instanceof InDynamicPayMessage) {
			receiveDynamicPayMessage((InDynamicPayMessage) message);
		}
	}

	private void receiveChatMessage(InChatMessage message) {
		InChatMessage chat = message;
		byte messageType = chat.messageType;
		String fromName = chat.senderName;
		int fromId = chat.senderId;
		boolean sendByMyself = false;
		if (messageType == Def.CHAT_TYPE_FAMILY
				&& fromId == HallAccess.myMonetId) {
			return;
		}
		if ((messageType == Def.CHAT_TYPE_WORLD || messageType == Def.CHAT_TYPE_WORLD_TOP)
				&& fromId == HallAccess.myMonetId) {
			sendByMyself = true;
		}
		String messageId = chat.messageId;
		String messageContent = chat.messageContent;

		Adaptor.debug("chat from " + fromName + "<" + fromId + "> : "
				+ messageContent);

		int duration = chat.duration;

		byte localType = Utils.translateMessageType(messageType);

		if (faceHandler != null) {
			int splitWidth=messageWidth;
			if(localType == Def.CHAT_TYPE_WORLD_TOP){
				splitWidth=this.topMessageWidth;
			}
			MessageLine line[] = faceHandler.splitFaceMessage(messageContent,
					uiListener.getFont(), splitWidth);
			String msgBody[]=Utils.getMessageLines(line);
			int width[]=Utils.getMessageLineWidth(line);
			
			int toId = HallAccess.myMonetId;
			Message local = new Message(localType, fromId, fromName, toId,
					HallAccess.myUserName, msgBody, sendByMyself, messageId,
					messageContent, duration,width);
//			Message.setFont(uiListener.getFont());
			if (messageType == Def.CHAT_TYPE_WORLD_TOP)
				HallAccess.showTopMessage(local);
			else {
				HallAccess.showNormalMessage(local);
			}
			showMessage(local);
		} else {
			throw new NullPointerException(
					"receiveMessage faceMessageHandler=null");
		}
	}

	private void receiveChatResponse(InChatResponseMessage message) {
		Adaptor.debug("chat result : " + message.result);
		if (message.result == CHAT_SEND_SUCCESS) {
			msgListener.sendMessageSuccess(message.messageId);
		} else {
			if (message.result != SEND_FAIL_NO_TOOL) {
			} else {
				if(!this.debug)
				   msgListener.sendMessageFail(message.messageId);
			}
			switch (message.result) {
			case SEND_FAIL_WRONG_ID:
				uiListener.notifyShowAlertInfo(Def.sendFailWrongId);
				break;
			case SEND_FAIL_TOO_LONG:
				uiListener.notifyShowAlertInfo(Def.sendFailTooLong);
				break;
			case SEND_FAIL_FORBIDDEN_WORD:
				uiListener.notifyShowAlertInfo(Def.sendFailForbidenWord);
				break;
			case SEND_FAIL_NO_TOOL:
				String popMenuString = message.errorMessage;
				showPayMenu(popMenuString);
				break;
			case SEND_FAIL_FLOODING:
				uiListener.notifyShowAlertInfo(Def.sendFailTooFast);
				break;
			case SEND_FAIL_LAST_TOPMESSAGE_VALID:
				long topMsgTime = Long.parseLong(message.errorMessage);
				Message.setTopMessageValidTime(topMsgTime);
				String info = getL10nString(Def.sendFailTopValid,
						(topMsgTime / 1000) + "");
				uiListener.notifyShowAlertInfo(info);
				break;
			default:
				uiListener.notifyShowAlertInfo(Def.sendFail);
				break;
			}
		}
	}

	private MArray array;

	public String getL10nString(String info, String replace) {
		if (array == null) {
			array = new MArray();
		}
		if (replace == null || replace.length() == 0) {
			return Adaptor.getInstance().getTranslation(info, null);
		}
		array.clean();
		array.append(replace);
		return Adaptor.getInstance().getTranslation(info, array);
	}

	private void receiveDynamicPayMessage(InDynamicPayMessage message) {

		int result = message.result;
		Adaptor.debug("chat pay response : " + result);
		if (result == 0) {// ok,send again
			if (lastSendMessage != null) {
				lastSendMessage.sendMessageAgain(this);
				lastSendMessage = null;
			}
			String info = getL10nString(message.parameters, null);
			uiListener.notifyShowAlertInfo(info);
		} else {
			String param = message.parameters;
			if (isDynamicPayMessageToShowMenu(param))
				showPayMenu(param);
			else {
				String info = getL10nString(param, null);
				uiListener.notifyShowAlertInfo(info);
			}
		}
	}

	private boolean isDynamicPayMessageToShowMenu(String parameters) {
		return parameters.indexOf("options") != -1;
	}

	private Message lastSendMessage;
	// private MArray array = new MArray();
	private final String titlePrefix = ":";
	private final String optionPrefix = "[";

	private void showPayMenu(String msg) {
		String msgTitle = Utils.getPayTitle(msg, titlePrefix);
		System.out.println("--- msgTitle=" + msgTitle);
		String title = this.getL10nString(msgTitle, null);
		System.out.println("--- title after trans=" + title);
		boolean hasOption = msg.indexOf(optionPrefix) > -1;
		if (!hasOption) {
			uiListener.notifyShowAlertInfo(title);
			return;
		}
		String optionBody = Utils.getPayOptoions(msg, optionPrefix);
		String options[] = Utils.splitOptions(optionBody);
		MenuItem menus[] = new MenuItem[options.length + 1];
		for (int i = 0; i < options.length; i++) {
			menus[i] = new MenuItem(options[i]);
		}
		menus[options.length] = new MenuItem(Def.cmdCancel,
				PopMenuListener.actionCancel);
		uiListener.notifyShowPopMenu(title, menus, 0, "popToName", this);
	}

	public void initL10nString() {
		Def.initL10nString();
		Message.initL10nString();
		// getMenuItemManager().initMenuItemQueue();
		getMenuItemManager().loadL10nString();
		Message.initL10nString();
	}

	public String getTitle() {
		return Def.titleChat;
	}

	public String[] getRoomNameList() {
		return Def.roomNameList;
	}

	public byte[] getRoomTypeList() {
		return Def.roomTypeList;
	}

	public byte getRoomType(int focus) {
		if (focus < 0 || focus >= Def.roomTypeList.length) {
			throw new IllegalArgumentException(
					"HallCore getFocusRoomType focus:" + focus);
		}
		return Def.roomTypeList[focus];
	}

	public int getRoomIndex(byte type) {
		verifyRoomType(type);
		for (int i = 0; i < Def.roomTypeList.length; i++) {
			if (Def.roomTypeList[i] == type) {
				return i;
			}
		}
		throw new IllegalArgumentException("getRoomFocusIndex type:" + type);
	}

	private static void verifyRoomType(byte type) {
		switch (type) {
		case Def.CHAT_TYPE_PRIVATE:
		case Def.CHAT_TYPE_FAMILY:
		case Def.CHAT_TYPE_WORLD:
		case Def.CHAT_TYPE_SYSTEM:
			return;
		}
		throw new IllegalArgumentException("verifyRoomType chatType:" + type);
	}

	private static void verifyMessageType(byte type) {
		switch (type) {
		case Def.CHAT_TYPE_PRIVATE:
		case Def.CHAT_TYPE_FAMILY:
		case Def.CHAT_TYPE_WORLD:
		case Def.CHAT_TYPE_WORLD_TOP:
		case Def.CHAT_TYPE_SYSTEM:
			return;
		}
		throw new IllegalArgumentException("verifyMessageType chatType:" + type);
	}

	public MenuItem[] getMessageMenuItems(Message message) {
		if (message == null) {
			throw new IllegalArgumentException("verifyRoomType message:"
					+ message);
		}
		byte type = message.getType();

		verifyMessageType(type);

		if (message.isSendByMyself()) {
			if (type == Def.CHAT_TYPE_WORLD_TOP) {
				MenuItem menu[] = new MenuItem[] {
						new MenuItem(Def.cmdTopSend,
								PopMenuListener.actionTopSend),
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menu;
			} else {
				return null;
			}
		} else {
			switch (type) {
			case Def.CHAT_TYPE_PRIVATE:
				MenuItem menuPrivate[] = {
						new MenuItem(Def.cmdHomePage,
								PopMenuListener.actionMainPage),
						new MenuItem(Def.cmdReply, PopMenuListener.actionReply),
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menuPrivate;
			case Def.CHAT_TYPE_FAMILY:
			case Def.CHAT_TYPE_WORLD:
				MenuItem reply = new MenuItem(Def.cmdReply,
						PopMenuListener.actionWorldMsgTo);
				reply.payParameters = message.getPopMenuUserName();
				MenuItem menuNormal[] = {
						new MenuItem(Def.cmdHomePage,
								PopMenuListener.actionMainPage),
						reply,
						new MenuItem(Def.cmdPrivateChat,
								PopMenuListener.actionPrivateChat),
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menuNormal;
			case Def.CHAT_TYPE_WORLD_TOP:
				MenuItem replayTopMsg = new MenuItem(Def.cmdReply,
						PopMenuListener.actionWorldMsgTo);
				replayTopMsg.payParameters = message.getPopMenuUserName();
				MenuItem menuTop[] = new MenuItem[] {
						new MenuItem(Def.cmdTopSend,
								PopMenuListener.actionTopSend),
						replayTopMsg,
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menuTop;
			case Def.CHAT_TYPE_SYSTEM:
				return null;
			}
		}
		return null;
	}
	
	public MenuItem[] getAndroidMessageMenuItems(Message message){
		if (message == null) {
			throw new IllegalArgumentException("verifyRoomType message:"
					+ message);
		}
		byte type = message.getType();

		verifyMessageType(type);

		if (message.isSendByMyself()) {
			return null;
		} else {
			switch (type) {
			case Def.CHAT_TYPE_PRIVATE:
				MenuItem menuPrivate[] = {
						new MenuItem(Def.cmdHomePage,
								PopMenuListener.actionMainPage),
						new MenuItem(Def.cmdReply, PopMenuListener.actionReply),
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menuPrivate;
			case Def.CHAT_TYPE_FAMILY:
			case Def.CHAT_TYPE_WORLD:
				MenuItem reply = new MenuItem(Def.cmdReply,
						PopMenuListener.actionWorldMsgTo);
				reply.payParameters = message.getPopMenuUserName();
				MenuItem menuNormal[] = {
						new MenuItem(Def.cmdHomePage,
								PopMenuListener.actionMainPage),
						reply,
						new MenuItem(Def.cmdPrivateChat,
								PopMenuListener.actionPrivateChat),
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menuNormal;
			case Def.CHAT_TYPE_WORLD_TOP:
				MenuItem replayTopMsg = new MenuItem(Def.cmdReply,PopMenuListener.actionWorldMsgTo);
				replayTopMsg.payParameters = message.getPopMenuUserName();
				MenuItem menuTop[] = new MenuItem[] {
						new MenuItem(Def.cmdTopSend,
								PopMenuListener.actionTopSend),
						replayTopMsg,
						new MenuItem(Def.cmdCancel,
								PopMenuListener.actionCancel) };
				return menuTop;
			case Def.CHAT_TYPE_SYSTEM:
				return null;
			}
		}
		return null;
	}

	public boolean showMessageMenu(Message message) {
		MenuItem[] menu = this.getMessageMenuItems(message);
		if (menu == null) {
			return false;
		}
		int toId = message.getPopMenuUserId();
		String toName = message.getPopMenuUserName();
		this.uiListener.notifyShowPopMenu(Def.popTitleWantTo, menu, toId,
				toName, null);
		return true;
	}

}

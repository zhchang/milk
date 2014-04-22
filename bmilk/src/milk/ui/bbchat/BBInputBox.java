package milk.ui.bbchat;

import milk.chat.core.Def;



import milk.ui.MilkAppImpl;
import milk.chat.core.HallAccess;
import milk.chat.port.CoreListener;
import milk.chat.core.MenuItem;
import milk.chat.core.PopMenuListener;
import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.implement.EditorSetting;
import milk.implement.InputReceiver;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class BBInputBox implements InputReceiver, PopMenuListener,
		BBFaceListener {

	private static final int MAX_INPUT_CONTENT_LENGTH = 75;
	private static final int MIN_SEND_INTERVAL_TIME = 2000;
	private long previousSendTime;
	private static int rectX, rectY, rectW, rectH;
	private static MilkApp factory;
	private boolean isFocus = true;
	private EditorSetting editorSetting;
	private byte chatType;
	private CoreListener coreListener;
	
	private final static MilkFont font = Adaptor.uiFactory.getFont(
			MilkFont.STYLE_PLAIN, MilkFont.SIZE_SMALL);

	public BBInputBox(MilkApp factory, byte chatType) {
		BBInputBox.factory = factory;
		this.chatType = chatType;
		editorSetting = new EditorSetting();
	}


	public void setFocus(boolean focus) {
		isFocus = focus;
	}

	public void setInitInputText(String input) {
		inputText = input;
		((MilkAppImpl) Adaptor.milk).setInputText(input);
	}

	public void updateInput(String input) {
//		Utils.info("-------input text " + input);
		inputText = input;
	}

	public String getInitText() {
		if (inputText != null)
			return inputText;
		else
			return "";
	}

	public void initInputBox(int x, int y, int w, int h) {
		if (h > font.getHeight()) {
			h = font.getHeight();
		}
		rectX = x;
		rectY = y - 1;
		rectW = w;
		rectH = h + 2;
		editorSetting.x = x;
		editorSetting.y = y;
		editorSetting.width = w;
		editorSetting.height = h;
		editorSetting.maxlength = 75;
		editorSetting.maxLines = 1;
		editorSetting.bgColor = 0xfffea3;
		editorSetting.receiver = this;
		editorSetting.autoBottom=true;
	}

	public void showEdit() {
		// editorSetting.bgColor = 0xffeddb;
		Adaptor.milk.showInput(editorSetting);

	}

	// public void showFocusEdit() {
	// editorSetting.bgColor = 0xfffea3;
	// MilkApp.milk.showInput(editorSetting);
	// }

	public static void hideEdit() {
		Adaptor.milk.hideInput();
	}

	public void draw(MilkGraphics g) {
		if (isFocus)
			return;
		RoundRect inputRect;
		g.setClip(0, rectY, factory.getCanvasWidth(), rectH);
		inputRect = BBResourceManager.inputFrame;
		inputRect.drawRoundRect(g, rectX, rectY, rectW, rectH);
		g.setClip(rectX, 0, rectW, factory.getCanvasHeight());
		g.setColor(0x000000);
		MilkFont oldfont = g.getFont();
		if (font != null)
			g.setFont(font);
		if (inputText != null)
			g.drawString(inputText, rectX + 2, rectY, 0);
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());
		g.setFont(oldfont);
	}


	public boolean isTouch(int x, int y) {
		if (factory.isTouchDevice()) {
			return Utils.pointInRect(x, y, rectX, rectY, rectW, rectH);
		}
		return false;
	}

	private String popToName;
	private int popToId;

	public void setTarget(int id, String name) {
		popToId = id;
		popToName = name;
	}

	private String inputText;

	public void showNotify() {
		((MilkAppImpl) Adaptor.milk).setEditFocus();
	}

	private void sendChatMessage(byte sendType) {
		String contents = inputText;
//		Utils.info("------ sendChatMessage inputText:" + contents);
		coreListener = HallAccess.getCoreListener();
		if (contents != null && contents.length() > 0) {
			if (contents.length() > MAX_INPUT_CONTENT_LENGTH) {
		
				String info = coreListener.getL10nString(Def.chatInpuMsgTooLong
//						"Input too long. (more than {0} alphanumeric characters)"
						, MAX_INPUT_CONTENT_LENGTH + "");
				showInputError(info);

				return;
			}
			if (System.currentTimeMillis() - previousSendTime < MIN_SEND_INTERVAL_TIME) {
				showInputError(Def.chatInputSendTooFast);
				return;
			}
			if (coreListener != null) {
				coreListener
						.sendMessage(sendType, popToId, popToName, contents);
				previousSendTime = System.currentTimeMillis();
			} else {
				throw new NullPointerException("chatListener=null");
			}
		} else {
			showInputError(Def.chatInputMsgIsNull);
		}
	}

	private void showInputError(String info) {
		Adaptor.milk.showAlert(info);
	}

	public void handlePopMenuEvent(MenuItem item, int userId,
			String userName, int focus) {
		coreListener = HallAccess.getCoreListener();
		String menuName = item.actionName;
		if (menuName.equals(Def.cmdNormalSend)) {
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
			sendChatMessage(chatType);
			inputText = "";
			((MilkAppImpl) Adaptor.milk).clearInputText();
		} else if (menuName.equals(Def.cmdTopSend)) {
			sendChatMessage(Def.CHAT_TYPE_WORLD_TOP);
			inputText = "";
			((MilkAppImpl) Adaptor.milk).clearInputText();
		} else if (menuName.equals(Def.cmdAddEmotion)) {
			String contents = inputText;
			if (contents != null
					&& contents.length() + 4 > MAX_INPUT_CONTENT_LENGTH) {
				
				String info = coreListener.getL10nString(Def.chatInpuMsgTooLong
						,MAX_INPUT_CONTENT_LENGTH + "");
						
				showInputError(info);
				return;
			}
			BBRoom.bbFace.setFaceInputListener(this);
			BBRoom.bbFace.popupFaceScreen();
		}
	}

	public void showPopMenu() {
		MenuItem[] menu = HallAccess.getCoreListener().getInputMenuItem(chatType);
		BBPopMenu.getInstance().initPopMenu(Def.popTitleWantTo,menu, -1, "", this);
		BBPopMenu.getInstance().show();
	}

	public void insertFace(String faceName) {
		Utils.info("-------inputBox insertFace face:" + faceName);
		((MilkAppImpl) Adaptor.milk).setEditFocus();
		((MilkAppImpl) Adaptor.milk).insertInputText(faceName);
	}

}

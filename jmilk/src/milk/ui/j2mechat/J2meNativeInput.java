package milk.ui.j2mechat;

import javax.microedition.lcdui.Command;



import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import milk.chat.core.Def;
import milk.chat.core.Utils;
import milk.chat.port.CoreListener;
import milk.ui.MilkDisplayableImpl;
import milk.implement.Adaptor;
import milk.ui2.MilkDisplayable;

public class J2meNativeInput extends Form implements J2meFaceListener,
		J2meButtonListener, CommandListener {

	private static final int MAX_INPUT_CONTENT_LENGTH = 75;
	private static final int MIN_SEND_INTERVAL_TIME = 1000;
	private static long previousSendTime;

	private static TextField textInput;
	private static MilkDisplayable backDisplayable;
	private static byte chatInputType;
	private static int receiveId;
	private static String receiveName;
	private static StringItem error;

	private static CoreListener coreListener;
	private MilkDisplayable md;

	private static J2meButtonManager chatButtonManager;
	private static boolean isTouchDevice;

	public J2meNativeInput() {
		super("Chat");
		isTouchDevice = Adaptor.milk.isTouchDevice();
		textInput = new TextField("", "", MAX_INPUT_CONTENT_LENGTH,
				TextField.ANY);
		error = new StringItem("", "");

		md = new MilkDisplayableImpl(this);
		this.setItemStateListener(new ItemStateListener() {
			public void itemStateChanged(Item item) {
				if (item == textInput) {
					hideInputError();
				}
			}
		});
		initL10nString();
		
	}

	private Command cmdSend;
	private Command cmdTopSend;
	private Command cmdAddEmotion;
	private Command cmdCancel;
	private Command cmdBack;

	public void initL10nString() {
		remveAllCommand();
		if (!isTouchDevice) {
			initCommand();
		} else {
			chatButtonManager = new J2meButtonManager(Adaptor.milk, this);
			cmdBack = new Command(Def.cmdCancel, Command.BACK, 0);
		}
	}

	private void initCommand() {
		cmdSend = new Command(Def.cmdNormalSend, Command.BACK, 0);
		cmdTopSend = new Command(Def.cmdTopSend, Command.ITEM, 0);
		cmdAddEmotion = new Command(Def.cmdAddEmotion, Command.ITEM, 0);
		cmdCancel = new Command(Def.cmdCancel, Command.ITEM, 0);
	}

	public void setChatListener(CoreListener chatListener) {
		J2meNativeInput.coreListener = chatListener;
	}

	public void openInputPage(byte chatType, String toName) {
		openInputPage(chatType, 0, "");
		hideInputError();
		textInput.setString(coreListener.getL10nString("To {0}:", toName));
	}

	public void openInputPage(byte chatType, int _receiveId, String _receiveName) {
		chatInputType = chatType;
		receiveId = _receiveId;
		receiveName = _receiveName;
		initTitle();
		textInput.setString("");
		updateFormItems(0);
		hideInputError();
		backDisplayable = Adaptor.milk.getCurrentDisplay();
		Adaptor.milk.switchDisplay(md);
	}

	private void updateFormItems(int textLength) {
		this.deleteAll();
		this.append(textInput);
		remveAllCommand();
		if (isTouchDevice) {

			J2meButton[] btn = chatButtonManager.getInputButton(chatInputType);
			for (int i = 0; i < btn.length; i++) {
				this.append(btn[i]);
			}
			this.addCommand(this.cmdBack);
			this.setCommandListener(this);
		} else {
			Command[] cmd = getCommand();
			for (int i = 0; i < cmd.length; i++) {
				this.addCommand(cmd[i]);
			}
			this.setCommandListener(this);
		}
		append(error);
	}

	private void remveAllCommand() {
		if (cmdSend != null)
			this.removeCommand(cmdSend);
		if (cmdTopSend != null)
			this.removeCommand(cmdTopSend);
		if (cmdAddEmotion != null)
			this.removeCommand(cmdAddEmotion);
		if (cmdCancel != null)
			this.removeCommand(cmdCancel);
		if (cmdBack != null)
			this.removeCommand(cmdBack);
	}

	private Command[] getCommand() {
		if (chatInputType == Def.CHAT_TYPE_WORLD_TOP) {
			return new Command[] { cmdTopSend, cmdAddEmotion, cmdCancel };
		} else if (chatInputType == Def.CHAT_TYPE_WORLD) {
			return new Command[] { cmdSend, cmdTopSend, cmdAddEmotion,
					cmdCancel };
		} else {
			return new Command[] { cmdSend, cmdAddEmotion, cmdCancel };
		}
	}

	private void initTitle() {
		switch (chatInputType) {
		case Def.CHAT_TYPE_PRIVATE:
			this.setTitle(Def.inputPrivate);
			break;
		case Def.CHAT_TYPE_FAMILY:
			this.setTitle(Def.inputTribe);
			break;
		case Def.CHAT_TYPE_WORLD:
			this.setTitle(Def.inputWorld);
			break;
		case Def.CHAT_TYPE_WORLD_TOP:
			this.setTitle(Def.inputTop);
			break;
		default:
			throw new IllegalArgumentException("setTitleAndCommand chat type:"
					+ chatInputType);
		}
	}

	private void sendChatMessage(byte sendType) {
		String contents = textInput.getString();
		if (contents != null && contents.length() > 0) {
			if (contents.length() > MAX_INPUT_CONTENT_LENGTH) {
				String info = coreListener.getL10nString(Def.chatInpuMsgTooLong, MAX_INPUT_CONTENT_LENGTH + "");
				showInputError(info);

				return;
			}
			if (System.currentTimeMillis() - previousSendTime < MIN_SEND_INTERVAL_TIME) {
				showInputError(Def.chatInputSendTooFast);
				return;
			}
			if (coreListener != null) {
				coreListener.sendMessage(sendType, receiveId, receiveName,
						contents);
				previousSendTime = System.currentTimeMillis();
			} else {
				throw new NullPointerException("chatListener=null");
			}
			Adaptor.milk.switchDisplay(backDisplayable);
		} else {
			showInputError(Def.chatInputMsgIsNull);
		}
	}

	private void showInputError(String info) {
		error.setLabel(Def.chatInputErrorInfo);
		error.setText(info);
		// Utils.info("---showInputError ---:" + info);
	}

	private void hideInputError() {
		error.setLabel("");
		error.setText("");
		// Utils.info("---hideInputError ---:");
	}
	
	private void showFacePage() {
		J2meHallScreen.faceScreen.setFaceInputListener(this);
		J2meHallScreen.faceScreen.show(backDisplayable);
	}

	public void insertFace(String emotionName) {
		if (emotionName == null || emotionName.length() == 0) {
			throw new NullPointerException("emotionName=null");
		}
		int inputPos = textInput.getCaretPosition();
		textInput.insert(emotionName, inputPos);
	}

	public void handleClick(J2meButton button) {
		String buttonName = button.getName();
		Utils.info("---handleClick---" + buttonName);
		if (buttonName.equals(Def.cmdNormalSend)) {
			sendChatMessage(chatInputType);
		} else if (buttonName.equals(Def.cmdTopSend)) {
			sendChatMessage(Def.CHAT_TYPE_WORLD_TOP);
		} else if (buttonName.equals(Def.cmdAddEmotion)) {
			String contents = textInput.getString();
			if (contents != null
					&& contents.length() + 4 > MAX_INPUT_CONTENT_LENGTH) {
				String info = coreListener.getL10nString(Def.chatInpuMsgTooLong,MAX_INPUT_CONTENT_LENGTH + "");
				showInputError(info);
				return;
			}
			showFacePage();
			initTitle();
		} else if (buttonName.equals(Def.cmdCancel)) {
			// Utils.info("---switchDisplay--backDisplayable:" +
			// backDisplayable);
			Adaptor.milk.switchDisplay(backDisplayable);
		}
	}

	public void commandAction(Command command, Displayable d) {
		if (command == cmdSend) {
			sendChatMessage(chatInputType);
		} else if (cmdTopSend == command) {
			sendChatMessage(Def.CHAT_TYPE_WORLD_TOP);
		} else if (cmdAddEmotion == command) {
			String contents = textInput.getString();
			if (contents != null
					&& contents.length() + 4 > MAX_INPUT_CONTENT_LENGTH) {
				String info = coreListener.getL10nString(Def.chatInpuMsgTooLong,MAX_INPUT_CONTENT_LENGTH + "");
				showInputError(info);
				return;
			}
			showFacePage();
			initTitle();
		} else if (command == cmdCancel) {
			Adaptor.milk.switchDisplay(backDisplayable);
		} else if (command == cmdBack) {
			Adaptor.milk.switchDisplay(backDisplayable);
		}

	}

	// public void itemStateChanged(Item item) {
	// if (item == textInput) {
	// hideInputError();
	// }
	// }

}

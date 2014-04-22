package milk.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.TextField;

import milk.implement.Adaptor;
import milk.implement.EditorSetting;
import milk.implement.InputReceiver;
import milk.implement.mk.MRect;
import milk.ui2.MilkCanvas;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class MilkCanvasImpl extends Screen implements DialogClosedListener,
		MilkCanvas {

	public static final int FIRE = Canvas.FIRE;

	OneLineInputField edit;
	PasswordEditField password;

	private int xOffset = -1;
	private int yOffset = -1;

	XYRect editRect = new XYRect();
	XYRect passwordRect = new XYRect();

	boolean editShowing = false;
	boolean passwordShowing = false;

	private boolean navigationRepeating = false;

	private long lastMovedTime = 0;

	private MilkGraphicsImpl mg = new MilkGraphicsImpl();
	private Dialog exitDialog;

	private final int gameScreenHeight, gameScreenWidth;

	private MRect viewPort;

	MilkCanvasImpl() {
		super(MilkManager.instance);
		Ui.getUiEngineInstance().setAcceptableDirections(
				Display.DIRECTION_PORTRAIT);
		gameScreenHeight = Adaptor.getInstance().getConfigHeight();
		gameScreenWidth = Adaptor.getInstance().getConfigWidth();
		System.out.println("------------game height=" + gameScreenHeight
				+ "/width=" + gameScreenWidth);

		xOffset = (getWidth() - gameScreenWidth) / 2;
		yOffset = (getHeight() - gameScreenHeight) / 2;
		viewPort = new MRect(0, 0, gameScreenWidth, gameScreenHeight);
	}

	private void showExitDialog() {
		exitDialog = new Dialog(Dialog.D_YES_NO, Adaptor.getInstance()
				.getTranslation("Do you want to exit game?", null), 0, null, 0) {
			protected boolean keyDown(int keyCode, int arg1) {
				if (Keypad.key(keyCode) == Keypad.KEY_END
						|| Keypad.key(keyCode) == Keypad.KEY_MENU) {
					return true;
				}
				return super.keyDown(keyCode, arg1);
			}

		};
		exitDialog.setDialogClosedListener(this);
		exitDialog.show();
	}

	protected void sublayout(int width, int height) {
		setExtent(Display.getWidth(), Display.getHeight());
		setPosition(0, 0);
		xOffset = (Display.getWidth() - gameScreenWidth) / 2;
		yOffset = (Display.getHeight() - gameScreenHeight) / 2;
		// System.out.println("---------bb screen sublayout Display.getHeight()"+Display.getHeight());
		// System.out.println("---------bb screen sublayout height"+height);
		// System.out.println("---------bb screen sublayout yOffset"+yOffset);
		sublayoutInputField(height);
	}

	private void sublayoutInputField(int newGameScreenHeight) {
		if (VirtualKeyboard.isSupported()) {
			if (editShowing) {
				if (onelineInputEditSet.autoBottom) {// int chat scene
					int gameBottomY = this.gameScreenHeight;
					if (gameBottomY > newGameScreenHeight) {
						gameBottomY = newGameScreenHeight;
					}
					MilkManager.instance.doLayoutChild(edit,
							onelineInputEditSet.x + xOffset, gameBottomY
									- onelineInputEditSet.height - 1,
							onelineInputEditSet.width,
							onelineInputEditSet.height);
				} else {
					MilkManager.instance.doLayoutChild(edit,
							onelineInputEditSet.x + xOffset,
							onelineInputEditSet.y + yOffset,
							onelineInputEditSet.width,
							onelineInputEditSet.height);
				}
			}
			if (passwordShowing) {
				MilkManager.instance.doLayoutChild(password, passWordEditSet.x
						+ xOffset, passWordEditSet.y + yOffset,
						passWordEditSet.width, passWordEditSet.height);
			}
		}
	}

	// private void showOrHideVirtualKeyboard() {
	// if (VirtualKeyboard.isSupported()) {
	// VirtualKeyboard vk = getVirtualKeyboard();
	// if (vk.getVisibility() == VirtualKeyboard.HIDE)
	// vk.setVisibility(VirtualKeyboard.SHOW);
	// else if (vk.getVisibility() == VirtualKeyboard.SHOW)
	// vk.setVisibility(VirtualKeyboard.HIDE);
	// }
	// }

	void init() {
		edit = new OneLineInputField(EditField.NO_NEWLINE
				| EditField.NON_SPELLCHECKABLE);
		add(edit);
		edit.setEditable(true);

		password = new PasswordEditField();
		add(password);
		password.setEditable(true);
	}

	public static MilkFontImpl getFontByHeight(int height) {
		return (MilkFontImpl) MilkFontImpl.getFontByHeight(height);
	}

	private EditorSetting onelineInputEditSet, passWordEditSet;

	public void showEdit(EditorSetting setting) {
		this.hideInputWithOutHideVirtualKeyboard();
		BasicEditField bef = null;
		if ((setting.constraints & TextField.PASSWORD) > 0) {
			passwordShowing = true;
			bef = password;
			bef.setFont(getFontByHeight(setting.height).font);
			MilkManager.instance.doLayoutChild(bef, setting.x + xOffset,
					setting.y + yOffset, setting.width, setting.height);
			passWordEditSet = setting;
		} else {

			editShowing = true;
			bef = edit.getEditField();
			if (setting.maxlength > 0) {
				bef.setMaxSize(setting.maxlength);
			}
			MilkManager.instance.doLayoutChild(edit, setting.x + xOffset,
					setting.y + yOffset, setting.width, setting.height);
			onelineInputEditSet = setting;
		}

		bef.setFocus();
		bef.setEditable(true);
		bef.setFont(getFontByHeight(setting.height).font);
		final InputReceiver receiver = setting.receiver;
		bef.setText(receiver.getInitText());
		bef.setBackground(BackgroundFactory
				.createSolidBackground(setting.bgColor));

		bef.setChangeListener(new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {
				if (field == edit.getEditField()) {
					receiver.updateInput(edit.getEditField().getText());
				} else if (field == password) {
					receiver.updateInput(password.getText());
				}
			}
		});
		this.donotCallSuperEvent = false;
		if (VirtualKeyboard.isSupported()) {
			VirtualKeyboard vk = this.getVirtualKeyboard();
			vk.setVisibility(VirtualKeyboard.SHOW);
		}
	}

	public void hideInput() {
		hideInputWithOutHideVirtualKeyboard();
		if (VirtualKeyboard.isSupported()) {
			VirtualKeyboard vk = getVirtualKeyboard();
			vk.setVisibility(VirtualKeyboard.HIDE);
		}
	}

	private void hideInputWithOutHideVirtualKeyboard() {
		password.setChangeListener(null);
		edit.setChangeListener(null);
		edit.setNotLastLine();
		MilkManager.instance.doLayoutChild(edit, -100, -100, 20, 20);
		edit.setText("");
		MilkManager.instance.doLayoutChild(password, -100, -100, 20, 20);
		password.setText("");
		passwordShowing = false;
		editShowing = false;
		password.setEditable(false);
		onelineInputEditSet = null;
		edit.setEditable(false);
	}

	public void clearInputText() {
		edit.setText("");
	}

	private boolean keyPressed = false;
	private boolean keyRepeating = false;
	private long pointerDraggedTime;

	protected boolean touchEvent(TouchEvent message) {
		if (Adaptor.getInstance().ignoreInputEvent())
			return true;
		int x = message.getGlobalX(1);
		int y = message.getGlobalY(1);
		x -= xOffset;
		y -= yOffset;
		switch (message.getEvent()) {
		case TouchEvent.DOWN: {
			Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_PRESSED);
			break;
		}
		case TouchEvent.MOVE: {
			if (System.currentTimeMillis() - pointerDraggedTime > 200) {
				Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_DRAGGED);
				pointerDraggedTime = System.currentTimeMillis();
			}
			break;
		}
		case TouchEvent.UP: {
			Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_RELEASED);
			break;
		}
		}
		return true;
	}

	protected void paintBackground(Graphics g) {
		mg.setG(g);
		mg.setColor(0);
		mg.fillRect(0, 0, Display.getWidth(), Display.getHeight());
		mg.translate(xOffset - mg.getTranslateX(), yOffset - mg.getTranslateY());
		Adaptor.getInstance().draw(mg);
		mg.translate(0 - mg.getTranslateX(), 0 - mg.getTranslateY());

	}

	protected void paint(Graphics g) {
		if (editShowing) {
			g.setColor(Color.BLACK);
			MilkManager.instance.doPaintChild(g, edit);
		}
		if (passwordShowing) {
			g.setColor(Color.BLACK);
			MilkManager.instance.doPaintChild(g, password);
		}
	}

	private int canvasKey2AdatorKey(int keyCode) {// make mobile hardware key

		char c = Keypad.map(keyCode);
		keyCode = Keypad.key(keyCode);

		if (keyCode == Keypad.KEY_MENU) {
			return Adaptor.KEY_LEFT_SOFT;
		}
		if (keyCode == Keypad.KEY_ESCAPE) {
			return Adaptor.KEY_RIGHT_SOFT;
		}
		if (c >= '0' && keyCode <= '9') {// 0-9
			return Adaptor.KEY_NUM0 + c - '0';
		}
		if (keyCode == Keypad.KEY_ENTER) {
			return Adaptor.KEY_FIRE;
		}
		return keyCode;
	}

	public void ignoreNextRepeat() {
	}

	private long keyRepeatedTime;
	private int repeatedKeyCode;

	public void hideNotify() {
		clearKeyStatus();
		Adaptor.getInstance().pauseApp();
	}

	public void clearKeyStatus() {
		keyPressed = false;
		keyRepeating = false;
	}

	public void showNotify() {
		Adaptor.getInstance().resumeApp();
	}

	public void processKeyRepeatedEvent() {
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
		if (keyPressed) {
			boolean shouldRepeat = false;
			if (!keyRepeating) {
				shouldRepeat = System.currentTimeMillis() - keyRepeatedTime >= 500;
				if (shouldRepeat) {
					keyRepeating = true;
				}
			} else {
				shouldRepeat = System.currentTimeMillis() - keyRepeatedTime >= 60;
			}
			if (shouldRepeat) {
				Adaptor.getInstance().onKey(repeatedKeyCode,
						Adaptor.KEYSTATE_PRESSED);
				Adaptor.getInstance().onKey(repeatedKeyCode, 0);
				keyRepeatedTime = System.currentTimeMillis();
			}
		}
	}

	protected boolean keyChar(char c, int status, int time) {
		// showOrHideVirtualKeyboard();
		if (!donotCallSuperEvent)
			return super.keyChar(c, status, time);
		else
			return true;
	}

	protected boolean keyRepeat(int keycode, int time) {
		if (!donotCallSuperEvent)
			return super.keyRepeat(keycode, time);
		else
			return true;
	}

	protected boolean keyDown(int keyCode, int time) {
		if (Keypad.key(keyCode) == Keypad.KEY_END) {
			showExitDialog();
			return true;
		}
		boolean rs = true;
		if (!donotCallSuperEvent)
			rs = super.keyDown(keyCode, time);
		if (Keypad.key(keyCode) == Keypad.KEY_MENU) {
			Adaptor.getInstance().onKey(Adaptor.KEY_MENU,
					Adaptor.KEYSTATE_PRESSED);
		}
		if ((passwordShowing || editShowing) && !donotCallSuperEvent
				&& Keypad.key(keyCode) != Keypad.KEY_ESCAPE) {
			return rs;
		}
		if (Adaptor.getInstance().ignoreInputEvent())
			return true;
		keyCode = canvasKey2AdatorKey(keyCode);
		if (keyCode == Adaptor.KEY_LEFT_SOFT) {
			Adaptor.getInstance().onLeftSoftKey();
		} else if (keyCode == Adaptor.KEY_RIGHT_SOFT) {
			Adaptor.getInstance().onRightSoftKey();
		} else {
			keyPressed = true;
			keyRepeatedTime = System.currentTimeMillis();
			repeatedKeyCode = keyCode;
			Adaptor.getInstance().onKey(keyCode, Adaptor.KEYSTATE_PRESSED);

		}
		return rs;
	}

	protected boolean keyUp(int keyCode, int time) {
		boolean sr = true;
		if (!donotCallSuperEvent)
			sr = super.keyUp(keyCode, time);
		if (keyPressed) {
			keyPressed = false;
			keyRepeating = false;
			if (Adaptor.getInstance().ignoreInputEvent())
				return true;
			keyCode = canvasKey2AdatorKey(keyCode);
			if (keyCode == Adaptor.KEY_LEFT_SOFT
					|| keyCode == Adaptor.KEY_RIGHT_SOFT) {
				return true;
			}
			Adaptor.getInstance().onKey(keyCode, Adaptor.KEYSTATE_RELEASED);
		}
		return sr;
	}

	public void repaint() {
		invalidateAll(0, 0, getWidth(), getHeight());
	}

	private long lastNavigation = System.currentTimeMillis();

	private boolean moved = false;

	protected boolean navigationClick(int status, int time) {
		boolean rs = true;
		if (!donotCallSuperEvent)
			rs = super.navigationClick(status, time);
		if ((passwordShowing || editShowing) && !donotCallSuperEvent) {
			return rs;
		}
		moved = false;
		return rs;
	}

	protected boolean navigationUnclick(int status, int time) {
		boolean rs = true;
		if (!donotCallSuperEvent)
			rs = super.navigationUnclick(status, time);
		if ((passwordShowing || editShowing) && !donotCallSuperEvent) {
			return rs;
		}
		navigationRepeating = false;
		if (!moved) {
			Adaptor.getInstance().onKey(Adaptor.KEY_FIRE, -1);
			// Adaptor.getInstance().onKey(Adaptor.KEY_FIRE, 0);
			Adaptor.getInstance().onKey(Adaptor.KEY_FIRE, 1);
		}
		return rs;
	}

	protected boolean navigationMovement(int dx, int dy, int status, int time) {

		boolean passdown = true, rs = true;
		// Utils.info("-------keyDown forwardEventToScene:" + callSuperEvent);
		if (!donotCallSuperEvent) {
			passdown = showPassNavigationOutside(dx, dy);
			rs = super.navigationMovement(dx, dy, status, time);
		}
		if (!passdown && !donotCallSuperEvent) {
			return rs;
		}
		if (time - lastMovedTime < 100) {
			return rs;
		}
		lastMovedTime = time;
		boolean shouldSend = false;
		moved = true;
		if (!navigationRepeating) {
			shouldSend = System.currentTimeMillis() - lastNavigation > 500;
			if (shouldSend) {
				navigationRepeating = true;
			}
		} else {
			shouldSend = true;
		}
		int absX = Math.abs(dx);
		int absY = Math.abs(dy);
		if (absX > 0 && absY > 0) {
			if (absX > absY) {
				dy = 0;
			} else {
				dx = 0;
			}
		}
		if (shouldSend) {
			int key = 0;
			if (dx < 0) {
				key = Adaptor.KEY_LEFT;
			} else if (dx > 0) {
				key = Adaptor.KEY_RIGHT;
			}
			Adaptor.getInstance().onKey(key, -1);
			// Adaptor.getInstance().onKey(key, 0);
			Adaptor.getInstance().onKey(key, 1);
			key = 0;
			if (dy < 0) {
				key = Adaptor.KEY_UP;
			} else if (dy > 0) {
				key = Adaptor.KEY_DOWN;
			}
			Adaptor.getInstance().onKey(key, -1);
			// Adaptor.getInstance().onKey(key, 0);
			Adaptor.getInstance().onKey(key, 1);
			lastNavigation = System.currentTimeMillis();
		}

		return rs;
	}

	private boolean showPassNavigationOutside(int dx, int dy) {
		boolean leftUp = (dx < 0 || dy < 0);
		boolean rightDown = (dx > 0 || dy > 0);

		if (passwordShowing || editShowing) {
			BasicEditField bef = password;
			if (editShowing) {
				return edit.navigationMovement(dx, dy);
			}
			if (leftUp && bef.getCursorPosition() == 0) {
				return true;
			}
			if (rightDown && bef.getCursorPosition() == bef.getTextLength()) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	private boolean donotCallSuperEvent = false;

	public void setCallSuperEvent(boolean b) {
		donotCallSuperEvent = b;
		edit.setLastLine();
	}

	public void setInitInputText(String text) {
		this.edit.setText(text);
		edit.setFocus();
	}

	public void setEditFocus() {
		edit.setFocus();
	}

	public void insertInputText(final String text) {
		edit.insert(text);
		edit.setFocus();
	}

	public void dialogClosed(Dialog dialog, int selectedChoice) {
		// System.out.println("dialogClosed selectedChoice:"+selectedChoice);
		if (selectedChoice == Dialog.YES) {
			Adaptor.milk.destroyApp();
		}
	}

}

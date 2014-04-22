package milk.ui;

import javax.microedition.lcdui.Canvas;


import javax.microedition.lcdui.Graphics;

import milk.implement.Adaptor;
import milk.implement.mk.MRect;
import milk.ui2.MilkCanvas;

public class MilkCanvasImpl extends Canvas implements MilkCanvas {

	public static final int FIRE = Canvas.FIRE;
	private boolean keyPressed = false;
	private boolean keyRepeating = false;
	private long pointerDraggedTime;
	private long keyRepeatedTime;
	private int repeatedKeyCode;

	// private boolean pointerDown = false;

	private int xOffset = -1;
	private int yOffset = -1;
	private MilkGraphicsImpl mg = new MilkGraphicsImpl();
	private MRect viewPort;

	MilkCanvasImpl() {
		setFullScreenMode(true);
		Adaptor adaptor = Adaptor.getInstance();
		xOffset = (getWidth() - adaptor.getConfigWidth()) / 2;
		yOffset = (getHeight() - adaptor.getConfigHeight()) / 2;
		viewPort = new MRect(0, 0, adaptor.getConfigWidth(),
				adaptor.getConfigHeight());
	}

	protected void pointerDragged(int x, int y) {
		x -= xOffset;
		y -= yOffset;
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
		if (System.currentTimeMillis() - pointerDraggedTime > 200) {
			Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_DRAGGED);
			pointerDraggedTime = System.currentTimeMillis();
		}
	}

	protected void pointerPressed(int x, int y) {
		// if (pointerDown) {
		// return;
		// }
		// pointerDown = true;
		x -= xOffset;
		y -= yOffset;
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
		Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_PRESSED);
	}

	protected void pointerReleased(int x, int y) {
		// pointerDown = false;
		x -= xOffset;
		y -= yOffset;
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
		Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_RELEASED);
	}

	protected void paint(Graphics g) {
		mg.setG(g);
		mg.setColor(0);
		mg.fillRect(0, 0, getWidth(), getHeight());
		mg.translate(xOffset - mg.getTranslateX(), yOffset - mg.getTranslateY());
		try {
			Adaptor.getInstance().draw(mg);
		} catch (Exception e) {
		}
		mg.translate(0 - mg.getTranslateX(), 0 - mg.getTranslateY());

	}

	private int canvasKey2AdatorKey(int keyCode) {// make mobile hardware key
													// value to logic value
		if (keyCode == -6 || keyCode == -21 || keyCode == 21) {
			return Adaptor.KEY_LEFT_SOFT;
		}
		if (keyCode == -7 || keyCode == -22 || keyCode == 22) {
			return Adaptor.KEY_RIGHT_SOFT;
		}
		if (keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9) {// 0-9
			return keyCode;
		}
		if (keyCode == Canvas.KEY_POUND || keyCode == Canvas.KEY_STAR) {// #,*
			return keyCode;
		}
		keyCode = this.getGameAction(keyCode);
		switch (keyCode) {// navigate key,middle key
		case Canvas.UP: {
			keyCode = Adaptor.KEY_UP;
			break;
		}
		case Canvas.DOWN: {
			keyCode = Adaptor.KEY_DOWN;
			break;
		}
		case Canvas.LEFT: {
			keyCode = Adaptor.KEY_LEFT;
			break;
		}
		case Canvas.RIGHT: {
			keyCode = Adaptor.KEY_RIGHT;
			break;
		}
		case Canvas.FIRE: {
			keyCode = Adaptor.KEY_FIRE;
			break;
		}
		}
		return keyCode;
	}

	public void ignoreNextRepeat() {
	}

	protected void keyPressed(int keyCode) {
		
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
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
	}

	protected void keyReleased(int keyCode) {
		if (keyPressed) {
			keyRepeating = false;
			keyPressed = false;
			if (Adaptor.getInstance().ignoreInputEvent())
				return;
			keyCode = canvasKey2AdatorKey(keyCode);
			if (keyCode == Adaptor.KEY_LEFT_SOFT
					|| keyCode == Adaptor.KEY_RIGHT_SOFT) {
				return;
			}
			Adaptor.getInstance().onKey(keyCode, Adaptor.KEYSTATE_RELEASED);
		}
	}

	public void hideNotify() {
		clearKeyStatus();
		Adaptor.getInstance().pauseApp();
	}

	public void showNotify() {
		Adaptor.getInstance().resumeApp();
		setFullScreenMode(true);
	}

	public void processKeyRepeatedEvent() {
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
		if (keyPressed && isShown()) {
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

	public void clearKeyStatus() {
		keyPressed = false;
		keyRepeating = false;
	}

	public int getKeyAction(int keyCode) {
		try {
			return getGameAction(keyCode);
		} catch (Exception e) {
			return 0;
		}
	}

	public void setCallSuperEvent(boolean b) {
	}

	public void setInitInputText(String text) {

	}
}

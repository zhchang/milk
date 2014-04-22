package milk.ui.bbchat;

import java.util.Vector;

import milk.chat.core.SelectBoxCore;
import milk.chat.core.Def;
import milk.chat.core.UserInfo;
import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.implement.IMEvent.MKeyEvent;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class BBSelectBox {

	static final int DEFAULT_FRAME_WIDTH = 40;

	private static final int notifyInfoColor = 0x4a2c03;
	private static final int infoColor = 0xfff694;

	private static final int spaceY = 0, spaceX = 6;
	private int tabRectX, tanRectY, tabRectW, tabRectH;
	private MilkApp factory;
	private int popRectW;
	private int popRectH;
	private int popRectX;
	private int popRectY;
	private int maxUserNameDrawWidth;
	private int focus;
	private String userNameShowInTab;
	private static final MilkFont font = BBHallScreen.font;
	private static final int lineH = font.getHeight();

	private boolean isFocus = false;
	private RoundRect focusRect, unfocusRect;

	private Vector popQueue;
	private final int maxQueueLength;
	private SelectBoxCore boxCore;
	private BBSelectBoxListener selectBoxListener;

	BBSelectBox(MilkApp factory) {
		this.factory = factory;
		boxCore = new SelectBoxCore();
		maxQueueLength = boxCore.getMaxLength();
		popQueue = new Vector(maxQueueLength);
	}

	void setItemSelectBoxListener(BBSelectBoxListener l) {
		selectBoxListener = l;
	}

	void initUserQueue(Vector messageQueue) {
		boxCore.initUserQueue(messageQueue);
		UserInfo last = boxCore.getTopUserInfo();
		if (last != null) {
			this.userNameShowInTab = last.name;
			if (selectBoxListener != null) {
				selectBoxListener.focusItemChange(last.id, last.name);
			} else {
				throw new NullPointerException(
						"initSelectBoxByMessageQueue(),selectBoxListener=null");
			}
		}
	}

	boolean isEmpty() {
		return popQueue.size() == 0;
	}

	void setFocus(boolean focus) {
		isFocus = focus;
		if (!isFocus) {
			this.hideSelectBox();
		}
	}

	boolean isFocus() {
		return isFocus;
	}

	void initSelectBox(int x, int y, int w, int h) {
		this.tabRectX = x;
		this.tanRectY = y;
		this.tabRectW = w;
		this.tabRectH = h;
	}

	void handleKeyEvent(MKeyEvent key) {
		int type = key.getType();
		int keyCode = key.getCode();
		if (type == Adaptor.KEYSTATE_PRESSED) {
			switch (keyCode) {
			case Adaptor.KEY_UP:
				if (focus > 0) {
					focus--;
				}
				break;
			case Adaptor.KEY_DOWN:
				if (focus < popQueue.size() - 1) {
					focus++;
				}
				break;
			case Adaptor.KEY_NUM9:
				if (isShownSelectBox()) {
					hideSelectBox();
				}
				break;
			case Adaptor.KEY_FIRE:
				selectItem();
				hideSelectBox();
				break;
			case Adaptor.KEY_RIGHT_SOFT:
				hideSelectBox();
				break;
			}
		}
	}

	private void selectItem() {
		UserInfo selectItem = (UserInfo) popQueue.elementAt(focus);
		if (selectBoxListener != null) {
			selectBoxListener.focusItemChange(selectItem.id, selectItem.name);
		} else {
			throw new NullPointerException("selectBoxListener=null");
		}
		if (selectItem.name.length() > 10) {
			this.userNameShowInTab = selectItem.name.substring(0, 10) + "...";
		} else {
			this.userNameShowInTab = selectItem.name;
		}
	}

	boolean pointerPressed(int x, int y) {
		if (!isShownSelectBox()) {
			if (Utils.pointInRect(x, y, tabRectX, tanRectY, tabRectW, tabRectH)) {
				showSelectBox();
				this.setFocus(true);
				return true;
			}
		} else {
			for (int i = 0; i < popQueue.size(); i++) {
				if (Utils.pointInRect(x, y, popRectX, popRectY + spaceY + lineH
						* i - lineH, popRectW, lineH)) {
					this.focus = i;
					selectItem();
					hideSelectBox();
					return true;
				}
			}
		}
		return false;
	}

	void draw(MilkGraphics g) {
		// ResourceManager.loadResource();
		g.setFont(font);
		focusRect = BBResourceManager.focusRect;
		unfocusRect = BBResourceManager.unfocusRect;

		if (userNameShowInTab != null) {
			int newRectW = font.stringWidth(userNameShowInTab) + 20;
			if (tabRectW < newRectW) {
				int dx = newRectW - tabRectW;
				tabRectX -= dx;
				tabRectW = newRectW;
			}
		} else {
			if (tabRectW - DEFAULT_FRAME_WIDTH > 0) {
				int dx = tabRectW - DEFAULT_FRAME_WIDTH;
				tabRectX += dx;
				tabRectW = DEFAULT_FRAME_WIDTH;
			}
		}
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());
		RoundRect roundRect;
		if (isFocus) {
			roundRect = focusRect;
		} else {
			roundRect = unfocusRect;
		}
		roundRect.drawRoundRect(g, tabRectX, tanRectY, tabRectW, tabRectH);
		int temp = tabRectH * 2 / 3;
		int flagX = tabRectX + tabRectW - temp - 6;
		if (userNameShowInTab != null) {
			g.setColor(infoColor);
			int offset = (flagX - tabRectX - 3 - font
					.stringWidth(userNameShowInTab)) / 2;
			g.drawString(userNameShowInTab, tabRectX + 3 + offset, tanRectY, 0);
		}

		g.setColor(0x382100);
		g.drawLine(flagX, tanRectY + 3, flagX, tanRectY + tabRectH - 4);

		fillDownArrow(g, tabRectX + tabRectW - temp - 4, tanRectY + tabRectH
				/ 2, temp);

		g.setColor(notifyInfoColor);
		String showInfo = Def.privateChatSelectBoxInfo;
		g.drawString(showInfo, tabRectX - 2 - font.stringWidth(showInfo),
				tanRectY, MilkGraphics.LEFT | MilkGraphics.TOP);
		if (isShownSelectBox())
			drawSelectBox(g);
	}

	private void fillDownArrow(MilkGraphics g, int x, int y, int w) {
		g.fillTriangle(x, y, x + w, y, x + w / 2, y + w / 2);
	}

	private void drawSelectBox(MilkGraphics g) {
		MilkFont font = g.getFont();
		unfocusRect.drawRoundRect(g, popRectX, popRectY - lineH, popRectW,
				popRectH);
		for (int i = 0; i < popQueue.size(); i++) {
			UserInfo user = (UserInfo) popQueue.elementAt(i);
			String name = user.name;
			if (i == focus) {
				focusRect.drawRoundRect(g, popRectX + 1, popRectY + spaceY
						+ lineH * i + 1 - lineH, popRectW - 2, lineH - 2);
			}
			int nameX = popRectX + spaceX
					+ (maxUserNameDrawWidth - font.stringWidth(name)) / 2;
			g.setColor(infoColor);
			g.drawString(name, nameX, popRectY + spaceY + lineH * i - lineH, 0);
		}
	}

	void addItem(String name, int id) {
		if (name == null || name.length() == 0) {
			this.userNameShowInTab = String.valueOf(id);
		} else {
			if (name.length() > 10) {
				this.userNameShowInTab = name.substring(0, 10) + "...";
			} else
				this.userNameShowInTab = name;
		}
		this.boxCore.addItem(name, id);
	}

	void keyFire() {
		if (isShownSelectBox()) {
			hideSelectBox();
		} else {
			showSelectBox();
		}
	}

	private void showSelectBox() {
		popQueue.removeAllElements();
		maxUserNameDrawWidth = 0;
		Vector userQueue = boxCore.getUserInfoQueue();
		for (int i = 0; i < userQueue.size(); i++) {
			UserInfo user = (UserInfo) userQueue.elementAt(i);
			popQueue.addElement(user);
			if (maxUserNameDrawWidth < font.stringWidth(user.name)) {
				maxUserNameDrawWidth = font.stringWidth(user.name);
			}
		}

		popRectW = maxUserNameDrawWidth + spaceX * 2;
		popRectH = lineH * popQueue.size() + spaceY * 2;
		popRectX = tabRectX + tabRectW - popRectW - 7;
		popRectY = tanRectY + tabRectH - popRectH + spaceY;
	}

	private void hideSelectBox() {
		popQueue.removeAllElements();
	}

	boolean isShownSelectBox() {
		return popQueue.size() > 0;
	}

	boolean doRightSoftKey() {
		if (isShownSelectBox()) {
			hideSelectBox();
			return true;
		}
		return false;
	}

}

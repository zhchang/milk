package milk.ui.j2mechat;

import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.implement.IMEvent.MKeyEvent;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;

public class J2meTabBar {

	private static final int tabItemNameColor = 0x4a2c03;
	private static final int UNFOUS_OFFSET_Y = 0;
	private String tabItemName[];
	private int barWidth;
	private int tabItemWidth;
	private int tabItemSpace;
	private int tabBarRectX, tabBarRectY;
	private int focus;
	private J2meTab unfocusTab, focusTab;
	private int tabItemPosX[];
	private J2meTabBarListener tabListener;
	
	J2meTabBar(String tabs[], int tabBarWidth, J2meTab unfocus, J2meTab focus) {
		if (tabs == null || tabs.length == 0){
			Utils.info("--------------------------------tabs = null");
			throw new IllegalArgumentException("tabs = null");
		}
		tabItemName = tabs;
		barWidth = tabBarWidth;
		tabItemWidth = tabBarWidth / 2;
		tabItemSpace = (tabBarWidth - tabItemWidth) / (tabs.length - 1);
		unfocusTab = unfocus;
		focusTab = focus;
		tabItemPosX = new int[tabs.length];
	}
	
	void initL10nString(String tabs[]){
		tabItemName = tabs;
	}

	void setTabListener(J2meTabBarListener l) {
		tabListener = l;
	}

	void setFocus(int focus) {
		this.focus = focus;
	}

	void setPosition(int x, int y) {
		this.tabBarRectX = x;
		this.tabBarRectY = y;
		for (int i = 0; i < tabItemPosX.length; i++) {
			tabItemPosX[i] = x + tabItemSpace * i;
		}
		tabItemPosX[tabItemPosX.length - 1] = tabBarRectX + barWidth
				- tabItemWidth;
	}

	void draw(MilkGraphics g) {
		g.setColor(tabItemNameColor);
		for (int i = tabItemName.length - 1; i > focus; i--) {
			unfocusTab.drawTab(g, tabItemPosX[i],
					tabBarRectY + UNFOUS_OFFSET_Y, tabItemWidth);
			drawTabItemName(g, tabItemName[i], tabItemPosX[i], tabBarRectY
					+ UNFOUS_OFFSET_Y, 1);
		}
		for (int i = 0; i < focus; i++) {
			unfocusTab.drawTab(g, tabItemPosX[i],
					tabBarRectY + UNFOUS_OFFSET_Y, tabItemWidth);
			drawTabItemName(g, tabItemName[i], tabItemPosX[i], tabBarRectY
					+ UNFOUS_OFFSET_Y, 0);
		}
		focusTab.drawTab(g, tabItemPosX[focus], tabBarRectY, tabItemWidth);
		drawTabItemName(g, tabItemName[focus], tabItemPosX[focus], tabBarRectY,3);
	}

	void handleKeyEvent(MKeyEvent key) {
		int type = key.getType();
		int keyCode = key.getCode();
		if (type == Adaptor.KEYSTATE_PRESSED) {
			switch (keyCode) {
			case Adaptor.KEY_LEFT:
				if (focus > 0) {
					focus--;
					tabListener.focusTabChange(tabItemName[focus], focus);
				}
				break;
			case Adaptor.KEY_RIGHT:
				if (focus < tabItemName.length - 1) {
					focus++;
					tabListener.focusTabChange(tabItemName[focus], focus);
				}
				break;
			}
		}
	}

	void pointerPressed(int x, int y) {
		lastPointerDraggedX = 0;
		if (unfocusTab.isTouch(x, y, tabItemPosX[focus], tabBarRectY,
				tabItemWidth)) {
			return;
		}
		for (int i = focus - 1; i >= 0; i--) {
			if (unfocusTab.isTouch(x, y, tabItemPosX[i], tabBarRectY,
					tabItemWidth)) {
				focus = i;
				tabListener.focusTabChange(tabItemName[focus], focus);
				return;
			}
		}
		for (int i = focus + 1; i < tabItemName.length; i++) {
			if (unfocusTab.isTouch(x, y, tabItemPosX[i], tabBarRectY,
					tabItemWidth)) {
				focus = i;
				tabListener.focusTabChange(tabItemName[focus], focus);
				return;
			}
		}
	}

	private int lastPointerDraggedX;

	void pointerDragged(int x, int y) {
		if (y > 0 && y < tabBarRectY + focusTab.getHeight()) {
			if (lastPointerDraggedX == 0) {
				lastPointerDraggedX = x;
			} else if (Math.abs(lastPointerDraggedX - x) > barWidth / 8) {
				if (x > lastPointerDraggedX) {// right
					if (focus < tabItemName.length - 1) {
						focus++;
						tabListener.focusTabChange(tabItemName[focus], focus);
					}
				} else {
					if (focus > 0) {
						focus--;
						tabListener.focusTabChange(tabItemName[focus], focus);
					}
				}
				lastPointerDraggedX = x;
			}
		}
	}

	void pointerReleased(int x, int y) {
		lastPointerDraggedX = 0;
	}

	private void drawTabItemName(MilkGraphics g, String name, int x, int y,
			int anchorX) {
		MilkFont font = g.getFont();
		int strlen = font.stringWidth(name);
		int dy = (focusTab.getHeight() - font.getHeight()) / 2;
		int drawX;
		if (anchorX == 0) {
			int leftX = x + focusTab.getTriangleWidth();
			int dx = (tabItemSpace - focusTab.getTriangleWidth() - strlen) / 2;
			if (dx <= 0) {
				dx = 0;
			}
			drawX = leftX + dx;
		} else if (anchorX == 1) {//right
			int rightX = x + tabItemWidth - focusTab.getTriangleWidth();
			int dx = (tabItemSpace - focusTab.getTriangleWidth() - strlen) / 2;
			if (dx <= 0) {
				dx = 0;
			}
			drawX = rightX - dx - strlen;
		} else {
			drawX = x + (tabItemWidth - strlen) / 2;
		}
		g.drawString(name, drawX, y + dy, 0);
	}

}

package milk.ui.androidchat;

import milk.chat.core.Def;
import milk.chat.core.MenuItem;
import milk.chat.core.PopMenuListener;
import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.implement.IMEvent.MKeyEvent;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class AndroidPopMenu {

	private String title[];
	private MenuItem items[];

	private int focus;
	private int rectX, rectY, rectW, rectH;
	private static final MilkFont font = AndroidHallScreen.font;
	private static int lineH = font.getHeight();
	private static final int itemColor = 0xffffff;

	private static final int frameBorderSize = 4;
	private static final int frameSpaceSize = lineH / 4;
	private static int itemH = lineH * 2;
	private static int titleDrawWidth;
	private static int itemBackRectWidth;

	private boolean isDisplay = false;

	private int toUserId;
	private String toUserName;
	private int menuItemX, menuItemY;

	private PopMenuListener menuListener;
	
	private RoundRect menuFrame;
	private RoundRect menuUnFocus, menuFocus;
	private static AndroidPopMenu popMenu;
	private static int screenWidth, screenHeight;
	private static RoundRect btnUnFocus;
	
	private AndroidPopMenu() {
		if (btnUnFocus == null)
			btnUnFocus = Adaptor.uiFactory.createRoundRect("gray", 5, 0x844d0b);
	}

	public static void init(int w, int h) {
		screenWidth = w;
		screenHeight = h;
		titleDrawWidth = screenWidth - 40;
		itemBackRectWidth = titleDrawWidth - 40;
	}

	public static AndroidPopMenu getInstance() {
		if (popMenu == null) {
			popMenu = new AndroidPopMenu();
		}
		return popMenu;
	}
	


	public void initPopMenu(String info, MenuItem items[], int toId,
			String toName, PopMenuListener l,int bottomY) {
		this.title = Utils.autoNewLine(info, font, titleDrawWidth, '\n');
		this.items = items;
		this.toUserId = toId;
		toUserName = toName;
		setMenuFrameRect(bottomY);
		menuListener = l;
		
	}

	private void setMenuFrameRect(int bottomY) {
		lineH = font.getHeight();
		if (screenHeight > screenWidth) {
			itemH = lineH + lineH;
		} else {
			itemH = lineH + lineH / 2;
		}
		rectH = (items.length + title.length) * itemH + 2 * frameSpaceSize + 2
				* frameBorderSize + 10;
		rectY = bottomY - rectH;
		rectW = titleDrawWidth + 2 * frameSpaceSize + 2 * frameBorderSize;
		rectX = (screenWidth - rectW) / 2;
	}
	

	public void showPopNotifyInfo(String info,int bottomY) {
		initPopMenu(info, 
				new MenuItem[] { new MenuItem(Def.cmdOk,PopMenuListener.actionCancel) }, 
				0, "popToName",null,bottomY);
		show();
	}

	boolean isShown() {
		return isDisplay;
	}

	public void show() {
		isDisplay = true;
		focus = 0;
	}

	void hide() {
		isDisplay = false;
	}

	void handleKeyEvent(MKeyEvent key) {
		int type = key.getType();
		int keyCode = key.getCode();
		if (type == Adaptor.KEYSTATE_PRESSED) {
			if (keyCode == Adaptor.KEY_FIRE)
				fireKey();
			else {
				switch (keyCode) {
				case Adaptor.KEY_UP:
					if (focus > 0)
						focus--;
					break;
				case Adaptor.KEY_DOWN:
					if (focus < items.length - 1)
						focus++;
					break;
				}
			}
		}
	}

	private void fireKey() {
		if (menuListener != null) {
			menuListener.handlePopMenuEvent(items[focus], toUserId, toUserName,focus);
		}
//		else {
//			throw new NullPointerException("chatPopMenuListnener=null");
//		}
		hide();
	}

	void pointerPressed(int x, int y) {
		for (int i = 0; i < items.length; i++) {
			if (Utils.pointInRect(x, y, menuItemX, menuItemY + itemH * i,
					itemBackRectWidth, itemH)) {
				this.focus = i;
				fireKey();
				return;
			}
		}
	}

	void draw(MilkGraphics g) {
		g.setFont(font);
//		setMenuFrameRect();
		menuFrame = AndroidResourceManager.menuFrame;
		menuUnFocus = AndroidResourceManager.btnUnFocus;
		menuFocus = AndroidResourceManager.focusRect;
		menuFrame.drawRoundRect(g, rectX, rectY, rectW, rectH);
		int titleY = rectY + frameSpaceSize + frameBorderSize;
		drawMenuTitle(g, titleY);

		int lineY = titleY + title.length * lineH + frameSpaceSize;
		int lineWidth = rectW - frameSpaceSize * 2 - frameBorderSize * 2;
		int lineX = rectX + (rectW - lineWidth) / 2;
		menuFrame.drawHorizonLine(g, lineX, lineY, lineWidth);

		menuItemY = lineY + lineH / 2;
		drawMenuItems(g, menuItemY);
	}

	private void drawMenuItems(MilkGraphics g, int y) {
		menuItemX = (screenWidth - itemBackRectWidth) / 2;
		for (int i = 0; i < items.length; i++) {
			RoundRect itemBack;
			if (i == focus) {
				itemBack = menuFocus;
			} else {
				itemBack = menuUnFocus;
			}
			itemBack.drawRoundRect(g, menuItemX, y + itemH * i + 2,
					itemBackRectWidth, itemH - 4);
			g.setColor(itemColor);
			g.drawString(items[i].showName,
					(screenWidth - font.stringWidth(items[i].showName)) / 2, y
							+ itemH * i + (itemH - lineH) / 2 - 1, 0);
		}
	}

	private void drawMenuTitle(MilkGraphics g, int y) {
		g.setColor(itemColor);
		for (int i = 0; i < title.length; i++) {
			g.drawString(title[i],
					(screenWidth - font.stringWidth(title[i])) / 2, y + lineH
							* i, 0);
		}
	}

}

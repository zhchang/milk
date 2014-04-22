package milk.ui.androidchat;

import milk.chat.core.Utils;

import milk.ui.MilkImageImpl;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class AndroidTab {
	
	private static final int tabNameColor = 0xffffff;
	private static final int tabNameFocusColor = 0x000000;
	private static int sizeWidth, sizeHeight;
	private static MilkImage bg, bgFocus;
	private int tabX;
	private int tabY;
	private MilkImage icon, iconFocus;
	private boolean focus;

	private String name;

	static double resizeFactor;
	
	static void init(int tabWidth) {
		sizeWidth = tabWidth;
		sizeHeight = (sizeWidth * AndroidResourceManager.tab.getHeight())
				/ AndroidResourceManager.tab.getWidth();
		resizeFactor=((double)sizeWidth)/AndroidResourceManager.tab.getWidth();
		if (resizeFactor < 0.75) {
			resizeFactor = 0.75;
		}
//		Utils.info("--AndroidTab tabWidth:"+tabWidth+"/image width"+AndroidResourceManager.tab.getWidth());
		if (bg == null) {
			MilkImageImpl.resizeImage(AndroidResourceManager.tab, tabWidth, sizeHeight);
			MilkImageImpl.resizeImage(AndroidResourceManager.tabFocus, tabWidth, sizeHeight);
			bg = AndroidResourceManager.tab;
			bgFocus = AndroidResourceManager.tabFocus;
		}
	}
	
	static double getResizeFactor(){
		return resizeFactor;
	}

	static int getTabHeight() {
		return sizeHeight;
	}

	static int getTabWidth() {
		return sizeWidth;
	}

	AndroidTab(MilkImage icon, MilkImage iconFocus) {
		this.icon = icon;
		this.iconFocus = iconFocus;
	}

	void setTabName(String name) {
		this.name = name;
	}

	void setFocus(boolean isFocus) {
		focus = isFocus;
	}

	void setPosition(int index, int y) {
		this.tabX = index * sizeWidth;
		this.tabY = y;
	}

	void draw(MilkGraphics g) {
		MilkImage iconImg, bgImg;
		if (this.focus) {
			bgImg = bgFocus;
		} else {
			bgImg = bg;
		}
		if (this.focus&&(System.currentTimeMillis() / 500) % 2 > 0) {
			iconImg = iconFocus;
		} else {
			iconImg = icon;
		}
		MilkFont font = g.getFont();
		g.drawImage(bgImg, tabX, tabY, 0);
		int dx = (bg.getWidth() - icon.getWidth()) / 2;
		int dy = (bg.getHeight() - icon.getHeight() - font.getHeight()) / 3;
		g.drawImage(iconImg, tabX + dx, tabY + dy, 0);
		if (this.focus)
		    g.setColor(tabNameFocusColor);
		else 
			g.setColor(tabNameColor);
		g.drawString(name, tabX + (bg.getWidth() - font.stringWidth(name)) / 2,
				tabY + dy + icon.getHeight() + dy, 0);
	}

	private int getHeight() {
		return bg.getHeight();
	}

	private int getWidth() {
		return bg.getWidth();
	}

	boolean isTouch(int x, int y) {
		if (Utils.pointInRect(x, y, tabX, tabY, getWidth(), getHeight())) {
			return true;
		}
		return false;
	}

}

package milk.menu;

import milk.implement.Adaptor;

import milk.implement.mk.MArray;
import milk.implement.mk.MMap;
import milk.implement.sv3.MilkImageUtil;
//import milk.ui.RoundRectImpl;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.RoundRect;
import smartview3.utils.IImageRequester;

public class PopMenu implements IImageRequester {

	private static final int itemsDivisionLineColor = 0x492609;
	private static final int contentBackFocusColor = 0x85552e;
	private static final int contentBackUnFocusColor = 0x723D19;
	private static final int itemsNameFocusColor = 0xffeacb;
	private static final int itemsNameUnFocusColor = 0x000000;
	private static final int itemSpaceSizeY = 6;
	private static final int itemSpaceSizeX = 5;
	private static final int frameTotalSize = 5;
	private static final int frameBorderSize = 3;

	private int itemHeight;
	private MenuItem menuItems[];
	private MilkFont font;
	private int leftX, topY, rectW, rectH, bottomY;
	// private MilkSprite corner, horizontalLine, verticalLine;
	public boolean isDisplay = false;
	private int focus;

	private String windowTarget;
	// private MilkImage imgHomePage;
	private RoundRect pith9Rect;


	protected PopMenu(String windowTarget) {
		this.windowTarget = windowTarget;
		MilkImageUtil.getInstance().loadImageAsync("menu-bg", this, 0, 0);
		// MilkImageUtil.getInstance().loadImageAsync("menuhome", this, 0, 0);
	}

	protected void setMenuItems(MArray menus, MilkFont font, int leftX,
			int bottomY) {
		int count = menus.size();
		this.menuItems = new MenuItem[count];
		for (int i = 0; i < count; i++) {
			MMap map = menus.getMap(i);
			int width = 0;
			int height = 0;
			if (map.hasKey("width")) {
				try {
					width = map.getInt("width");
				} catch (Exception e) {
				}
			}
			if (map.hasKey("height")) {
				try {
					height = map.getInt("height");
				} catch (Exception e) {
				}
			}
			try {
				menuItems[i] = new MenuItem(map.getString("caption"),
						map.getString("action"), map.getString("icon"), width,
						height);
			} catch (Exception e) {
			}
		}
		this.font = font;
		this.leftX = leftX;
		this.bottomY = bottomY;
		isDisplay = false;
		calculateCoordinate();
	}

	private void calculateCoordinate() {
		int maxItemsWidth = getMaxItemsWidth();
		rectW = maxItemsWidth + frameTotalSize * 2 + itemSpaceSizeX * 4;
		itemHeight = menuItems[0].getItemHeight(font) + itemSpaceSizeY;
		rectH = menuItems.length * itemHeight + frameBorderSize * 2;
		topY = bottomY - rectH;
	}

	protected MenuItem getFocusedItem() {
		return menuItems[focus];
	}

	protected void moveFocusUp() {
		if (focus > 0) {
			focus--;
		}
	}

	protected void moveFocusDown() {
		if (focus < menuItems.length - 1) {
			focus++;
		}
	}

	protected int getFocus() {
		return focus;
	}

	protected void setFocus(int newFocus) {
		this.focus = newFocus;
	}

	protected int getItemCount() {
		if(menuItems==null)
			return 0;
		return menuItems.length;
	}

	protected boolean pointerPressed(int x, int y) {
		for (int i = 0; i < menuItems.length; i++) {
			int itemY = topY + frameBorderSize + itemHeight * i;
			if (WindowMenu.pointInRect(x, y, leftX, itemY, rectW, itemHeight)) {
				focus = i;
				trigger();
				return true;
			}
		}
		return false;
	}

	protected void draw(MilkGraphics g) {
		saveClip(g);

		g.setFont(font);
		drawFrameBorderAndCorner(g, leftX, topY, rectW, rectH);

		int areaX = leftX + frameBorderSize;
		int areaY = topY + frameBorderSize;
		int areaW = rectW - 2 * frameBorderSize;
		int areaH = rectH - 2 * frameBorderSize;
		fillContentArea(g, areaX, areaY, areaW, areaH);

		int itemX = leftX + frameBorderSize + itemSpaceSizeX;
		int itemY = topY + frameBorderSize;
		int itemW = rectW - 2 * frameBorderSize - itemSpaceSizeX * 2;
		drawItemsAndDivLines(g, itemX, itemY, itemW, rectH);

		restoreClip(g);
	}

	protected void show() {
		if (menuItems != null) {
			isDisplay = true;
		}
	}

	protected void trigger() {
		if (menuItems != null) {
			MenuItem item = getFocusedItem();
			MMap command = new MMap();
			command.set("menu-action", item.getItemAction());
			Adaptor.getInstance().sendCommand(windowTarget, command);
		}
		hide();

	}

	protected void hide() {
		isDisplay = false;
	}

	protected boolean isShown() {
		return isDisplay;
	}

	private void drawFrameBorderAndCorner(MilkGraphics g, int x, int y, int w,
			int h) {
		if (pith9Rect != null)
			pith9Rect.drawRoundRect(g, x, y, w, h);
		// g.setClip(x + frameTotalSize, y, w - frameTotalSize * 2, h);
		// int drawTemp = x;
		// while ((x + horizontalLine.getWidth()) < w + drawTemp) {// up,down
		// // border
		// horizontalLine.setTransform(MilkSprite.TRANS_NONE);
		// horizontalLine.setPosition(x + frameTotalSize, y);
		// horizontalLine.paint(g);
		// horizontalLine.setTransform(MilkSprite.TRANS_ROT180);
		// horizontalLine.setPosition(x + frameTotalSize, y + h
		// - frameTotalSize);
		// horizontalLine.paint(g);
		// x += horizontalLine.getWidth();
		// }
		// x = drawTemp;
		//
		// drawTemp = y;
		// g.setClip(x, y + frameTotalSize, w, h - frameTotalSize * 2);
		// while (y + verticalLine.getHeight() < h + drawTemp) {// left,right
		// // border
		// verticalLine.setTransform(MilkSprite.TRANS_NONE);
		// verticalLine.setPosition(x, y + frameTotalSize);
		// verticalLine.paint(g);
		// verticalLine.setTransform(MilkSprite.TRANS_ROT180);
		// verticalLine
		// .setPosition(x + w - frameTotalSize, y + frameTotalSize);
		// verticalLine.paint(g);
		// y += verticalLine.getHeight();
		// }
		// y = drawTemp;
		// g.setClip(x, y, w, h);
		//
		// // 4 corner
		// corner.setTransform(MilkSprite.TRANS_NONE);
		// corner.setPosition(x, y);
		// corner.paint(g);
		//
		// corner.setTransform(MilkSprite.TRANS_ROT90);
		// corner.setPosition(x + w - corner.getWidth(), y);
		// corner.paint(g);
		//
		// corner.setTransform(MilkSprite.TRANS_ROT180);
		// corner.setPosition(x + w - corner.getWidth(),
		// y + h - corner.getHeight());
		// corner.paint(g);
		//
		// corner.setTransform(MilkSprite.TRANS_ROT270);
		// corner.setPosition(x, y + h - corner.getHeight());
		// corner.paint(g);

	}

	private void fillContentArea(MilkGraphics g, int x, int y, int w, int h) {
		// // fill all back
		// g.setColor(contentBackUnFocusColor);
		// g.fillRoundRect(x, y, w, h, frameBorderSize, frameBorderSize);

		// fill focus back
		g.setColor(contentBackFocusColor);
		if (focus == 0) {
			g.fillRoundRect(x, y + itemHeight * focus, w, itemHeight,
					frameBorderSize, frameBorderSize);
			g.fillRect(x, y + itemHeight * focus + frameBorderSize, w,
					itemHeight - frameBorderSize);
		} else if (focus == menuItems.length - 1) {
			g.fillRoundRect(x, y + itemHeight * focus, w, itemHeight,
					frameBorderSize, frameBorderSize);
			g.fillRect(x, y + itemHeight * focus, w, itemHeight
					- frameBorderSize);
		} else {
			g.fillRect(x, y + itemHeight * focus, w, itemHeight);
		}
	}

	private void drawItemsAndDivLines(MilkGraphics g, int x, int y, int w, int h) {
		for (int i = 0; i < menuItems.length; i++) {
			int color = itemsNameUnFocusColor;
			if (i == focus) {
				color = itemsNameFocusColor;
			}
			int itemY = y + i * itemHeight;
			menuItems[i].draw(g, x, w, itemY, color, itemHeight,
					itemSpaceSizeX, i + 1);
			if (i < menuItems.length - 1) {
				int lineY = itemY + itemHeight;
				g.setColor(itemsDivisionLineColor);
				g.drawLine(x, lineY, x + w, lineY);
			}
		}
	}

	private int getMaxItemsWidth() {
		int temp = menuItems[0].getItemWidth(font);
		for (int i = 0; i < menuItems.length; i++) {
			if (temp < menuItems[i].getItemWidth(font)) {
				temp = menuItems[i].getItemWidth(font);
			}
		}
		return temp;
	}

	private int saveX, saveY, saveWidth, saveHeight;

	private void saveClip(MilkGraphics g) {
		saveX = g.getClipX();
		saveY = g.getClipY();
		saveWidth = g.getClipWidth();
		saveHeight = g.getClipHeight();
	}

	private void restoreClip(MilkGraphics g) {
		g.setClip(saveX, saveY, saveWidth, saveHeight);
	}

	public void didReceiveImage(Object image, String src) {
		if ("menu-bg".equals(src)) {
			MilkImage back = (MilkImage) image;
			pith9Rect = Adaptor.uiFactory.createRoundRect(back, frameTotalSize,
					contentBackUnFocusColor);
			// int size = frameTotalSize;
			// MilkImage cornerImage = MilkImage.createImage(back, 0, 0, size,
			// size, MilkSprite.TRANS_NONE);
			// corner = new MilkSprite(cornerImage);
			// MilkImage frameImage = MilkImage.createImage(back, size, 0,
			// back.getWidth() - size * 2, size, MilkSprite.TRANS_NONE);
			// horizontalLine = new MilkSprite(frameImage);
			//
			// frameImage = MilkImage.createImage(back, 0, size, size,
			// back.getHeight() - 2 * size, MilkSprite.TRANS_NONE);
			// verticalLine = new MilkSprite(frameImage);
		}
		// else if ("menuhome".equals(src)) {
		// imgHomePage = (MilkImage) image;
		// }
	}

}

package milk.menu;

import milk.implement.sv3.MilkImageUtil;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import smartview3.utils.IImageRequester;

public class MenuItem implements IImageRequester {

	private MilkImage itemIcon;
	private String itemName;
	private String itemAction;
	
	public static boolean showNumberPostfix=true;

	public String getItemAction() {
		return itemAction;
	}

	public void setItemAction(String itemAction) {
		this.itemAction = itemAction;
	}

	protected MenuItem(String name, String action, String iconUrl, int width,
			int height) {
		this.itemName = name;
		this.itemAction = action;
		MilkImageUtil.getInstance()
				.loadImageAsync(iconUrl, this, width, height);
	}

	protected String getItemName() {
		return itemName;
	}

	protected void draw(MilkGraphics g, int x, int w, int y, int color,
			int itemHeight, int space, int position) {
		int oldX = x;
		if (itemIcon != null) {
			int iconY = (itemHeight - itemIcon.getHeight()) / 2 + y;
			g.drawImage(itemIcon, x, iconY, MilkGraphics.LEFT
					| MilkGraphics.TOP);
			x += itemIcon.getWidth() + space;
		}
		int nameY = (itemHeight - g.getFont().getHeight()) / 2 + y;
		g.setColor(color);
		g.drawString(itemName, x, nameY, MilkGraphics.LEFT | MilkGraphics.TOP);
		if(showNumberPostfix){
			String strPos = String.valueOf(position);
			int offset = g.getFont().stringWidth(strPos) / 2;
			g.drawString(strPos, oldX + w - space - offset, nameY,
					MilkGraphics.LEFT | MilkGraphics.TOP);
		}
	}

	protected int getItemWidth(MilkFont font) {
		int size = font.stringWidth(itemName);
		if (itemIcon != null)
			size += itemIcon.getWidth();
		// #if polish.blackberry
		// #else
		size += font.stringWidth("3");
		// #endif
		return size;
	}

	protected int getItemHeight(MilkFont font) {
		int size = 0;
		if (itemIcon != null)
			size = itemIcon.getHeight();
		if (font.getHeight() > size)
			size = font.getHeight();
		return size;
	}

	public void didReceiveImage(Object image, String src) {
		itemIcon = (MilkImage) image;
	}

}

package milk.ui;

import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import net.rim.device.api.ui.Graphics;

//import net.rim.device.api.ui.XYRect;

public class MilkGraphicsImpl implements MilkGraphics {

	private Graphics g;
	private MilkFont last = MilkFontImpl.getFont(MilkFont.STYLE_PLAIN,
			MilkFont.SIZE_SMALL);;
	// private XYRect clipRect = new XYRect();

	public static final int LEFT = javax.microedition.lcdui.Graphics.LEFT;
	public static final int RIGHT = javax.microedition.lcdui.Graphics.RIGHT;
	public static final int TOP = javax.microedition.lcdui.Graphics.TOP;
	public static final int BOTTOM = javax.microedition.lcdui.Graphics.BOTTOM;
	public static final int HCENTER = javax.microedition.lcdui.Graphics.HCENTER;
	public static final int VCENTER = javax.microedition.lcdui.Graphics.VCENTER;

	public Graphics getG() {
		return g;
	}

	public void setG(Graphics g) {
		this.g = g;
	}

	public MilkGraphicsImpl() {
	}

	public void setColor(int color) {
		g.setColor(color);
	}

	public void drawLine(int x, int y, int x1, int y1) {
		g.drawLine(x, y, x1, y1);
	}

	public void drawString(String input, int x, int y, int anchor) {
		g.drawText(input, x, y, anchor);
	}

	public void drawSubstring(String input, int offset, int length, int x,
			int y, int anchor) {
		g.drawText(input, offset, length, x, y, anchor, -1);
	}

	public void drawRect(int x, int y, int w, int h) {
		g.drawRect(x, y, w, h);
	}

	public void fillRect(int x, int y, int w, int h) {
		g.fillRect(x, y, w, h);
	}

	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		g.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {
		g.drawRoundRect(x, y, w, h, aw, ah);
	}

	public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
		g.fillRoundRect(x, y, w, h, aw, ah);
	}

	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
		// int[] xPts = { x1, x2, x3 };
		// int[] yPts = { y1, y2, y3 };
		// int[] colors = { g.getColor(), g.getColor(), g.getColor() };
		// g.drawShadedFilledPath(xPts, yPts, null, colors, null);
		int[] xPositions = new int[] { x1, x2, x3 };
		int[] yPositions = new int[] { y1, y2, y3 };
		this.g.drawFilledPath(xPositions, yPositions, null, null);
	}

	public void setFont(MilkFont font) {
		last = font;
		g.setFont(((MilkFontImpl) font).font);
	}

	public MilkFont getFont() {
		((MilkFontImpl) last).font = g.getFont();
		return last;
	}

	public void drawImage(MilkImage image, int x, int y, int anchor) {
		if ((anchor & RIGHT) > 0) {
			x -= image.getWidth();
		} else if ((anchor & HCENTER) > 0) {
			x -= image.getWidth() / 2;
		}
		if ((anchor & BOTTOM) > 0) {
			y -= image.getHeight();
		} else if ((anchor & VCENTER) > 0) {
			y -= image.getHeight() / 2;
		}

		g.drawBitmap(x, y, image.getWidth(), image.getHeight(),
				((MilkImageImpl) image).bitmap, 0, 0);
	}

	public void drawRGB(int[] rgbData, int offset, int scanlength, int x,
			int y, int width, int height, boolean processAlpha) {
		g.drawARGB(rgbData, offset, scanlength, x, y, width, height);
	}

	public void translate(int x, int y) {
		g.translate(x, y);
	}

	public int getTranslateX() {
		return g.getTranslateX();
	}

	public int getTranslateY() {
		return g.getTranslateY();
	}

	public int getClipWidth() {
		return g.getClippingRect().width;
	}

	public int getClipHeight() {
		return g.getClippingRect().height;
	}

	public int getClipX() {
		return g.getClippingRect().x;
	}

	public int getClipY() {
		return g.getClippingRect().y;
	}

	public void setClip(int x, int y, int w, int h) {

		this.clipX = x;
		this.clipY = y;
		this.clipWidth = w;
		this.clipHeight = h;

		int color = g.getColor();
		try {
			if (g.getContextStackSize() > 0) {
				g.popContext();
			}
		} catch (Exception e) {
			System.out.println("Unable to pop clipping context" + e);
		}
		g.pushContext(x, y, w, h, 0, 0);
		g.setColor(color);

	}

	private int clipX, clipY, clipWidth, clipHeight;

	public void clipRect(int x, int y, int width, int height) {

		// g.getAbsoluteClippingRect(clipRect);
		// int clipx = Math.max(x, clipRect.x);
		// int clipWidth = Math.min(w, clipRect.width);
		// int clipy = Math.max(y, clipRect.y);
		// int clipHeight = Math.min(h, clipRect.height);
		//
		// int color = g.getColor();
		// if (g.getContextStackSize() > 0) {
		// g.popContext();
		// }
		// g.pushContext(clipx, clipy, clipWidth, clipHeight, 0, 0);
		// g.setColor(color);

		if (x < this.clipX) {
			width -= (this.clipX - x);
			x = this.clipX;
		}
		if (y < this.clipY) {
			height -= (this.clipY - y);
			y = this.clipY;
		}
		if (x + width > this.clipX + this.clipWidth) {
			width = this.clipX + this.clipWidth - x;
		}
		if (y + height > this.clipY + this.clipHeight) {
			height = this.clipY + this.clipHeight - y;
		}
		setClip(x, y, width, height);
	}

	public void resize(int ratio) {
		// TODO Auto-generated method stub

	}

	public void resetSize() {
		// TODO Auto-generated method stub

	}

	public void drawImage(MilkImage image, int x, int y, int anchor, int alpha) {
		// TODO Auto-generated method stub
		
	}

	public void save() {
		// TODO Auto-generated method stub
		
	}

	public void restore() {
		// TODO Auto-generated method stub
		
	}

}

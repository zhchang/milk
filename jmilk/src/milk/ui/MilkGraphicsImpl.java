package milk.ui;

import javax.microedition.lcdui.Graphics;

import milk.implement.Adaptor;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class MilkGraphicsImpl implements MilkGraphics {

	private Graphics g;
	private MilkFont last = Adaptor.uiFactory.getFont(MilkFont.STYLE_PLAIN,
			MilkFont.SIZE_SMALL);

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

	public void setClip(int x, int y, int w, int h) {
		g.setClip(x, y, w, h);
	}

	public void drawLine(int x, int y, int x1, int y1) {
		g.drawLine(x, y, x1, y1);
	}

	public void drawString(String input, int x, int y, int anchor) {
		g.drawString(input, x, y, anchor);
	}

	public void drawSubstring(String input, int offset, int length, int x,
			int y, int anchor) {
		g.drawSubstring(input, offset, length, x, y, anchor);
	}

	public int getClipWidth() {
		return g.getClipWidth();
	}

	public int getClipHeight() {
		return g.getClipHeight();
	}

	public int getClipX() {
		return g.getClipX();
	}

	public int getClipY() {
		return g.getClipY();
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
		g.fillTriangle(x1, y1, x2, y2, x3, y3);
	}

	public void clipRect(int x, int y, int w, int h) {
		g.clipRect(x, y, w, h);
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
		g.drawImage(((MilkImageImpl) image).image, x, y, anchor);
	}

	public void drawRGB(int[] rgbData, int offset, int scanlength, int x,
			int y, int width, int height, boolean processAlpha) {
		g.drawRGB(rgbData, offset, scanlength, x, y, width, height,
				processAlpha);
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

	public void resize(int ratio) {
		// do nothing for j2me.
	}

	public void resetSize() {
		// do nothing
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

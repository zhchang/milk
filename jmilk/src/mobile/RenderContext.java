package mobile;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;

/**
 * 
 * @author luyx
 */
public class RenderContext extends IRenderContext {

	protected MilkGraphics g;

	private Rect clip = new Rect();

	public RenderContext(MilkGraphics g, Rect bounding, Rect viewPort) {
		super(bounding, viewPort);
		this.g = g;
	}

	public void setColor(int c) {
		g.setColor(c);
	}

	public void drawLine(int i, int j, int k, int l) {
		g.drawLine(i, j, k, l);
	}

	public void drawString(String title, int x, int y) {
		g.drawString(title, x, y, MilkGraphics.TOP | MilkGraphics.LEFT);
	}

	public void drawSubstring(String title, int offset, int length, int x, int y) {
		g.drawSubstring(title, Math.min(title.length(), offset),
				Math.min(title.length(), length), x, y, MilkGraphics.TOP
						| MilkGraphics.LEFT);
	}

	public int getClipHeight() {
		return g.getClipHeight();
	}

	public int getClipWidth() {
		return g.getClipWidth();
	}

	public int getClipY() {
		return g.getClipY();
	}

	public int getClipX() {
		return g.getClipX();
	}

	public Object getGraphics() {
		return g;
	}

	public void clipRect(int x, int y, int width, int height) {
		g.clipRect(x, y, width, height);
	}

	public void setClip(int x, int y, int width, int height) {
		g.setClip(x, y, width, height);
	}

	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}

	public void drawRect(int x, int y, int width, int height) {
		g.drawRect(x, y, width, height);
	}

	public int getLeft() {
		return MilkGraphics.LEFT;
	}

	public int getTop() {
		return MilkGraphics.TOP;
	}

	public int getRight() {
		return MilkGraphics.RIGHT;
	}

	public int getBottom() {
		return MilkGraphics.BOTTOM;
	}

	private RenderContext subCtx;

	public synchronized IRenderContext getSubCtx(Rect padded, Rect viewport) {
		if (subCtx == null) {
			subCtx = new RenderContext(g, padded, viewport);
		} else {
			subCtx.setGraphics(g);
			subCtx.getBounding().copy(padded);
			subCtx.getViewPort().copy(viewport);
		}
		return subCtx;
	}

	protected void setGraphics(MilkGraphics g) {
		this.g = g;
	}

	public void setFont(MilkFont font) {
		g.setFont(font);
	}

	public void drawImage(Object image, int x, int y) {
		g.drawImage((MilkImage) image, x, y, MilkGraphics.TOP
				| MilkGraphics.LEFT);
	}

	public void drawImage(Object image, int x, int y, int w, int h) {
		g.drawImage((MilkImage) image, x + w / 2, y + h / 2,
				MilkGraphics.HCENTER | MilkGraphics.VCENTER);
	}

	public void storeClip() {
		clip.copy(g.getClipX(), g.getClipY(), g.getClipWidth(),
				g.getClipHeight());
	}

	public void restoreClip() {
		g.setClip(clip.x, clip.y, clip.width, clip.height);
	}

}

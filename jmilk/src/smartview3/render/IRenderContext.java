/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartview3.render;

import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;

import smartview3.layout.Rect;


/**
 * 
 * @author luyx
 */
public abstract class IRenderContext {
	/**
	 * <code>
     // TableGenerator.java
     public class TableGenerator {
         public static void main(String[] args) {
             int lasty = -1;
             int count = 0;
             System.out.println("private static short[] RADIUS_TABLE = {");
             for(int i = 0; i <= 100000; ++i) {
                 double t = i / 100000.0f; 
                 double x = 1 - Math.sin(t * Math.PI / 2);
                 double y = 1 - Math.cos(t * Math.PI / 2);
                 int yout = (int)(y * 65);
                 if(yout != lasty) {
                     System.out.print((int)(x * 10000) + ", ");
                     lasty = yout;
                     ++count;
                     if(count % 8 == 0) System.out.println();
                 }
             }
             System.out.println("};");
         }
     }
     * </code>
	 */
	private static short[] RADIUS_TABLE = { 10000, 8252, 7538, 6996, 6546,
			6153, 5803, 5485, 5193, 4922, 4670, 4433, 4210, 3999, 3800, 3610,
			3429, 3256, 3092, 2934, 2783, 2639, 2500, 2367, 2240, 2117, 1999,
			1886, 1778, 1673, 1573, 1477, 1384, 1295, 1210, 1128, 1050, 975,
			903, 834, 769, 706, 646, 590, 536, 485, 436, 391, 348, 307, 269,
			234, 202, 171, 144, 119, 96, 76, 58, 42, 29, 18, 10, 4, 1, };

	protected final Rect bounding;
	protected final Rect viewPort;

	protected final Rect absRect = new Rect();
	protected final Rect paddedRect = new Rect();
	protected final Rect textBoundRect = new Rect();

	public IRenderContext(Rect bounding, Rect viewPort) {
		this.bounding = new Rect(bounding);
		this.viewPort = new Rect(viewPort);
	}

	public Rect getTextBoundRect() {
		return textBoundRect;
	}

	public Rect getAbsRect() {
		return absRect;
	}

	public Rect getPaddedRect() {
		return paddedRect;
	}

	public Rect getBounding() {
		return bounding;
	}

	public Rect getViewPort() {
		return viewPort;
	}


	public abstract void setColor(int c);

	public abstract void drawLine(int x1, int y1, int x2, int y2);

	public abstract void drawString(String title, int x, int y);

	public abstract void drawSubstring(String title, int offset, int length,
			int x, int y);

	public abstract int getClipHeight();

	public abstract int getClipWidth();

	public abstract int getClipY();

	public abstract int getClipX();

	public abstract Object getGraphics();

	public abstract void clipRect(int x, int y, int width, int height);

	public abstract void setClip(int x, int y, int width, int height);

	public abstract void storeClip();

	public abstract void restoreClip();

	public abstract void fillRect(int x, int y, int width, int height);

	public abstract void drawRect(int x, int y, int width, int height);

	// public abstract IRenderContext copy(Rect padded, Rect rectWithOffset,
	// IFontUtil fu2);
	public abstract IRenderContext getSubCtx(Rect padded, Rect rectWithOffset);

	private short[] tempShorts = new short[4];

	public short[] resetAndGetTempShortArray() {
		tempShorts[0] = tempShorts[1] = tempShorts[2] = tempShorts[3] = 0;
		return tempShorts;
	}

	public abstract void setFont(MilkFont font);

	public abstract void drawImage(Object image, int x, int y);

	public abstract void drawImage(Object image, int x, int y, int width,
			int height);

	public void fillGradientRect(Rect r, int bgColor, int bgColor2) {
		if (bgColor >= 0) {
			if (bgColor2 >= 0) {
				int r1 = (bgColor >> 16) & 0xff;
				int g1 = (bgColor >> 8) & 0xff;
				int b1 = bgColor & 0xff;
				int r2 = (bgColor2 >> 16) & 0xff;
				int g2 = (bgColor2 >> 8) & 0xff;
				int b2 = bgColor2 & 0xff;
				int left = r.x;
				int right = r.getRight();
				int top = r.y;
				int h = r.height - 1;
				// gradient
				for (int i = 0; i <= h; ++i) {
					int h_i = h - i;
					int c = ((r1 * h_i / h + r2 * i / h) << 16)
							| ((g1 * h_i / h + g2 * i / h) << 8)
							| (b1 * h_i / h + b2 * i / h);
					this.setColor(c);
					this.drawLine(left, top + i, right - 1, top + i);
				}
			} else {
				this.setColor(bgColor);
				this.fillRect(r.x, r.y, r.width, r.height);
			}
		}
	}

	public void drawRoundRect(Rect r, int radius, int bgColor) {
		MilkGraphics g = (MilkGraphics) this.getGraphics();
		g.setColor(bgColor);
		g.drawRoundRect(r.x, r.y, r.width, r.height, radius, radius);
	}

	public void fillRoundRect(Rect r, int radius, int bgColor) {
		MilkGraphics g = (MilkGraphics) this.getGraphics();
		g.setColor(bgColor);
		g.fillRoundRect(r.x, r.y, r.width, r.height, radius, radius);
	}

	public void fillRoundedRect(Rect r, short[] radius, int bgColor,
			int bgColor2, int highlightColor) {
		if (bgColor >= 0) {
			int r1 = (bgColor >> 16) & 0xff;
			int g1 = (bgColor >> 8) & 0xff;
			int b1 = bgColor & 0xff;
			int r2 = (bgColor2 >> 16) & 0xff;
			int g2 = (bgColor2 >> 8) & 0xff;
			int b2 = bgColor2 & 0xff;
			int left = r.x;
			int right = r.getRight();
			int top = r.y;
			int h = r.height;
			int w = r.width;
			boolean blend = bgColor2 >= 0 && bgColor != bgColor2;
			// gradient
			int rh = (highlightColor >> 16) & 0xff;
			int gh = (highlightColor >> 8) & 0xff;
			int bh = highlightColor & 0xff;
			for (int i = 0; i < h; ++i) {
				int h_i = h - i - 1;
				int h_2 = h / 2;
				int c;

				int offset_left = 0, offset_right = 0; // offset caused by
														// corner radius
				{
					int tableSize = RADIUS_TABLE.length - 1;
					if (i <= h_2) {
						short tl = radius[0];// top-left
						short tr = radius[1];// top-right
						// top corners
						if (tl > 0 && i < tl) {
							int index = i * tableSize / tl + 1;
							offset_left = (tl * RADIUS_TABLE[index] / 1000 + 6) / 10;
						}
						if (tr > 0 && i < tr) {
							int index = i * tableSize / tr + 1;
							offset_right = (tr * RADIUS_TABLE[index] / 1000 + 6) / 10;
						}
					} else {
						short br = radius[2];// bottom-left
						short bl = radius[3];// bottom-right
						// bottom corners
						if (bl > 0 && h_i < bl) {
							int index = h_i * tableSize / bl + 1;
							offset_left = (bl * RADIUS_TABLE[index] / 1000 + 6) / 10;
						}
						if (br > 0 && h_i < br) {
							int index = h_i * tableSize / br + 1;
							offset_right = (br * RADIUS_TABLE[index] / 1000 + 6) / 10;
						}
					}

					int sum = offset_left + offset_right;
					// offset sum must not exceed width
					if (sum > 0 && sum > w) {
						// pro-rata
						offset_left = w * offset_left / sum;
						offset_right = w * offset_right / sum;
					}
				}
				int resolvedLeft = left + offset_left;
				int resolvedRight = right - offset_right - 1;
				if (resolvedRight > resolvedLeft) {
					if (blend) {
						c = ((r1 * h_i / h + r2 * i / h) << 16)
								| ((g1 * h_i / h + g2 * i / h) << 8)
								| (b1 * h_i / h + b2 * i / h);
					} else {
						c = bgColor;
					}

					if (highlightColor >= 0 && i < h_2) {
						int rc = (c >> 16) & 0xff;
						int gc = (c >> 8) & 0xff;
						int bc = c & 0xff;
						int h_2_i = h_2 - i;
						c = ((rh * h_2_i / h_2 + rc * i / h_2) << 16)
								| ((gh * h_2_i / h_2 + gc * i / h_2) << 8)
								| (bh * h_2_i / h_2 + bc * i / h_2);
					}

					this.setColor(c);
					int resolvedY = top + i;
					this.drawLine(resolvedLeft, resolvedY, resolvedRight,
							resolvedY);
				}
			}
		}
	}

}

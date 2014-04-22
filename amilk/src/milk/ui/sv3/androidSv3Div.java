package milk.ui.sv3;

import milk.implement.sv3.MilkDiv9;
import milk.ui.MilkImageImpl;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import smartview3.elements.Sv3Div;
import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Page;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;

public class androidSv3Div extends MilkDiv9 {

	@Override
	protected void updateBuffer() {
		bufferImage = null;
	}

	public androidSv3Div(String id) {
		super(id);
	}

	public androidSv3Div(String id, Sv3Div prototype, Sv3Page page) {
		super(id, prototype, page);
	}

	MilkImage bufferImage;

	protected void renderImgBg(IRenderContext ctx, Rect bounding, Rect rect,
			boolean gradientFocus) {

		Object image = this.isFocused() ? focusImage : this.image;
		if (image == null) {
			image = this.image;
		}
		if (image != null) {

			Rect r = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (image != null) {
				// render the 9 patch background
				int imgw = imageUtil.getWidth(image);
				int imgh = imageUtil.getHeight(image);
				int w2 = marker[SIDE_RIGHT];
				int h2 = marker[SIDE_BOTTOM];
				int w0 = marker[SIDE_LEFT];
				int h0 = marker[SIDE_TOP];
				int x2 = imgw - w2;
				int y2 = imgh - h2;
				int x1 = w0;
				int y1 = h0;
				int w1 = x2 - x1;
				int h1 = y2 - y1;

				int rx = r.x;
				int ry = r.y;
				int rw = r.width;
				int rh = r.height;

				int cx = ctx.getClipX(), cy = ctx.getClipY(), cw = ctx
						.getClipWidth(), ch = ctx.getClipHeight();

				if (bufferImage == null) {
					MilkImage milkImage = (MilkImage) image;
					bufferImage = MilkImageImpl.createImage(rw, rh);
					MilkGraphics g = bufferImage.getGraphics();

					rx = 0;
					ry = 0;
					cx = 0;
					cy = 0;
					cw = rw;
					ch = rh;

					g.setClip(cx, cy, cw, ch);

					// center
					g.setColor(getFillColor());

					g.clipRect(rx + x1, ry + y1, rw - w0 - w2, rh - h0 - h2);
					g.fillRect(rx + x1, ry + y1, rw - w0 - w2, rh - h0 - h2);

					g.setClip(cx, cy, cw, ch);

					if (w1 > 0) {
						for (int x = x1; x < rw - w2; x += w1) {
							g.clipRect(rx + x, ry, Math.min(w1, rw - w2 - x),
									h0);
							g.drawImage(milkImage, rx + x - w0, ry,
									MilkGraphics.LEFT | MilkGraphics.TOP);
							g.setClip(cx, cy, cw, ch);

							g.clipRect(rx + x, ry + rh - h2,
									Math.min(w1, rw - w2 - x), h2);
							g.drawImage(milkImage, rx + x - w0, ry + rh - imgh,
									MilkGraphics.LEFT | MilkGraphics.TOP);
							g.setClip(cx, cy, cw, ch);
						}
					}

					// left // right
					if (h1 > 0) {
						for (int y = y1; y < rh - h2; y += h1) {
							g.clipRect(rx, ry + y, w0,
									Math.min(h1, rh - h2 - y));
							g.drawImage(milkImage, rx, ry + y - h0,
									MilkGraphics.LEFT | MilkGraphics.TOP);
							g.setClip(cx, cy, cw, ch);

							g.clipRect(rx + rw - w2, ry + y, w2,
									Math.min(h1, rh - h2 - y));
							g.drawImage(milkImage, rx + rw - imgw, ry + y - h0,
									MilkGraphics.LEFT | MilkGraphics.TOP);
							g.setClip(cx, cy, cw, ch);
						}
					}

					// top left
					g.clipRect(rx, ry, w0, h0);
					g.drawImage(milkImage, rx, ry, MilkGraphics.LEFT
							| MilkGraphics.TOP);
					g.setClip(cx, cy, cw, ch);

					// top right
					g.clipRect(rx + rw - w2, ry, w2, h0);
					g.drawImage(milkImage, rx + rw - imgw, ry,
							MilkGraphics.LEFT | MilkGraphics.TOP);
					g.setClip(cx, cy, cw, ch);

					// bottom left
					g.clipRect(rx, ry + rh - h2, w0, h2);
					g.drawImage(milkImage, rx, ry + rh - imgh,
							MilkGraphics.LEFT | MilkGraphics.TOP);
					g.setClip(cx, cy, cw, ch);

					// bottom right
					g.clipRect(rx + rw - w2, ry + rh - h2, w2, h2);
					g.drawImage(milkImage, rx + rw - imgw, ry + rh - imgh,
							MilkGraphics.LEFT | MilkGraphics.TOP);
					g.setClip(cx, cy, cw, ch);

					// restore the clip settings
					// ctx.setClip(oldClip.x, oldClip.y, oldClip.width,
					// oldClip.height);
					g.setClip(cx, cy, cw, ch);

				}

				if (bufferImage != null) {
					// ctx.setColor(0);
					// ctx.fillRect(rx, ry, rw, rh);
					ctx.drawImage(bufferImage, r.x, r.y);
				}

			}

		}

	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new androidSv3Div(id, this, page);
	}
}

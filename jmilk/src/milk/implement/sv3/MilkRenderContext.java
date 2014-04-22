package milk.implement.sv3;

import milk.ui2.MilkGraphics;

import mobile.RenderContext;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;

public class MilkRenderContext extends RenderContext {

	private MilkRenderContext subCtx;

	public MilkRenderContext(MilkGraphics g, Rect bounding, Rect viewPort) {
		super(g, bounding, viewPort);
	}

	public void reset(MilkGraphics g, Rect bounding, Rect viewPort) {
		this.setGraphics(g);
		this.bounding.copy(bounding);
		this.viewPort.copy(viewPort);
	}

	public synchronized IRenderContext getSubCtx(Rect padded, Rect viewport) {
		if (subCtx == null) {
			subCtx = new MilkRenderContext(g, padded, viewport);
		} else {
			subCtx.setGraphics(g);
			subCtx.getBounding().copy(padded);
			subCtx.getViewPort().copy(viewport);
		}
		return subCtx;
	}

	public void fillGradientRect(Rect r, int bgColor, int bgColor2) {
		if (bgColor >= 0) {
			if (bgColor2 >= 0) {
				int r1 = (bgColor >> 16) & 0xff;
				int g1 = (bgColor >> 8) & 0xff;
				int b1 = bgColor & 0xff;
				int left = r.x;
				int right = r.getRight();
				int top = r.y;
				int h = r.height - 1;
				if (bgColor2 != bgColor) {
					int r2 = (bgColor2 >> 16) & 0xff;
					int g2 = (bgColor2 >> 8) & 0xff;
					int b2 = bgColor2 & 0xff;

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
					int len = r.height / 2;
					for (int i = 0; i < len; i++) {
						int c = blend(45 + (len - i), r1, g1, b1);
						this.setColor(c);
						this.drawLine(left, top + i, right - 1, top + i);
					}
					this.setColor(bgColor);
					this.fillRect(r.x, r.y + r.height / 2, r.width,
							r.height / 2);
				}

			} else {
				this.setColor(bgColor);
				this.fillRect(r.x, r.y, r.width, r.height);
			}
		}
	}

	private static int blend(int alpha, int r, int g, int b) {
		int c = 0xFF;
		r = (r * (255 - alpha) + (c * alpha)) / 255;
		g = (g * (255 - alpha) + (c * alpha)) / 255;
		b = (b * (255 - alpha) + (c * alpha)) / 255;
		return (r << 16) | (g << 8) | (b);
	}

}

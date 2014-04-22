package milk.ui;

import milk.ui2.MilkGraphics;
import milk.ui2.ShrinkRect;

public class ShrinkRectImpl implements ShrinkRect {

	private RoundRectImpl rect;

	public ShrinkRectImpl(String path, int frameSize, int fillColor) {
		rect = new RoundRectImpl(path, frameSize, fillColor);
	}

	public void drawRoundRect(MilkGraphics g, int x, int y, int w, int h) {
		rect.drawRoundRect(g, x, y, w, h);
	}

}

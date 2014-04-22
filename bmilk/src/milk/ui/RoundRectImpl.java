package milk.ui;

import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;
import milk.ui2.RoundRect;

public class RoundRectImpl implements RoundRect {

	private final int fillColor;
	private final int frameTotalSize;
	private final MilkSprite lineUp, lineDown, lefLine, rightLine;
	private final MilkSprite ltCorner, lrCorner, lbCorner, rbCorner;
	private MilkSprite inside;

	public RoundRectImpl(MilkImage image, int frameSize, int fillColor) {
		frameTotalSize = frameSize;
		int size = frameSize;
		this.fillColor = fillColor;
		ltCorner = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, 0, size, size, MilkSprite.TRANS_NONE));
		lrCorner = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, image.getWidth() - size, 0, size, size,
						MilkSprite.TRANS_NONE));
		lbCorner = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, image.getHeight() - size, size, size,
						MilkSprite.TRANS_NONE));
		rbCorner = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, image.getWidth() - size, image.getHeight()
						- size, size, size, MilkSprite.TRANS_NONE));

		lineUp = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, size, 0, image.getWidth() - size * 2, size,
						MilkSprite.TRANS_NONE));
		lineDown = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, size, image.getHeight() - size,
						image.getWidth() - size * 2, size,
						MilkSprite.TRANS_NONE));
		lefLine = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, frameSize, size, image.getHeight() - 2
						* size, MilkSprite.TRANS_NONE));
		rightLine = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, image.getWidth() - size, frameSize, size,
						image.getHeight() - 2 * size, MilkSprite.TRANS_NONE));

		if (fillColor == 0) {
			inside = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
					.createImage(image, size, size, size, size,
							MilkSprite.TRANS_NONE));
		}
		image = null;
	}

	public RoundRectImpl(String path, int frameSize, int fillColor) {
		this(Utils.getImage(path), frameSize, fillColor);
	}

	public void drawHorizonLine(MilkGraphics g, int x, int y, int w) {
		int oldWidth = g.getClipWidth();
		int oldHeight = g.getClipHeight();
		g.setClip(x, y, w, oldHeight - y);
		int drawTemp = x;
		while ((x + lineUp.getWidth()) < w + drawTemp) {
			lineUp.setTransform(MilkSprite.TRANS_NONE);
			lineUp.setPosition(x, y);
			lineUp.paint(g);
			x += lineUp.getWidth();
		}
		g.setClip(0, 0, oldWidth, oldHeight);
	}

	public void drawRoundRect(MilkGraphics g, int x, int y, int w, int h) {
		int oldWidth = g.getClipWidth();
		int oldHeight = g.getClipHeight();
		g.setClip(x + frameTotalSize, y, w - frameTotalSize * 2, h);
		int oriValue = x;
		while (x < w + oriValue) {// up,down border
			lineUp.setTransform(MilkSprite.TRANS_NONE);
			lineUp.setPosition(x + frameTotalSize, y);
			lineUp.paint(g);
			lineDown.setTransform(MilkSprite.TRANS_NONE);
			lineDown.setPosition(x + frameTotalSize, y + h - frameTotalSize);
			lineDown.paint(g);
			x += lineUp.getWidth();
		}
		x = oriValue;

		oriValue = y;
		g.setClip(x, y + frameTotalSize, w, h - frameTotalSize * 2);
		while (y < h + oriValue) {// left,right border
			lefLine.setTransform(MilkSprite.TRANS_NONE);
			lefLine.setPosition(x, y + frameTotalSize);
			lefLine.paint(g);
			rightLine.setTransform(MilkSprite.TRANS_NONE);
			rightLine.setPosition(x + w - frameTotalSize, y + frameTotalSize);
			rightLine.paint(g);
			y += lefLine.getHeight();
		}
		y = oriValue;
		g.setClip(x, y, w, h);

		// 4 corner
		ltCorner.setTransform(MilkSprite.TRANS_NONE);
		ltCorner.setPosition(x, y);
		ltCorner.paint(g);

		lrCorner.setTransform(MilkSprite.TRANS_NONE);
		lrCorner.setPosition(x + w - ltCorner.getWidth(), y);
		lrCorner.paint(g);

		rbCorner.setTransform(MilkSprite.TRANS_NONE);
		rbCorner.setPosition(x + w - ltCorner.getWidth(),
				y + h - ltCorner.getHeight());
		rbCorner.paint(g);

		lbCorner.setTransform(MilkSprite.TRANS_NONE);
		lbCorner.setPosition(x, y + h - ltCorner.getHeight());
		lbCorner.paint(g);
		if (inside != null) {
			fillInsideRect(g, x + frameTotalSize, y + frameTotalSize, w - 2
					* frameTotalSize, h - 2 * frameTotalSize);
		} else {
			g.setColor(fillColor);
			g.fillRect(x + frameTotalSize, y + frameTotalSize, w - 2
					* frameTotalSize, h - 2 * frameTotalSize);
		}
		g.setClip(0, 0, oldWidth, oldHeight);
	}

	private void fillInsideRect(MilkGraphics g, int x, int y, int w, int h) {
		int size = inside.getWidth();
		g.setClip(x, y, w, h);
		for (int i = 0; i * size < w; i++) {
			for (int j = 0; j * size < h; j++) {
				inside.setPosition(x + i * size, y + j * size);
				inside.paint(g);
			}
		}
	}

}

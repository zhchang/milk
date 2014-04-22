package milk.ui;

import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;
import milk.ui2.ShrinkRect;

public class ShrinkRectImpl implements ShrinkRect {

	private final int fillColor;
	private final int frameTotalSize;
	private final int lineLength;
	private final MilkSprite cornerUp, cornerDown, lineUp, lineDown;

	// private RoundRect rect;

	public ShrinkRectImpl(String path, int frameSize, int fillColor) {
		// rect = new RoundRect(path, frameSize, fillColor);
		MilkImage image = Utils.getImage(path);
		if (image == null) {
			throw new NullPointerException("image==null,path=" + path);
		}
		frameTotalSize = frameSize;
		int size = frameSize;
		this.fillColor = fillColor;
		cornerUp = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, 0, size, size, MilkSprite.TRANS_NONE));
		cornerDown = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, image.getHeight() - size, size, size,
						MilkSprite.TRANS_NONE));

		lineUp = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, size, 0, image.getWidth() - size * 2, size,
						MilkSprite.TRANS_NONE));
		lineDown = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, size, image.getHeight() - size,
						image.getWidth() - size * 2, size,
						MilkSprite.TRANS_NONE));

		lineLength = lineUp.getWidth();
		image = null;
	}

	public void drawRoundRect(MilkGraphics g, int x, int y, int w, int h) {
		// rect.drawRoundRect(g, x, y, w, h);
		int oldWidth = g.getClipWidth();
		int oldHeight = g.getClipHeight();
		g.setClip(x + frameTotalSize, y, w - frameTotalSize * 2, h);
		int oriValue = x;
		while (x <= w + oriValue) {// up,down border
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
			lineUp.setTransform(MilkSprite.TRANS_MIRROR_ROT270);
			lineUp.setPosition(x, y + frameTotalSize);
			lineUp.paint(g);
			lineUp.setTransform(MilkSprite.TRANS_ROT90);
			lineUp.setPosition(x + w - frameTotalSize, y + frameTotalSize);
			lineUp.paint(g);
			y += lineLength;
		}
		y = oriValue;
		g.setClip(x, y, w, h);

		// 4 corner
		cornerUp.setTransform(MilkSprite.TRANS_NONE);
		cornerUp.setPosition(x, y);
		cornerUp.paint(g);

		cornerUp.setTransform(MilkSprite.TRANS_ROT90);
		cornerUp.setPosition(x + w - cornerUp.getWidth(), y);
		cornerUp.paint(g);

		cornerDown.setTransform(MilkSprite.TRANS_MIRROR);
		cornerDown.setPosition(x + w - cornerUp.getWidth(),
				y + h - cornerUp.getHeight());
		cornerDown.paint(g);

		cornerDown.setTransform(MilkSprite.TRANS_NONE);
		cornerDown.setPosition(x, y + h - cornerUp.getHeight());
		cornerDown.paint(g);
		g.setColor(fillColor);
		g.fillRect(x + frameTotalSize, y + frameTotalSize, w - 2
				* frameTotalSize, h - 2 * frameTotalSize);

		g.setClip(0, 0, oldWidth, oldHeight);
	}

}

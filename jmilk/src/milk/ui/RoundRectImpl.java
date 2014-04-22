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
	private final int lineLength;
	private final MilkSprite corner, line;
	private MilkSprite inside;

	public RoundRectImpl(MilkImage image, int frameSize, int fillColor) {
		if (image == null) {
			throw new NullPointerException("image==null,path=" + image);
		}
		frameTotalSize = frameSize;
		int size = frameSize;
		this.fillColor = fillColor;
		corner = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, 0, size, size, MilkSprite.TRANS_NONE));
		line = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, size, 0, image.getWidth() - size * 2, size,
						MilkSprite.TRANS_NONE));
		lineLength = line.getWidth();
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
		while ((x + line.getWidth()) < w + drawTemp) {
			line.setTransform(MilkSprite.TRANS_NONE);
			line.setPosition(x, y);
			line.paint(g);
			x += line.getWidth();
		}
		g.setClip(0, 0, oldWidth, oldHeight);
	}

	public void drawRoundRect(MilkGraphics g, int x, int y, int w, int h) {
		int oldWidth = g.getClipWidth();
		int oldHeight = g.getClipHeight();
		g.setClip(x + frameTotalSize, y, w - frameTotalSize * 2, h);
		int oriValue = x;
		while (x < w + oriValue) {// up,down border
			line.setTransform(MilkSprite.TRANS_NONE);
			line.setPosition(x + frameTotalSize, y);
			line.paint(g);
			line.setTransform(MilkSprite.TRANS_ROT180);
			line.setPosition(x + frameTotalSize, y + h - frameTotalSize);
			line.paint(g);
			x += line.getWidth();
		}
		x = oriValue;

		oriValue = y;
		g.setClip(x, y + frameTotalSize, w, h - frameTotalSize * 2);
		while (y < h + oriValue) {// left,right border
			line.setTransform(MilkSprite.TRANS_MIRROR_ROT270);
			line.setPosition(x, y + frameTotalSize);
			line.paint(g);
			line.setTransform(MilkSprite.TRANS_ROT90);
			line.setPosition(x + w - frameTotalSize, y + frameTotalSize);
			line.paint(g);
			y += lineLength;
		}
		y = oriValue;
		g.setClip(x, y, w, h);

		// 4 corner
		corner.setTransform(MilkSprite.TRANS_NONE);
		corner.setPosition(x, y);
		corner.paint(g);

		corner.setTransform(MilkSprite.TRANS_ROT90);
		corner.setPosition(x + w - corner.getWidth(), y);
		corner.paint(g);

		corner.setTransform(MilkSprite.TRANS_ROT180);
		corner.setPosition(x + w - corner.getWidth(),
				y + h - corner.getHeight());
		corner.paint(g);

		corner.setTransform(MilkSprite.TRANS_ROT270);
		corner.setPosition(x, y + h - corner.getHeight());
		corner.paint(g);
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

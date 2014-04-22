package milk.ui.j2mechat;

import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;

public class J2meTab {

	private static final boolean pixelLevelPointerEventCheck = true;
	private MilkSprite triangle, rect, tmpCollides, triangle2;
	private int triangleWidth;

	J2meTab(String path, int triangleWidth) {
		MilkImage image = Utils.getImage(path);
		if (image == null) {
			throw new NullPointerException("image==null,path=" + path);
		}
		triangle = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, 0, 0, triangleWidth, image.getHeight(),
						MilkSprite.TRANS_NONE));

		triangle2 = Adaptor.uiFactory
				.createMilkSprite(Adaptor.uiFactory.createImage(image,
						image.getWidth() - triangleWidth, 0, triangleWidth,
						image.getHeight(), MilkSprite.TRANS_NONE));

		rect = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
				.createImage(image, triangleWidth, 0, image.getWidth() - 2
						* triangleWidth, image.getHeight(),
						MilkSprite.TRANS_NONE));

		if (pixelLevelPointerEventCheck)
			tmpCollides = Adaptor.uiFactory.createMilkSprite(Adaptor.uiFactory
					.createImage(image, triangleWidth, 0, 1, 1,
							MilkSprite.TRANS_NONE));
		this.triangleWidth = triangleWidth;
	}

	void drawTab(MilkGraphics g, int x, int y, int w) {
		int oldWidth = g.getClipWidth();
		int oldHeight = g.getClipHeight();
		g.setClip(x, y, w, oldHeight);
		triangle.setTransform(MilkSprite.TRANS_NONE);
		triangle.setPosition(x, y);
		triangle.paint(g);
		triangle2.setTransform(MilkSprite.TRANS_NONE);
		triangle2.setPosition(x + w - triangle.getWidth(), y);
		triangle2.paint(g);
		g.clipRect(x + triangle.getWidth() - 1, y, w - 2 * triangle.getWidth()
				+ 2, oldHeight);
		int tempW = rect.getWidth() - 1;
		for (int i = 0; tempW * i < w; i++) {
			rect.setPosition(x + triangle.getWidth() - 1 + tempW * i, y);
			rect.paint(g);
		}
		g.setClip(0, 0, oldWidth, oldHeight);
	}

	int getHeight() {
		return rect.getHeight();
	}

	int getTriangleWidth() {
		return triangleWidth;
	}

	boolean isTouch(int x, int y, int tabX, int tabY, int tabW) {
		int rectX = tabX + getTriangleWidth();
		int rectW = tabW - 2 * getTriangleWidth();
		if (Utils.pointInRect(x, y, rectX, tabY, rectW, getHeight())) {
			return true;
		}
		if (pixelLevelPointerEventCheck) {
			tmpCollides.setPosition(x, y);
			triangle.setTransform(MilkSprite.TRANS_NONE);
			triangle.setPosition(tabX, tabY);
			if (triangle.collidesWith(tmpCollides, true)) {
				return true;
			}
			triangle2.setTransform(MilkSprite.TRANS_NONE);
			triangle2.setPosition(tabX + tabW - triangle.getWidth(), tabY);
			if (triangle2.collidesWith(tmpCollides, true)) {
				return true;
			}
		} else {
			if (y > tabY && y < tabY + getHeight()) {
				int leftTriangleX = tabX;
				int rightTriangleX = tabX + tabW - getTriangleWidth();
				int offsetY = y - tabY;
				if (x > leftTriangleX && x < leftTriangleX + getTriangleWidth()) {
					int offsetX = x - leftTriangleX;
					return offsetX <= offsetY * getTriangleWidth()
							/ getHeight();
				} else if (x > rightTriangleX
						&& x < rightTriangleX + getTriangleWidth()) {
					int offsetX = x - rightTriangleX;
					return offsetX <= offsetY * getTriangleWidth()
							/ getHeight();
				}

			}
		}
		return false;
	}

}

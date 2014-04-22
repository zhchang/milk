package milk.ui;

import milk.ui.graphics.Sprite;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;


public class MilkSpriteImpl implements MilkSprite{

	private Sprite sprite;

//	public static final int TRANS_MIRROR = Sprite.TRANS_MIRROR;
//	public static final int TRANS_MIRROR_ROT180 = Sprite.TRANS_MIRROR_ROT180;
//	public static final int TRANS_MIRROR_ROT270 = Sprite.TRANS_MIRROR_ROT270;
//	public static final int TRANS_MIRROR_ROT90 = Sprite.TRANS_MIRROR_ROT90;
//	public static final int TRANS_NONE = Sprite.TRANS_NONE;
//	public static final int TRANS_ROT180 = Sprite.TRANS_ROT180;
//	public static final int TRANS_ROT270 = Sprite.TRANS_ROT270;
//	public static final int TRANS_ROT90 = Sprite.TRANS_ROT90;

	public MilkSpriteImpl(MilkImage image) {
		sprite = new Sprite(((MilkImageImpl)image).image.getImg());
	}

	public MilkSpriteImpl(MilkImage image, int frameWidth, int frameHeight) {
		sprite = new Sprite(((MilkImageImpl)image).image.getImg(), frameWidth, frameHeight);
	}

	public void setFrame(int frame) {
		sprite.setFrame(frame);
	}

	public int getWidth() {
		return sprite.getWidth();
	}

	public int getHeight() {
		return sprite.getHeight();
	}

	public void setTransform(int transform) {
		sprite.setTransform(transform);
	}

	public void setPosition(int x, int y) {
		sprite.setPosition(x, y);
	}

	public void paint(MilkGraphics g) {
		sprite.paint(((MilkGraphicsImpl)g).getG().getCanvas());
	}

	public boolean collidesWith(MilkSprite s, boolean pixelLevel) {
		return sprite.collidesWith(((MilkSpriteImpl)s).sprite, pixelLevel);
	}

}

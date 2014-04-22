package milk.ui;

import javax.microedition.lcdui.game.Sprite;

import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;

public class MilkSpriteImpl implements MilkSprite{

	Sprite sprite;

	public MilkSpriteImpl(MilkImage image) {
		sprite = new Sprite(((MilkImageImpl)image).image);
	}

	 public MilkSpriteImpl(MilkImage image, int frameWidth, int frameHeight) {
		sprite = new Sprite(((MilkImageImpl)image).image, frameWidth, frameHeight);
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
		sprite.paint(((MilkGraphicsImpl)g).getG());
	}

	public boolean collidesWith(MilkSprite s, boolean pixelLevel) {
		return sprite.collidesWith(((MilkSpriteImpl)s).sprite, pixelLevel);
	}

}

package milk.ui2;

public interface MilkSprite {

	int TRANS_MIRROR = 2;// Sprite.TRANS_MIRROR;
	int TRANS_MIRROR_ROT180 = 1;// Sprite.TRANS_MIRROR_ROT180;
	int TRANS_MIRROR_ROT270 = 4;// Sprite.TRANS_MIRROR_ROT270;
	int TRANS_MIRROR_ROT90 = 7;// Sprite.TRANS_MIRROR_ROT90;
	int TRANS_NONE = 0;// Sprite.TRANS_NONE;
	int TRANS_ROT180 = 3;// Sprite.TRANS_ROT180;
	int TRANS_ROT270 = 6;// Sprite.TRANS_ROT270;
	int TRANS_ROT90 = 5;// Sprite.TRANS_ROT90;

	void setFrame(int frame);

	int getWidth();

	int getHeight();

	void setTransform(int transform);

	void setPosition(int x, int y);

	void paint(MilkGraphics g);

	boolean collidesWith(MilkSprite s, boolean pixelLevel);
}

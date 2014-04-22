package milk.ui;

import milk.implement.Adaptor;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.image.Image;
import net.rim.device.api.ui.image.ImageFactory;

public class MilkImageImpl implements MilkImage {

	Bitmap bitmap = null;
	Image image = null;

	public static MilkImage createImage(String path) {
		try {
			byte[] bytes = Adaptor.milk.readMutable(path);
			return createImage(bytes, 0, bytes.length);
		} catch (Exception e) {
			return null;
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public static MilkImage createImage(byte[] bytes, int imageOffset,
			int imageLength) {
		return new MilkImageImpl(Bitmap.createBitmapFromBytes(bytes,
				imageOffset, imageLength, 1));
	}

	public static MilkImage createImage(int w, int h) {
		Bitmap bitmap = new Bitmap(w, h);
		return new MilkImageImpl(bitmap);
	}

	public static Bitmap transform(Bitmap bitmap, int transition) {
		ImageManipulator im = new ImageManipulator(bitmap);
		switch (transition) {
		case MilkSprite.TRANS_MIRROR: {
			im.transformByAngle(0, true, false);
			break;
		}
		case MilkSprite.TRANS_MIRROR_ROT180: {
			im.transformByAngle(180, true, false);
			break;
		}
		case MilkSprite.TRANS_MIRROR_ROT270: {
			im.transformByAngle(270, true, false);
			break;
		}
		case MilkSprite.TRANS_MIRROR_ROT90: {
			im.transformByAngle(90, true, false);
			break;
		}
		case MilkSprite.TRANS_NONE: {
			break;
		}
		case MilkSprite.TRANS_ROT180: {
			im.transformByAngle(180, false, false);
			break;
		}
		case MilkSprite.TRANS_ROT270: {
			im.transformByAngle(270, false, false);
			break;
		}
		case MilkSprite.TRANS_ROT90: {
			im.transformByAngle(90, false, false);
			break;
		}
		}
		return im.transformAndPaintBitmap();
	}

	public static MilkImage createImage(MilkImage oriImage, int x, int y,
			int w, int h, int transition) {
		int rbg[] = new int[w * h];
		((MilkImageImpl) oriImage).bitmap.getARGB(rbg, 0, w, x, y, w, h);
		Bitmap temp = new Bitmap(w, h);
		temp.setARGB(rbg, 0, w, 0, 0, w, h);
		return new MilkImageImpl(temp);
	}

	public static MilkImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.setARGB(rgb, 0, width, 0, 0, width, height);
		return new MilkImageImpl(bitmap);
	}

	public MilkGraphics getGraphics() {
		MilkGraphicsImpl mg = new MilkGraphicsImpl();
		Graphics g = Graphics.create(bitmap);
		mg.setG(g);
		return mg;
	}

	private MilkImageImpl(Bitmap bitmap) {
		this.bitmap = bitmap;
		image = ImageFactory.createImage(bitmap);
	}

	public int getWidth() {
		return bitmap.getWidth();
	}

	public int getHeight() {
		return bitmap.getHeight();
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y,
			int width, int height) {
		bitmap.getARGB(rgbData, offset, scanlength, x, y, width, height);
	}

}

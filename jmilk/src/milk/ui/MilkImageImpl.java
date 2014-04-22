package milk.ui;

import javax.microedition.lcdui.Image;

import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class MilkImageImpl implements MilkImage {

	Image image = null;
	MilkGraphics mg = null;

	public static MilkImage createImage(String path) {
		try {
			return new MilkImageImpl(Image.createImage(path));
		} catch (Exception e) {
			// System.out.println("-----createImage Exception path:"+path);
			return null;
		}
	}

	public static MilkImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		return new MilkImageImpl(Image.createRGBImage(rgb, width, height,
				processAlpha));

	}

	public static MilkImage createImage(byte[] bytes, int imageOffset,
			int imageLength) {
		return new MilkImageImpl(Image.createImage(bytes, imageOffset,
				imageLength));
	}

	public static MilkImage createImage(int w, int h) {
		return new MilkImageImpl(Image.createImage(w, h));
	}

	public static MilkImage createImage(MilkImage image, int x, int y, int w,
			int h, int transition) {
		return new MilkImageImpl(Image.createImage(
				((MilkImageImpl) image).image, x, y, w, h, transition));
	}

	public MilkGraphics getGraphics() {
		if (mg == null) {
			mg = new MilkGraphicsImpl();
		}
		((MilkGraphicsImpl) mg).setG(image.getGraphics());
		return mg;
	}

	public MilkImageImpl(Image image) {
		this.image = image;
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y,
			int width, int height) {
		image.getRGB(rgbData, offset, scanlength, x, y, width, height);
	}

}

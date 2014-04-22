package milk.ui;

import milk.ui.graphics.Image;
import milk.ui2.MilkFont;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;



public class UIHelper {
	
	public static MilkAppImpl milk;
	
	public static MilkFont getDefaultFont(){
		return MilkFontImpl.getDefaultFont();
	}
	
	public static MilkFont getFont(int style, int size) {
        return MilkFontImpl.getFont(style, size);
	}
	
	public static MilkSprite createMilkSprite(MilkImage image, int frameWidth, int frameHeight){
		return new MilkSpriteImpl(image,frameWidth,frameHeight);
	}
	
	public static MilkSprite createMilkSprite(MilkImage image){
		return new MilkSpriteImpl(image);
	}
	
	public static MilkImage createImage(String path) {
		try {
			return new MilkImageImpl(Image.createImage(path));
		} catch (Exception e) {
			System.out.println("-----createImage Exception path:"+path);
			return null;
		}
	}

	public static MilkImage createImage(byte[] bytes, int imageOffset,
			int imageLength) {
		return MilkImageImpl.createImage(bytes, imageOffset, imageLength);
	}

	public static MilkImage createImage(int w, int h) {
		return MilkImageImpl.createImage(w, h);
	}

	public static MilkImage createImage(MilkImage image, int x, int y, int w,
			int h, int transition) {
		return MilkImageImpl.createImage(image, x, y, w, h,
				transition);
	}

	public static MilkImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		return MilkImageImpl.createRGBImage(rgb, width, height,
				processAlpha);

	}
	
	public static MilkTiledLayerImpl createMilkTiledLayer(int cols, int rows, MilkImage image, int width,
			int height) {
		return new MilkTiledLayerImpl(cols, rows, image, width, height);
	}
	
	
}

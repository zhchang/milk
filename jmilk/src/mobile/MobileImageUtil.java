package mobile;

import milk.implement.Adaptor;
import milk.ui2.MilkImage;
import smartview3.utils.AbstractImageUtil;
import smartview3.utils.IImageRequester;

public class MobileImageUtil extends AbstractImageUtil {
	public int getWidth(Object image) {
		return ((MilkImage) image).getWidth();
	}

	public int getHeight(Object image) {
		return ((MilkImage) image).getHeight();
	}

	// public Object loadImage(String name) {
	// return null;
	// }

	public void loadImageAsync(String src, IImageRequester receiver, int width,
			int height) {
		String localResName = this.resolveAsLocalResource(src);
		if (localResName == null) {
			receiver.didReceiveImage(null, src);
		} else {
			receiver.didReceiveImage(
					loadLocalImage(localResName, width, height), src);
		}
	}

	public Object loadLocalImage(String name, int width, int height) {
		try {
			MilkImage decodedImage = Adaptor.uiFactory.createImage("/" + name);
			return decodedImage;
		} catch (Exception e) {
			return null;
		}
	}
}

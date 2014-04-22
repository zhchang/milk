package com.mozat.sv3.smartview3.utils;

public abstract class AbstractImageUtil implements IImageUtil {
	public final int getWidthSafely(Object image) {
		return image == null ? 0 : getWidth(image);
	}

	public final int getHeightSafely(Object image) {
		return image == null ? 0 : getHeight(image);
	}

	public abstract int getWidth(Object image);

	public abstract int getHeight(Object image);

	public String resolveAsLocalResource(String src) {
		if (src != null && src.startsWith("local://")) {
			return src.substring(8);
		} else {
			return null;
		}
	}

	public final Object loadLocalImage(String name) {
		return loadLocalImage(name, 0, 0);
	}

	public abstract Object loadLocalImage(String name, int width, int heigth);

	public final void loadImageAsync(String src, IImageRequester receiver) {
		loadImageAsync(src, receiver, 0, 0);
	}

	public abstract void loadImageAsync(String src, IImageRequester receiver,
			int width, int height);
}

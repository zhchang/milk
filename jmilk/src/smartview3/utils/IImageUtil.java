package smartview3.utils;

public interface IImageUtil {
	public int getWidthSafely(Object image);

	public int getHeightSafely(Object image);

	public int getWidth(Object image);

	public int getHeight(Object image);

	public String resolveAsLocalResource(String src);

	public Object loadLocalImage(String name);

	public Object loadLocalImage(String name, int width, int heigth);

	public void loadImageAsync(String src, IImageRequester receiver);

	public void loadImageAsync(String src, IImageRequester receiver, int width, int height);
}

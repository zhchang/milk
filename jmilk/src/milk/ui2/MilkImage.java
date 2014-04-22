package milk.ui2;



public interface MilkImage {

	MilkGraphics getGraphics();

	int getWidth();

	int getHeight();

	void getRGB(int[] rgbData, int offset, int scanlength, int x, int y,
			int width, int height);
}

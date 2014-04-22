package milk.ui2;

import milk.implement.Resource;
import milk.implement.TaskRunner;
import milk.implement.mk.MPlayer;
import milk.implement.mk.MRect;
import milk.implement.mk.MTiles;
import milk.implement.sv3.MilkDiv9;
import smartview3.elements.Sv3Div;
import smartview3.elements.Sv3Page;

public interface MilkUiFactory {

	public MilkTask createMilkTask(TaskRunner runner);

	public MilkImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha);

	public MilkImage createImage(byte[] bytes, Resource resource);

	public MilkImage createImage(int w, int h);

	public MilkImage createImage(String path);

	public MilkFont getDefaultFont();

	public MilkFont getFont(int style, int size);

	public MilkSprite createMilkSprite(MilkImage image, int frameWidth,
			int frameHeight, int loadW, int loadH);

	public MilkSprite createMilkSprite(MilkImage image);

	public MilkImage createImage(MilkImage image, int x, int y, int w, int h,
			int transition);

	public MilkTiledLayer createMilkTiledLayer(int cols, int rows,
			MilkImage image, int width, int height);

//	public NativeInput createNativeInput();

	public RoundRect createRoundRect(String path, int frameSize, int fillColor);

	public RoundRect createRoundRect(MilkImage image, int frameSize,
			int fillColor);

	public ShrinkRect createShrinkRect(String path, int frameSize, int fillColor);

	public MilkLocker createLocker();

	public MPlayer createMPlayer(MRect rect);

	public MPlayer createMPlayer(MPlayer player);

	public MPlayer createMPlayer(int x, int y, int w, int h);

	public MTiles createMTiles(String resourceId, int width, int height,
			int rows, int cols);

	public MTiles createMTiles(String resourceId);

	public MilkDiv9 createMilkDiv9(String id);

	public MilkDiv9 createMilkDiv9(String id, Sv3Div prototype, Sv3Page page);
}

package milk.ui;


import smartview3.elements.Sv3Div;

import smartview3.elements.Sv3Page;
import milk.implement.Resource;
import milk.implement.TaskRunner;
import milk.implement.mk.MPlayer;
import milk.implement.mk.MRect;
import milk.implement.mk.MTiles;
import milk.implement.sv3.MilkDiv9;
import milk.ui.graphics.Image;
import milk.ui.internal.Net;
import milk.ui.sv3.androidSv3Div;
import milk.ui2.MilkFont;
import milk.ui2.MilkImage;
import milk.ui2.MilkLocker;
import milk.ui2.MilkSprite;
import milk.ui2.MilkTask;
import milk.ui2.MilkTiledLayer;
import milk.ui2.MilkUiFactory;
import milk.ui2.RoundRect;
import milk.ui2.ShrinkRect;
import mobile.MilkJavaLocker;

public class MilkJavaUiFactory implements MilkUiFactory {
	public MilkTask createMilkTask(TaskRunner runner) {
		return new MilkTaskImpl(runner);
	}

	public MilkImage createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		return MilkImageImpl.createRGBImage(rgb, width, height, processAlpha);

	}

	public MilkImage createImage(byte[] bytes, int imageOffset, int imageLength) {
		return MilkImageImpl.createImage(bytes, imageOffset, imageLength);
	}

	public MilkImage createImage(int w, int h) {
		return MilkImageImpl.createImage(w, h);
	}

	public MilkImage createImage(String path) {
		try {
			return new MilkImageImpl(Image.createImage(path));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public MilkFont getDefaultFont() {
		return MilkFontImpl.getDefaultFont();
	}

	public MilkFont getFont(int style, int size) {
		return MilkFontImpl.getFont(style, size);
	}

	public MilkSprite createMilkSprite(MilkImage image, int frameWidth,
			int frameHeight) {
		return new MilkSpriteImpl(image, frameWidth, frameHeight);
	}

	public MilkSprite createMilkSprite(MilkImage image) {
		return new MilkSpriteImpl(image);
	}

	public MilkImage createImage(MilkImage image, int x, int y, int w, int h,
			int transition) {
		return MilkImageImpl.createImage(image, x, y, w, h, transition);
	}

	public MilkTiledLayer createMilkTiledLayer(int cols, int rows,
			MilkImage image, int width, int height) {
		return new MilkTiledLayerImpl(cols, rows, image, width, height);
	}

	public RoundRect createRoundRect(String path, int frameSize, int fillColor) {
		return new RoundRectImpl(path, frameSize, fillColor);
	}

	public RoundRect createRoundRect(MilkImage image, int frameSize,
			int fillColor) {
		return new RoundRectImpl(image, frameSize, fillColor);
	}

	public ShrinkRect createShrinkRect(String path, int frameSize, int fillColor) {
		return new ShrinkRectImpl(path, frameSize, fillColor);
	}
	public MilkLocker createLocker() {
		return new MilkJavaLocker();
	}

	@Override
	public MilkImage createImage(byte[] bytes, Resource resource)
	{
		return MilkImageImpl.createImage(bytes, 0, bytes.length);
	}

	@Override
	public MilkSprite createMilkSprite(MilkImage image, int frameWidth, int frameHeight, int loadW, int loadH)
	{
		return createMilkSprite(image, frameWidth, frameHeight);
	}


	@Override
	public MPlayer createMPlayer(MRect rect)
	{
		return new MPlayer(rect);
	}

	@Override
	public MPlayer createMPlayer(MPlayer player)
	{
		return new MPlayer(player);
	}

	@Override
	public MPlayer createMPlayer(int x, int y, int w, int h)
	{
		return new MPlayer(x, y, w, h);
	}

	@Override
	public MTiles createMTiles(String resourceId, int width, int height, int rows, int cols)
	{
		return new MTiles(resourceId, width, height, rows, cols);
	}

	@Override
	public MTiles createMTiles(String resourceId)
	{
		return new MTiles(resourceId);
	}

	@Override
	public MilkDiv9 createMilkDiv9(String id)
	{
		return new androidSv3Div(id);
	}

	@Override
	public MilkDiv9 createMilkDiv9(String id, Sv3Div prototype, Sv3Page page)
	{
		return new androidSv3Div(id, prototype, page);
	}
}

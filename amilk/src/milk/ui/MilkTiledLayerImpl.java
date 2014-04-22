package milk.ui;

import milk.implement.mk.MRect;
import milk.ui.graphics.TiledLayer;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkTiledLayer;


public class MilkTiledLayerImpl implements MilkTiledLayer {

	private TiledLayer tl;

	public MilkTiledLayerImpl(int cols, int rows, MilkImage image, int width,
			int height) {
		tl = new TiledLayer(cols, rows, ((MilkImageImpl)image).image.getImg(), width, height);
	}

	public void setCell(int col, int row, int tileIndex) {
		tl.setCell(col, row, tileIndex);
	}

	public int getCell(int col, int row) {
		return tl.getCell(col, row);
	}

	public int createAnimatedTile(int tileIndex) {
		if (tl != null) {
			return tl.createAnimatedTile(tileIndex);
		}
		return 0;
	}

	public int getAnimatedTile(int animatedIndex) {
		if (tl != null) {
			return tl.getAnimatedTile(animatedIndex);
		}
		return 0;
	}

	public void setAnimatedTile(int animatedIndex, int tileIndex) {
		if (tl != null) {
			tl.setAnimatedTile(animatedIndex, tileIndex);
		}
	}

	public void setPosition(int x, int y) {
		tl.setPosition(x, y);
	}

	public void paint(MilkGraphics g) {
		tl.paint(((MilkGraphicsImpl)g).getG().getCanvas());
	}

	@Override
	public void paint(MilkGraphics g, MRect viewPort)
	{
		tl.paint(((MilkGraphicsImpl)g).getG().getCanvas());
		
	}
}

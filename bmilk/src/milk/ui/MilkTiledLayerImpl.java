package milk.ui;

import java.util.Hashtable;
import java.util.Vector;

import milk.implement.mk.MRect;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkTiledLayer;
import net.rim.device.api.system.Bitmap;

public class MilkTiledLayerImpl implements MilkTiledLayer {

	int cols = 0;
	int rows = 0;
	Bitmap bitmap;
	Vector cells;
	int width = 0;
	int height = 0;
	int xCount = 0;
	int yCount = 0;

	int animatedTile = 0;
	Hashtable animatedTiles = new Hashtable();

	int posX = 0;
	int posY = 0;

	public MilkTiledLayerImpl(int cols, int rows, MilkImage image, int width,
			int height) {
		this.cols = cols;
		this.rows = rows;
		this.width = width;
		this.height = height;
		bitmap = ((MilkImageImpl) image).bitmap;

		xCount = (image.getWidth() / width);
		yCount = (image.getHeight() / height);

		cells = new Vector(rows);
		for (int i = 0; i < rows; i++) {
			Vector row = new Vector(cols);
			for (int k = 0; k < cols; k++) {
				row.addElement(new Integer(0));
			}
			cells.addElement(row);
		}

	}

	public void setCell(int col, int row, int tileIndex) {
		Vector temp = (Vector) cells.elementAt(row);
		temp.setElementAt(new Integer(tileIndex), col);
	}

	public int getCell(int col, int row) {
		Vector temp = (Vector) cells.elementAt(row);
		return ((Integer) temp.elementAt(col)).intValue();
	}

	public int createAnimatedTile(int tileIndex) {
		int key = --animatedTile;
		animatedTiles.put(new Integer(key), new Integer(0));
		return key;
	}

	public int getAnimatedTile(int animatedIndex) {
		if (animatedTiles.containsKey(new Integer(animatedIndex))) {
			return ((Integer) animatedTiles.get(new Integer(animatedIndex)))
					.intValue();
		} else {
			return 0;
		}
	}

	public void setAnimatedTile(int animatedIndex, int tileIndex) {
		animatedTiles.put(new Integer(animatedIndex), new Integer(tileIndex));
	}

	public void setPosition(int x, int y) {
		posX = x;
		posY = y;
	}

	public void paint(MilkGraphics g) {

		for (int i = 0; i < rows; i++) {
			for (int k = 0; k < cols; k++) {

				int index = getCell(k, i);
				if (index < 0) {
					index = getAnimatedTile(index);
				}
				if (index > 0) {
					int left = ((index - 1) % xCount) * width;
					int top = ((index - 1) / xCount) * height;
					((MilkGraphicsImpl) g).getG()
							.drawBitmap(posX + k * width, posY + i * height,
									width, height, bitmap, left, top);
				}

			}
		}

	}

	public void paint(MilkGraphics g, MRect viewPort) {
		paint(g);
	}

}

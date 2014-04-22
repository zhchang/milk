package milk.implement.mk;

import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.VectorPool;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkTiledLayer;

public class MTiles extends MDraw {

	protected static class MTilesAnim {
		Vector indexes = VectorPool.produce();
		long startTime;
		int animIndex;
		int interval;
	}

	protected int x;
	protected int y;
	protected int cols;
	protected int rows;
	protected int cellWidth;
	protected int cellHeight;

	MTilesAnim anim = null;

	MilkTiledLayer tl;

	public MTiles(String resourceId, int width, int height, int rows, int cols) {
		cellWidth = width;
		cellHeight = height;
		this.rows = rows;
		this.cols = cols;
		try {
			MilkImage image = Adaptor.getInstance().loadImageResource(
					resourceId);
			if (image != null) {
				tl = Adaptor.uiFactory.createMilkTiledLayer(cols, rows, image,
						width, height);
			}
		} catch (Exception t) {
			Adaptor.exception(t);
		}
	}

	public MTiles(String resourceId) {
		try {
			MilkImage image = Adaptor.getInstance().loadImageResource(
					resourceId);
			if (image != null) {

				int iw = image.getWidth();
				int ih = image.getHeight();
				calcDimensions(iw, ih);
				tl = Adaptor.uiFactory.createMilkTiledLayer(cols, rows, image,
						cellWidth, cellHeight);
				for (int i = 0; i < rows; i++) {
					for (int k = 0; k < cols; k++) {
						tl.setCell(k, i, i * cols + k + 1);
					}
				}
			}
		} catch (Exception t) {
			Adaptor.exception(t);
		}
	}

	public void startAnimation(MArray data) {
		try {
			MTilesAnim temp = new MTilesAnim();
			temp.interval = data.getInt(0);
			temp.animIndex = data.getInt(1);
			for (int i = 2; i < data.size(); i++) {
				temp.indexes.addElement(new Integer(data.getInt(i)));
			}
			temp.startTime = System.currentTimeMillis();
			this.anim = temp;
		} catch (Exception t) {

		}
	}

	public void stopAnimation() {
		this.anim = null;
	}

	void calcDimensions(int iw, int ih) {
		cellWidth = getPossibleUnit(iw);
		cols = iw / cellWidth;
		cellHeight = getPossibleUnit(ih);
		rows = ih / cellHeight;
	}

	int getPossibleUnit(int length) {
		if (length % 20 == 0) {
			return 20;
		} else if (length % 10 == 0) {
			return 10;
		} else if (length % 8 == 0) {
			return 8;
		} else if (length % 5 == 0) {
			return 5;
		}
		return length;
	}

	public void setCells(MArray cells) {
		if (tl != null && rows == cells.size()
				&& cols == cells.getArray(0).size()) {
			for (int i = 0; i < rows; i++) {
				MArray row = cells.getArray(i);
				for (int k = 0; k < cols; k++) {
					tl.setCell(k, i, row.getInt(k));
				}
			}
		}
	}

	public void setTileMode(int mode) {
		// to be implemented
	}

	public void setCell(int col, int row, int tileIndex) {
		if (tl != null) {
			tl.setCell(col, row, tileIndex);
		}
	}

	public int getCell(int row, int col) {
		if (tl != null) {
			return tl.getCell(col, row);
		}
		return 0;
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

	public int getWidth() {
		return cols * cellWidth;
	}

	public int getHeight() {
		return rows * cellHeight;
	}

	protected boolean canDraw(MRect screen, int xOffset, int yOffset) {
		return isVisible() && screen.intersacts(screen, xOffset, yOffset);
	}

	public boolean draw(MilkGraphics g, MRect screen, int xOffset, int yOffset) {
		if (this.isVisible() && tl != null) {
			if (screen.intersacts(x + xOffset, y + yOffset, cellWidth * cols,
					cellHeight * rows)) {
				tl.setPosition(x + xOffset, y + yOffset);
				tl.paint(g, screen);
			}
			return moveState != null || anim != null;
		}
		return false;
	}

	public void processAnimation(int xOffset, int yOffset) {
		if (this.isVisible() && anim != null) {
			try {
				long now = System.currentTimeMillis();
				int diff = (int) (now - anim.startTime);
				int index = (diff / anim.interval + (diff % anim.interval == 0 ? 0
						: 1));
				index = index % anim.indexes.size();
				this.setAnimatedTile(anim.animIndex,
						((Integer) anim.indexes.elementAt(index)).intValue());
			} catch (Exception t) {
				Adaptor.exception(t);
			}
		}
		return;
	}

	public MDraw matchFinger(int x, int y, int xOffset, int yOffset) {
		return null;
	}

	public void setX(int value) {
		x = value;

	}

	public void setY(int value) {
		y = value;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public MArray matchFingerToCell(int x, int y) {
		MArray thing = null;

		return thing;
	}

	public MArray matchCellToCoord(int x, int y) {
		MArray thing = null;

		return thing;
	}

}

package milk.implement.mk;

//import smartview3.layout.Rect;

public class MRect {

	public int x, y, width, height;

	public MRect() {

	}

	public MRect(int x, int y, int width, int height) {
		set(x, y, width, height);
	}

	public void set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public MRect(MRect rect) {
		if (rect == null) {
			this.x = 0;
			this.y = 0;
			this.width = 0;
			this.height = 0;
		} else {
			this.x = rect.x;
			this.y = rect.y;
			this.width = rect.width;
			this.height = rect.height;
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void move(int x, int y) {
		this.x += x;
		this.y += y;
	}

	public void resize(int wr, int hr) {
		int nw = wr * width / 1000;
		int nh = hr * height / 1000;
		x = x + width / 2 - nw / 2;
		y = y + height / 2 - nh / 2;
		width = nw;
		height = nh;
	}

	public boolean contains(int x, int y) {
		boolean xok = (this.x <= x && x <= this.x + this.width);
		boolean yok = (this.y <= y && y <= this.y + this.height);
		return xok && yok;
	}

	public boolean intersacts(int x, int y, int w, int h) {
		return !(x > this.x + this.width || x + w < this.x
				|| y > this.y + this.height || y + h < this.y);
	}

	public boolean contains(int x, int y, int w, int h) {
		boolean xok = (this.x <= x && x + w <= this.x + this.width);
		boolean yok = (this.y <= y && y + h <= this.y + this.height);
		return xok && yok;
	}

	public boolean intersacts(MRect thing, int xOffset, int yOffset) {

		return !(thing.x > x + width + xOffset
				|| thing.x + thing.width < x + xOffset
				|| thing.y > y + height + yOffset || thing.y + thing.height < y
				+ yOffset);
	}

}

package smartview3.layout;

public class Rect {

	public short x;
	public int y;
	public short width;
	public int height;

	public Rect() {
	}

	public Rect(Rect r) {
		this(r.x, r.y, r.width, r.height);
	}

	public Rect(int x, int y, int width, int height) {
		this.x = (short) x;
		this.y = y;
		this.width = (short) width;
		this.height = height;
	}
	
	public void copy(Rect r)
	{
		this.x = r.x;
		this.y = r.y;
		this.width = r.width;
		this.height = r.height;
	}
	public void copy(int x, int y, int w, int h)
	{
		this.x = (short)x;
		this.y = y;
		this.width = (short)w;
		this.height = h;
	}

//	public boolean overlapsWithOffset(int ox, int oy, Rect rect) {
//		return !(x + ox >= rect.x + rect.width || rect.x >= x + ox + width)
//				&& !(y + oy >= rect.y + rect.height || rect.y >= y + oy + height);
//	}
	
//	public Rect toRectWithOffset(int ox, int oy) {
//		return new Rect(x + ox, y + oy, width, height);
//	}

	public Rect toRectWithOffset(Rect offset, Rect out) {
		out.x = (short)(x + offset.x);
		out.y = y + offset.y;
		out.width = width;
		out.height = height;
		return out;
	}

//	public Rect toRectWithOffset(Rect offset) {
//		return new Rect(x + offset.x, y + offset.y, width, height);
//	}

//	public Rect toRectWithOffsetAndPadding(Rect offset, short[] padding) {
//		return new Rect(x + offset.x + padding[3], y + offset.y + padding[0], width - padding[1] - padding[3], height
//				- padding[0] - padding[2]);
//	}

	public Rect toRectWithPadding(short[] padding, Rect out) {
		out.x = (short)(x + padding[3]);
		out.y = y + padding[0];
		out.width = (short)(width - padding[1] - padding[3]);
		out.height = height - padding[0] - padding[2];
		return out;
	}
//	public Rect toRectWithPadding(short[] padding) {
//		return new Rect(x + padding[3], y + padding[0], width - padding[1] - padding[3], height - padding[0]
//				- padding[2]);
//	}

//	public Rect toRectWithOffsetAndPadding(Rect offset, short[] padding1, short[] padding2) {
//		return new Rect(x + offset.x + padding1[3] + padding1[3], y + offset.y + padding1[0] + padding2[0], width
//				- padding1[1] - padding1[3] - padding2[1] - padding2[3], height - padding1[0] - padding1[2]
//				- padding2[0] - padding2[2]);
//	}

//	public Rect toRectWithOffsetAndBorder(Rect offset, short[] border) {
//		return new Rect(x + offset.x + border[3] / 2, y + offset.y + border[0] / 2,
//				width - (border[1] + border[3]) / 2, height - (border[0] + border[2]) / 2);
//	}

	public int getRight() {
		return x + width;
	}

	public int getBottom() {
		return y + height;
	}

	public boolean overlaps(Rect rect) {
		return !(x >= rect.x + rect.width || rect.x >= x + width)
				&& !(y >= rect.y + rect.height || rect.y >= y + height);
	}

	public boolean contains(Rect rect) {
		return (x <= rect.x && rect.x + rect.width <= x + width) && (y <= rect.y && rect.y + rect.height <= y + height);
	}

	public boolean contains(int px, int py) {
		return px >= x && px < x + width && py >= y && py < y + height;
	}

	public String toString() {
		return "(" + x + "," + y + "," + width + "," + height + ")";
	}

	// public String toString(Rect offset) {
	// return "(" + x + "+" + offset.x + "," + y + "+" + offset.y + "," + width + "," + height + ")";
	// }
}

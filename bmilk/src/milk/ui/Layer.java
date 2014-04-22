package milk.ui;


public abstract class Layer extends Object {
	
	protected int xPosition;
	protected int yPosition;
	protected int width;
	protected int height;
	protected boolean isVisible = true;

	public void setPosition(int x, int y) {
		this.xPosition = x;
		this.yPosition = y;
	}
	

//	public void move(int dx, int dy) {
//		this.xPosition += dx;
//		this.yPosition += dy;
//	}
//
//	public final int getX() {
//		return this.xPosition;
//	}
//
//	public final int getY() {
//		return this.yPosition;
//	}

	public final int getWidth() {
		return this.width;
	}

	public final int getHeight() {
		return this.height;
	}

//	public void setVisible(boolean visible) {
//		this.isVisible = visible;
//	}
//
//	public final boolean isVisible() {
//		return this.isVisible;
//	}

//	public abstract void paint(MilkGraphics g);

}

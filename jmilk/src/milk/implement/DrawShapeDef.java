package milk.implement;

import milk.ui2.MilkGraphics;

public class DrawShapeDef {

	public int strokeColor;

	public int fillColor;

	public int offsetX;

	public int offsetY;

	public int gradientStart;

	public int gradientEnd;

	public int gradientType;

	public MilkGraphics g;

	public void useFillColor() {
		g.setColor(fillColor);
	}

	public void useStrokeColor() {
		g.setColor(strokeColor);
	}

	public int adjustX(int value) {
		return value + offsetX;
	}

	public int adjustY(int value) {
		return value + offsetY;
	}

}
